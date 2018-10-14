(ns oj.numberfun
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [oj.plots :as plots]
            [taoensso.nippy :as nippy]
            [clojure.set :as set]
            [clojure.java.io :as io])
  (:import (org.apache.commons.io IOUtils)
           (java.io DataOutputStream DataInputStream)))


(def persisten-cache-init*
  (atom
    {:anywhere   {:domains #{}
                  :numbers #{}}
     :big-end    {:domains #{}
                  :numbers #{}}
     :little-end {:domains #{}
                  :numbers #{}}}))


(defn merge-domains [domain1 domain2]
  (let [s1 (first domain1)
        s2 (first domain2)
        e1 (second domain1)
        e2 (second domain2)]
    (if (> e1 s2)
      (let [s3 (if (> s1 s2)
                 s2
                 s1)
            e3 (if (< e1 e2)
                 e2
                 e1)]
        [[s3 e3]])
      [domain1 domain2])))

(defn consolidate-domains [domain]
  "
  #{[0 5] [2 3] [10 20] [18 21]} ->
  #{[0 5] [10 21]}

  do this by mapping each vec to a range,
  concat the ranges together,
  then scan through the single vec and cut at
  the discontinuities (difference between
  subsequent elements > 1

  ---->>> or sort by start element:
  [ [0 5] [2 7] [10 15] ]
  and then start with first one
  and see if next can collapse,
  if so, do it, if not, accumulate
  "
  (let [domain*          (atom [])
        ordered-by-start (vec (sort-by first (vec domain)))]
    (doseq [i (range (count domain))]
      (let [dom (get ordered-by-start i)]
        (if (= 0 i)
          (swap! domain* conj dom)
          (let [domains-so-far @domain*
                last-dom       (last domains-so-far)
                buttlast       (butlast domains-so-far)
                last-merged    (merge-domains last-dom dom)]
            (reset! domain* buttlast)
            (doseq [domain-to-put last-merged]
              (swap! domain* conj domain-to-put))
            (reset! domain* (set @domain*))))))
    @domain*))

(defn merge-atoms
  [one* two*]
  "Atoms look like:
    (atom
      {:anywhere   {:domains #{[a b] [c d] ...}
                    :numbers #{n1 n2 ...}}..."
  (let [new-atom*    (atom {})
        one-types    (set (keys @one*))
        two-types    (set (keys @two*))
        ;;types common to both sets
        common-types (set/intersection one-types two-types)
        ;;types which are not in common
        unique-types (set/difference (set/union one-types two-types) (set/intersection one-types two-types))]
    (reset! new-atom* (merge @one* @two*))
    (doseq [the-common-type common-types]
      (let [one-domains    (-> @one*
                               the-common-type
                               :domains)
            two-domains    (-> @two*
                               the-common-type
                               :domains)
            one-numbers    (-> @one*
                               the-common-type
                               :numbers)
            two-numbers    (-> @two*
                               the-common-type
                               :numbers)
            merged-domains (consolidate-domains (set/union one-domains two-domains))
            merged-numbers (set/union one-numbers two-numbers)]
        (swap! new-atom* assoc the-common-type {:domains merged-domains
                                                :numbers merged-numbers})))
    new-atom*))

(def numbers-cache* (atom {:anywhere   {:domains #{}
                                        :numbers #{}}
                           :big-end    {:domains #{}
                                        :numbers #{}}
                           :little-end {:domains #{}
                                        :numbers #{}}}))

(defn to-file [file-path some-data]
  (with-open [w (io/output-stream file-path)]
    (nippy/freeze-to-out! (DataOutputStream. w) some-data)))

(defn from-file [file-path]
  (with-open [r (io/input-stream file-path)]
    (nippy/thaw-from-in! (DataInputStream. r))))

(reset! numbers-cache* (try (do (from-file "nippy-cache.bin"))
                            (catch Throwable t
                              (println "Nippy file not found: " t)
                              @numbers-cache*)))

(println "******* Loaded numbers cache with keys: " (keys @numbers-cache*) " *******")

(doseq [the-key (keys @numbers-cache*)]
  (println "*** For key, domains & numbers count: "
           the-key
           " , "
           (get-in @numbers-cache* [the-key :domains])
           " , "
           (count (get-in @numbers-cache* [the-key :numbers]))))

(defn numbers->indexed-numbers [numbers start end]

  (zipmap (range 0 (count numbers)) (sort numbers))
  )

(defn numbers->cumulative-truth-count [numbers start end]
  "For a given input set of numbers,
  #{.. 3 1 5 ...} return the cumulative count
  of numbers in the vector like so:
  {0 0, 1 1, 2 1, 3 2, 4 2, 5 3}"
  (println "Getting cumulative count...")
  (let [domain  (range start (+ 1 end) 1)
        acc-map (atom {})]
    (println "setting up atom...")
    (doseq [x domain]
      (swap! acc-map assoc x 0))
    (println "iterating through numbers...")
    (doseq [number numbers]
      (print ".")
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

(defn compute-fun-numbers [{:keys [start end]} & {:keys [position] :or {position :anywhere}}]
  (println "getting fun numbers between " start end position)
  (let [domain    (range start end)
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


;;TODO make a cache struct be a single map, and have multiple caches for different fun numbers
;;

(defn get-fun-numbers [{:keys [start end]} &
                       {:keys [position cache-file]
                        :or   {position :anywhere cache-file "nippy-cache.bin"}}]
  (let [domains             (-> @numbers-cache*
                                position
                                :domains)
        domains-filtered    (->> domains
                                 (filter #(and (<= end (second %))
                                               (>= start (first %)))))
        domain-intersection (->> domains
                                 (filter #(or (<= (first %) end (second %))
                                              (<= (first %) start (second %)))))
        ]
    (println "covering domains: " domains-filtered)
    (println "domain intersection: " domain-intersection)
    ;;TODO fetch intersecting domains from cache instead of computing all from scratch each time

    (if (empty? domains-filtered)
      (do
        (do (println "cache miss")
            (let [computed (compute-fun-numbers {:start    start
                                                 :end      end
                                                 :position position})
                  entry*   (atom {position {:numbers (set computed)
                                            :domains #{[start end]}}})
                  merged*  (merge-atoms entry* numbers-cache*)]
              (reset! numbers-cache* @merged*)
              (to-file cache-file @numbers-cache*)
              (set computed)))
        #_(if (empty? domain-intersection)

            #_(do (println "partial cache hit")
                  ;; eg start = 5 end = 10 and domains are [[4 6] [9 11]]
                  (let [starts (filter #(>= start (first %)) domain-intersection)
                        ends   (filter #(<= end (first %)) domain-intersection)])))
        )
      (do (println "cache hit")
          (let [numbers          (-> @numbers-cache*
                                     position
                                     :numbers)
                numbers-filtered (->> numbers
                                      (filter #(<= start %))
                                      (filter #(>= end %)))]
            numbers-filtered)))))

(defn have-basic-fun [start end & {:keys [plotfile] :or
                                         {plotfile (format "fun-numbers-indexed-plot-%s.svg"
                                                           (str (System/currentTimeMillis)))}}]
  (println "basic fun")
  (let [config         {:start start
                        :end   end}
        fun-anywhere   (-> config
                           get-fun-numbers
                           (numbers->indexed-numbers start end)
                           mapxy->vecsxy)
        fun-big-end    (-> config
                           (get-fun-numbers :position :big-end)
                           (numbers->indexed-numbers start end)
                           mapxy->vecsxy)
        fun-little-end (-> config
                           (get-fun-numbers :position :little-end)
                           (numbers->indexed-numbers start end)
                           mapxy->vecsxy)]
    (plots/plot-cumulative-fun-numbers {:anywhere   fun-anywhere
                                        :big-end    fun-big-end
                                        :little-end fun-little-end
                                        :start      0
                                        :end        (count fun-anywhere)
                                        :plotfile   plotfile}))


  )

(defn have-cumulative-fun [start end & {:keys [plotfile] :or
                                              {plotfile (format "fun-numbers-cumulative-plot-%s.svg"
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
    (plots/plot-cumulative-fun-numbers {:anywhere   fun-anywhere
                                        :big-end    fun-big-end
                                        :little-end fun-little-end
                                        :start      start
                                        :end        end
                                        :plotfile   plotfile})))