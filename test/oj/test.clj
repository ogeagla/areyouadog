(ns oj.test
  (:require [clojure.test :refer :all]))

(def test-seq (range 100))

(defn number->sum-of-digits [number]
  (->>
    number
    str
    (map (fn [digit]
           (->>
             digit
             str
             read-string)))
    (reduce +)))

(defn number-contains-number? [ref-number number-to-find]
  (let [ref-str (str ref-number)
        test-str (str number-to-find)]
    (.contains ref-str test-str)))

(deftest computes-some-numbers
  (doseq [test-no test-seq]
    (let [sum (number->sum-of-digits test-no)
          is-contained (number-contains-number? test-no sum)]
      (if is-contained
        (println "number contains the sum of its digits: " test-no)))))
