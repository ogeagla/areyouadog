(ns oj.test
  (:require [clojure.test :refer :all]
            [oj.numberfun :as numberfun]))


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