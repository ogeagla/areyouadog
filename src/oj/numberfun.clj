(ns oj.numberfun
  (:require [clojure.test :refer :all]
            [clojure.string :as s]))

(defn numbers->cumulative-truth-count [numbers]
  "For a given input set of numbers,
  #{.. 3 1 5 ...} return the cumulative count
  of numbers in the vector like so:
  {0 0, 1 1, 2 1, 3 2, 4 2, 5 3}"
  (let [max-num (apply max numbers)
        domain  (range 0 (+ 1 max-num) 1)
        acc-map (atom {})]
    (doseq [x domain]
      (swap! acc-map assoc x 0))
    (doseq [number numbers]
      (doseq [x (range number (+ 1 max-num))]
        (swap! acc-map update-in [x] inc)))
    @acc-map))

(defn mapxy->vecsxy [the-map]
  (map (fn [[k v]] [k v]) the-map))

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

(defn number-big-end-is-number? [ref-number number-to-find]
  (let [test-str (str number-to-find)
        big-end  (str (subs (str ref-number) 0 (.length test-str)))]
    (= test-str big-end)))

(defn number-little-end-is-number? [ref-number number-to-find]
  (number-big-end-is-number? (s/reverse (str ref-number)) (s/reverse (str number-to-find))))


(defn filter-fun-numbers [seq & {:keys [position] :or {position :anywhere}}]
  (let [anywhere   (filter
                     (fn [item]
                       (number-contains-number?
                         item
                         (number->sum-of-digits item)))
                     seq)
        big-end    (filter
                     (fn [item]
                       (number-big-end-is-number?
                         item
                         (number->sum-of-digits item)))
                     seq)
        little-end (filter
                     (fn [item]
                       (number-little-end-is-number?
                         item
                         (number->sum-of-digits item)))
                     seq)]
    (case position
      :big-end big-end
      :little-end little-end
      :anywhere anywhere
      anywhere)))
