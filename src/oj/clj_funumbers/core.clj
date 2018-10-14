(ns oj.clj-funumbers.core
  (:require [oj.clj-funumbers.viz :as v]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))



(def the-dims-end 25)
(def sym-dims (range 5 the-dims-end))
(def the-range-start 10)
(def the-range-end 500000)


#_(defn symmetrical-nums []
    (->>
      (pmap
        (fn [n]
          (let [symms* (atom [])]
            (doseq [r sym-dims]
              (let [num (Integer/toString n r)]
                (when (= (seq num) (reverse (seq num)))
                  (swap! symms* conj num))))
            (when (<
                    (int (/ (count sym-dims) 2))
                    (count (set @symms*)))
              (swap! sym-nums* assoc n (set @symms*)))))
        (range 10 10000000))
      doall
      time))

(defn sum-containing-nums []
  (let [sym-nums* (atom {})]
    (->>
      (pmap
        (fn [n]
          (when (= 0 (mod n (int (/ the-range-end 20.0))))
            (println "1/20th n: " n))
          (let [symms* (atom [])]
            (doseq [r sym-dims]
              (let [num           (Integer/toString n r)
                    num-sum       (Integer/toString
                                    (reduce +
                                            (map
                                              #(Integer/valueOf
                                                 (str %)
                                                 r)
                                              num))
                                    r)
                    contains-sum? (clojure.string/includes?
                                    num
                                    num-sum)]
                (when contains-sum?
                  (swap! symms* conj {
                                      ;:num     num
                                      ;:num-sum num-sum
                                      :r r
                                      :n n}))))
            (if (<
                  (int (/ (count sym-dims) 5))
                  (count @symms*))
              ;TODO exp w storing all of them...
              (swap! sym-nums* assoc n @symms*)
              (swap! sym-nums* assoc n @symms*))))
        (range the-range-start the-range-end))
      doall
      time)
    @sym-nums*))

(defn sym-map->plottable [sym-map]

  (->>
    sym-map
    vals
    (map
      (fn [v]
        (map
          (fn [{:keys [r n] :as data}]
            [n r])
          v)))
    (mapcat identity)))

(v/doit
  (-> (sum-containing-nums)
      (sym-map->plottable))
  [1 (inc the-range-end)]
  [2 (inc the-dims-end)]
  "funumbers.svg")
