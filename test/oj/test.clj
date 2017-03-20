(ns oj.test
  (:require [clojure.test :refer :all]
            [oj.numberfun :as numberfun]
            [oj.nvc.core :as nvc]
            [oj.plots :as plots]
            [me.raynes.fs :as fs]))

;; FUN-NUMBERS ----------------------------------------------------------------

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
        actuals       (numberfun/numbers->cumulative-truth-count numbers 0 5)]
    (is (= expected actuals))
    (is (= expected-vecs (numberfun/mapxy->vecsxy actuals)))))


;; NVC ----------------------------------------------------------------

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

;;TODO make this pass
#_(deftest sentences-classified-using-heuristics
    (let [order-sentence       "You need to shut up."
          need-sentence        "I just need you to understand me."
          feeling-sentence     "I'm just feeling shitty."
          observation-sentence "When you do stuff..."
          request-sentence     "Would you mind just quitting that?"]

      (is (= ::nvc/order (nvc/classify-sentence-using-heuristics order-sentence)))
      (is (= ::nvc/need (nvc/classify-sentence-using-heuristics need-sentence)))
      (is (= ::nvc/feeling (nvc/classify-sentence-using-heuristics feeling-sentence)))
      (is (= ::nvc/observation (nvc/classify-sentence-using-heuristics observation-sentence)))
      (is (= ::nvc/request (nvc/classify-sentence-using-heuristics request-sentence)))))

(deftest sentence-is-continuous-w-gaps
  (let [input1 [{:sentence-index 0} {:sentence-index 1} {:sentence-index 2}]
        input2 [nil nil {:sentence-index 2}]
        input3 [{:sentence-index 0} nil {:sentence-index 2}]
        input4 [{:sentence-index 0} nil {:sentence-index 2} nil {:sentence-index 2}]
        input5 [{:sentence-index 0} nil {:sentence-index 2} nil {:sentence-index 2} nil nil nil nil]]
    (is (= true (:does-match? (nvc/sentence-is-continuous-with-gaps? input1 0 3))))
    (is (= true (:does-match? (nvc/sentence-is-continuous-with-gaps? input2 2 1))))
    (is (= true (:does-match? (nvc/sentence-is-continuous-with-gaps? input3 1 2))))
    (is (= true (:does-match? (nvc/sentence-is-continuous-with-gaps? input4 1 3))))
    (is (= true (:does-match? (nvc/sentence-is-continuous-with-gaps? input5 1 3))))
    (is (= false (:does-match? (nvc/sentence-is-continuous-with-gaps? input2 1 2))))))


;; PLOTS ----------------------------------------------------------------

(deftest plot
  (println "Making a big plot...")
  (numberfun/have-fun 0 1000 :plotfile "test-plot-file-01.svg")
  (is (= true (fs/exists? "test-plot-file-01.svg"))))
