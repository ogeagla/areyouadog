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

   :coerced {:primary-feelings [:angry :frustrated :scared :anxious]
            :underlying-needs [:autonomy :choice :freedom :self-efficacy]}

   ;: {:primary-feelings []
   ;   :underlying-needs []}
   ;
   ;: {:primary-feelings []
   ;   :underlying-needs []}
   ;
   ;: {:primary-feelings []
   ;   :underlying-needs []}


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
        it-contains? (contains? causal-attributions target-kw)]
    (if it-contains?
      {:values            (get causal-attributions target-kw)
       :target-attribute  target-attribute
       :matched-attribute target-attribute}
      (let [{:keys [matched-key distance]} (get-closest-key-and-distance target-attribute)
            matched-val (get causal-attributions matched-key)]
        {:values            matched-val
         :target-attribute  target-attribute
         :matched-attribute matched-key}))))

(defn primary-feeling->underlying-needs [target-primary-feeling]
  "for a given primary feeling, get the underlying needs
  which may be responsible. fuzzy matches on the feeling
  and returns the possible causal attribution by which
  the underlying needs was matched"
  ())

