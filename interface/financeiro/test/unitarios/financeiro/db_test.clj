(ns financeiro.db-test
  (:require [midje.sweet :refer :all]
            [financeiro.db :refer :all]))

(facts "Guarda uma transacao num atomo"
       (against-background [(before :facts (limpar))]

                           (fact "a colecao de transacoes inicia vazia"
                                 (count (transacoes)) => 0)

                           (fact "a transacao e o primeiro registro"
                                 (registrar {:valor 7 :tipo "receita"})
                                 => {:id 1 :valor 7 :tipo "receita"}
                                 (count (transacoes)) => 1)))

(facts "Calcula o saldo dada uma colecao de transacoes"
       (against-background [(before :facts (limpar))]
                           (fact "saldo e positivo quando so tem receita"
                                 (registrar {:valor 1 :tipo "receita"})
                                 (registrar {:valor 10 :tipo "receita"})
                                 (registrar {:valor 100 :tipo "receita"})
                                 (registrar {:valor 1000 :tipo "receita"})

                                 (saldo) => 1111)

                           (fact "saldo e negativo quando so tem despesa"
                                 (registrar {:valor 2 :tipo "despesa"})
                                 (registrar {:valor 20 :tipo "despesa"})
                                 (registrar {:valor 200 :tipo "despesa"})
                                 (registrar {:valor 2000 :tipo "despesa"})

                                 (saldo) => -2222)

                           (fact "saldo e a soma das receitas menos a soma das despesas"
                                 (registrar {:valor 2 :tipo "despesa"})
                                 (registrar {:valor 10 :tipo "receita"})
                                 (registrar {:valor 200 :tipo "despesa"})
                                 (registrar {:valor 1000 :tipo "receita"})

                                 (saldo) => 808)))

(facts "filtra transacoes por tipo"
       (def transacoes-aleatorias
         '({:valor 2 :tipo "despesa"}
           {:valor 10 :tipo "receita"}
           {:valor 200 :tipo "despesa"}
           {:valor 1000 :tipo "receita"}))

       (against-background
        [(before :facts
                 [(limpar)
                  (doseq [transacao transacoes-aleatorias]
                    (registrar transacao))])]

        (fact "encontra apenas as receitas"
              (transacoes-do-tipo "receita")
              => '({:valor 10 :tipo "receita"}
                   {:valor 1000 :tipo "receita"}))

        (fact "encontra apenas as despesas"
              (transacoes-do-tipo "despesa")
              => '({:valor 2 :tipo "despesa"}
                   {:valor 200 :tipo "despesa"}))))

(facts "filtra transações por rótulo"
       (def transacoes-aleatorias
         '({:valor 7.0M :tipo "despesa" :rotulos ["sorvete" "entretenimento"]}
           {:valor 88.0M :tipo "despesa" :rotulos ["livro" "educação"]}
           {:valor 106.0M :tipo "despesa" :rotulos ["curso" "educação"]}
           {:valor 8000.0M :tipo "receita" :rotulos ["salário"]}))
       (against-background
        [(before :facts [(limpar)
                         (doseq [transacao transacoes-aleatorias]
                           (registrar transacao))])]
        (fact "encontra a transação com rótulo 'salário'"
              (transacoes-com-filtro {:rotulos "salário"})
              => '({:valor 8000.0M :tipo "receita" :rotulos ["salário"]}))
        (fact "encontra as 2 transações com rótulo 'educação'"
              (transacoes-com-filtro {:rotulos ["educação"]})
              => '({:valor 88.0M :tipo "despesa" :rotulos ["livro" "educação"]}
                   {:valor 106.0M :tipo "despesa" :rotulos ["curso" "educação"]}))
        (fact "encontra as 2 transações com rótulo 'livro' ou 'curso'"
              (transacoes-com-filtro {:rotulos ["livro" "curso"]})
              => '({:valor 88.0M :tipo "despesa" :rotulos ["livro" "educação"]}
                   {:valor 106.0M :tipo "despesa" :rotulos ["curso" "educação"]}))))

