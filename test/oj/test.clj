(ns oj.test
  (:require [clojure.test :refer :all]
            [oj.numberfun :as numberfun]
            [oj.nvc.core :as nvc]))

(def test-seq (range 1000))
(deftest computes-fun-numbers
  (let [fun-numbers (numberfun/filter-fun-numbers test-seq)]
    (println "fun numbers: " fun-numbers)
    (is (= 48 (count fun-numbers)))))

(deftest number-contains
  (is (= true (numberfun/number-contains-number? 123456 45)))
  (is (= true (numberfun/number-contains-number? 123456 123456))))

(def causal-attributions ["attacked" "atakked" "attack"])
(deftest gets-feelings-and-needs-for-causal-attribution-with-fuzzy-match
  (let [mapped (map nvc/causal-attribution->primary-feelings-and-underlying-needs
                    causal-attributions)]
    (doseq [result mapped]
      (is (= :attacked (:matched-attribute result))))))