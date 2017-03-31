(ns oj.numberfun
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [oj.plots :as plots]
            [taoensso.nippy :as nippy]))

;;TODO memoize with nippy?
(defn numbers->cumulative-truth-count [numbers start end]
  "For a given input set of numbers,
  #{.. 3 1 5 ...} return the cumulative count
  of numbers in the vector like so:
  {0 0, 1 1, 2 1, 3 2, 4 2, 5 3}"
  (let [domain  (range start (+ 1 end) 1)
        acc-map (atom {})]
    (doseq [x domain]
      (swap! acc-map assoc x 0))
    (doseq [number numbers]
      (doseq [x (range number (+ 1 end))]
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
  (number-big-end-is-number? (s/reverse (str ref-number))
                             (s/reverse (str number-to-find))))


(defn get-fun-numbers [{:keys [start end]} & {:keys [position] :or {position :anywhere}}]
  (let [domain       (range start end)
        filter-fn (fn [the-seq comparator]
                    (filter
                      (fn [item]
                        (comparator
                          item
                          (number->sum-of-digits item)))
                      the-seq))]
    (case position
      :big-end (filter-fn domain number-big-end-is-number?)
      :little-end (filter-fn domain number-little-end-is-number?)
      :anywhere (filter-fn domain number-contains-number?)
      (filter-fn domain number-contains-number?))))

(defn have-fun [start end & {:keys [plotfile] :or
                                   {plotfile (format "fun-numbers-plot-%s.svg"
                                                     (str (System/currentTimeMillis)))}}]
  (println "Having fun numbers between: " start " and " end)
  (let [config         {:start start
                        :end   end}
        fun-anywhere   (-> config
                           get-fun-numbers
                           (numbers->cumulative-truth-count start end)
                           mapxy->vecsxy)
        fun-big-end    (-> config
                           (get-fun-numbers :position :big-end)
                           (numbers->cumulative-truth-count start end)
                           mapxy->vecsxy)
        fun-little-end (-> config
                           (get-fun-numbers :position :little-end)
                           (numbers->cumulative-truth-count start end)
                           mapxy->vecsxy)]
    (plots/plot-fun-numbers {:anywhere   fun-anywhere
                             :big-end    fun-big-end
                             :little-end fun-little-end
                             :start      start
                             :end        end
                             :plotfile   plotfile})))