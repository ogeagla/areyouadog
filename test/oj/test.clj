(ns oj.test
  (:require [clojure.test :refer :all]
            [oj.numberfun :as numberfun]
            [oj.nvc :as nvc]))


(def test-seq (range 1000))

(deftest computes-some-numbers
  (let [fun-numbers (filter
                      (fn [item]
                        (numberfun/number-contains-number?
                          item
                          (numberfun/number->sum-of-digits item)))
                      test-seq)]
    (println "fun numbers: " fun-numbers)
    (is (= 48 (count fun-numbers)))))

(def causal-attributions ["attacked" "atakked"])

(deftest gets-feelings-and-needs-for-causal-attribution-with-fuzzy-match
  (let [mapped (map nvc/causal-attribution->primary-feelings-and-underlying-needs causal-attributions)]
    (doseq [result mapped]
      (is (= :attacked (:matched-attribute result))))))