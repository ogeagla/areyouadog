(ns oj.numberfun
  (:require [clojure.test :refer :all]))

(defn numbers->cumulative-truth-count [numbers]
  "For a given input vector of numbers,
  [.. 3 1 5 ...] return the cumulative count
  of numbers in the vector like so:
  [{:x 0 :y 0}
  {:x 1 :y 1}
  {:x 2 :y 1}
  {:x 3 :y 2}
  {:x 4 :y 2}
  {:x 5 :y 3}...]"
  
  )

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
  (let [ref-str  (str ref-number)
        test-str (str number-to-find)]
    (.contains ref-str test-str)))
