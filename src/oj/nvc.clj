(ns oj.nvc
  (:require [clj-fuzzy.metrics :as fuzzy]))

(def causal-attributions
  {
   :attacked  {:primary-feelings [:scared :angry]
               :underlying-needs [:safety :respect]}

   :belittled {:primary-feelings [:indignant :distressed :tense :embarrassed :outraged]
               :underlying-needs [:respect :autonomy :to-be-seen :acknowledgement :appreciation]}

   :blamed    {:primary-feelings [:angry :scared :antagonistic :bewildered :hurt]
               :underlying-needs [:fairness :justice :understanding]}

   :betrayed  {:primary-feelings [:stunned :outraged :hurt :disappointed]
               :underlying-needs [:trust :dependability :honesty :commitment]}

   :boxed-in  {:primary-feelings [:frustrated :scared :anxious]
               :underlying-needs [:autonomy :choice :freedom :self-efficacy]}


   ;:blamed {:primary-feelings []
   ;         :underlying-needs []}

   })

(defn- get-distance [one two]
  "returns distance between strings,
  0 meaning identical,
  1 meaning completely different"
  (fuzzy/jaccard (str one) (str two)))

(defn- get-closest-key-and-distance [target]
  "for a given word, returns the key of
   causal attribution closest in distance
   and the distance score to the target word"
  (let [the-keys     (keys causal-attributions)
        keys-w-score (map
                       (fn [the-key]
                         (hash-map :key the-key
                                   :distance (get-distance target the-key)))
                       the-keys)
        best         (first (sort-by :distance keys-w-score))]
    best))

(defn causal-attribution->primary-feelings-and-underlying-needs [target-attribute]
  "for a given causal attribution, get the value from the map
  or if the word is not in the map, search for the key
  which is closest in word distance to the target attribute
  and return the values for that"
  (let [val-or-nil (get causal-attributions (keyword target-attribute))]
    (case val-or-nil
      nil (get causal-attributions
               (:key
                 (get-closest-key-and-distance target-attribute)))
      val-or-nil)))

