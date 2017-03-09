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
  (let [max-num   (apply max numbers)
        domain    (range 0 max-num 1)
        empty-map (atom {})]
    (doseq [x domain]
      (swap! empty-map assoc x 0))
    ;;TODO for every number in numbers, increment values for all keys smaller than number by 1
    )
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

(defn filter-fun-numbers [seq]
  (filter
    (fn [item]
      (number-contains-number?
        item
        (number->sum-of-digits item)))
    seq))
