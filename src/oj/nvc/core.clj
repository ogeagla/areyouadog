(ns oj.nvc.core
  (:require [clj-fuzzy.metrics :as fuzzy]
            [oj.nvc.dictionary :as dictionary]))

(defn- get-distance [one two]
  "returns distance between strings,
  0.0 meaning identical,
  1.0 meaning completely different"
  (fuzzy/jaccard (str one) (str two)))

(defn- get-closest-key-and-distance [target]
  "for a given word, returns the key of
   causal attribution closest in distance
   and the distance score to the target word"
  (let [the-keys     (keys dictionary/causal-attributions)
        keys-w-score (map
                       (fn [the-key]
                         (hash-map :matched-key the-key
                                   :distance (get-distance target the-key)))
                       the-keys)
        best         (first (sort-by :distance keys-w-score))]
    best))

(defn causal-attribution->primary-feelings-and-underlying-needs [target-attribute]
  "for a given causal attribution, get the value from the map
  or if the word is not in the map, search for the key
  which is closest in word distance to the target attribute
  and return the values for that"
  (let [target-kw    (keyword target-attribute)
        it-contains? (contains? dictionary/causal-attributions target-kw)]
    (if it-contains?
      {:values            (get dictionary/causal-attributions target-kw)
       :target-attribute  target-attribute
       :matched-attribute target-kw}
      (let [{:keys [matched-key distance]} (get-closest-key-and-distance target-attribute)
            matched-val (get dictionary/causal-attributions matched-key)]
        {:values            matched-val
         :target-attribute  target-attribute
         :matched-attribute matched-key}))))

(defn primary-feeling->underlying-needs [target-primary-feeling]
  "for a given primary feeling, get the underlying needs
  which may be responsible. fuzzy matches on the feeling
  and returns the possible causal attribution by which
  the underlying needs was matched"
  (let [feelings-w-match-dist (map (fn [feeling]
                                     {:primary-feeling feeling
                                      :distance        (get-distance feeling target-primary-feeling)})
                                   dictionary/all-primary-feelings)
        best                  (first (sort-by :distance feelings-w-match-dist))
        best-match-feeling    (:primary-feeling best)
        needs                 (->> dictionary/causal-attributions
                                   (map (fn [[k v]]
                                          (if (some #{best-match-feeling} (:primary-feelings v))
                                            (:underlying-needs v))))
                                   (remove nil?)
                                   flatten
                                   (apply hash-set))]
    (println "provided feeling: ")
    (println "best matched feeling: " best-match-feeling)
    (println "its corresponding needs: " needs)
    needs))

