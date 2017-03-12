(ns oj.test
  (:require [clojure.test :refer :all]
            [oj.numberfun :as numberfun]
            [oj.nvc.core :as nvc]
            [oj.plots :as plots]))

(def test-seq (range 1000))
(deftest computes-fun-numbers
  (let [fun-numbers (numberfun/filter-fun-numbers test-seq)]
    (println "fun numbers: " fun-numbers)
    (is (= 48 (count fun-numbers)))))

(deftest number-contains
  (is (= true (numberfun/number-contains-number? 123456 45)))
  (is (= true (numberfun/number-contains-number? 123456 123456))))

(deftest number-starts-with
  (is (= true (numberfun/number-big-end-is-number? 123456 1234)))
  (is (= true (numberfun/number-big-end-is-number? 1 1)))
  (is (= false (numberfun/number-big-end-is-number? 2123456 1234))))

(deftest number-ends-with
  (is (= true (numberfun/number-little-end-is-number? 123456 456)))
  (is (= true (numberfun/number-little-end-is-number? 1 1)))
  (is (= false (numberfun/number-little-end-is-number? 21234565 456))))

(deftest accumulates-freq
  (let [numbers       #{1 3 5}
        expected      {0 0
                       1 1
                       2 1
                       3 2
                       4 2
                       5 3}
        expected-vecs [[0 0]
                       [1 1]
                       [2 1]
                       [3 2]
                       [4 2]
                       [5 3]]
        actuals       (numberfun/numbers->cumulative-truth-count numbers)]
    (is (= expected actuals))
    (is (= expected-vecs (numberfun/mapxy->vecsxy actuals)))))

(def causal-attributions ["attacked" "atakked" "attack"])
(deftest gets-feelings-and-needs-for-causal-attribution-with-fuzzy-match
  (let [mapped (map nvc/causal-attribution->primary-feelings-and-underlying-needs
                    causal-attributions)]
    (doseq [result mapped]
      (is (= :attacked (:matched-attribute result))))))

(deftest gets-underlying-needs-for-feeling
  (let [angry-feeling-1 "defiant"
        angry-feeling-2 "defyent"
        expected-needs  #{:belonging :connection :acknowledgement}]
    (is (= expected-needs (nvc/primary-feeling->underlying-needs angry-feeling-1)))
    (is (= expected-needs (nvc/primary-feeling->underlying-needs angry-feeling-2)))))

(def expected-recs '({:causal-attr-recs (), :needs-recs (), :sentence "I can't believe how much of an asshole you've been recently."} {:causal-attr-recs (), :needs-recs ({:word "calm", :recommendations ({:word "calm", :are-met-summary :peaceful, :are-met-synonyms [:blissful :calm :centered :clear-headed :mellow :quiet :serene :tranquil], :recommendation "You said: calm. Are you trying to say your need to feel :peaceful is being met? Or do these other words describe it better? [:blissful :calm :centered :clear-headed :mellow :quiet :serene :tranquil]"})}), :sentence "You treat me like shit and need to learn to calm the fuck down."}))

(deftest text->nvc
  (let [text "I can't believe how much of an asshole you've been recently.  You treat me like shit and need to learn to calm the fuck down."
        recs (nvc/text->nvc text)]
    (is (= expected-recs recs))))

(def test-plot-seq (range 10000))
(deftest plot
  (let [fun-numbers            (numberfun/filter-fun-numbers test-plot-seq)
        fun-numbers-big-end    (numberfun/filter-fun-numbers test-plot-seq :position :big-end)
        fun-numbers-little-end (numberfun/filter-fun-numbers test-plot-seq :position :little-end)
        fun-acc                (numberfun/numbers->cumulative-truth-count fun-numbers)
        fun-scatter            (numberfun/mapxy->vecsxy fun-acc)
        fun-big-acc                (numberfun/numbers->cumulative-truth-count fun-numbers-big-end)
        fun-big-scatter            (numberfun/mapxy->vecsxy fun-big-acc)
        fun-little-acc                (numberfun/numbers->cumulative-truth-count fun-numbers-little-end)
        fun-little-scatter            (numberfun/mapxy->vecsxy fun-little-acc)]
    (plots/do-plot fun-scatter "test-output.svg")
    (plots/do-plot fun-big-scatter "test-output-big-end.svg")
    (plots/do-plot fun-little-scatter "test-output-little-end.svg")))