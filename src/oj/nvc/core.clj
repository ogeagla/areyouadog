(ns oj.nvc.core
  (:require [clj-fuzzy.metrics :as fuzzy]
            [oj.nvc.dictionary :as dictionary]
            [opennlp.nlp :as nlp]
            [opennlp.tools.filters :as nlpf]
    ;[opennlp.treebank :as nlptb]
            [clojure.pprint :as pp]
            ))

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

(defn causal-attribution->primary-feelings-and-underlying-needs [target-attribute & {:keys [max-distance] :or {max-distance 1.0}}]
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
        (if (<= distance max-distance)
          {:values            matched-val
           :target-attribute  target-attribute
           :matched-attribute matched-key})))))

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

(def get-sentences (nlp/make-sentence-detector (clojure.java.io/resource "nlp/en-sent.bin")))
(def tokenize (nlp/make-tokenizer (clojure.java.io/resource "nlp/en-token.bin")))
(def pos-tag (nlp/make-pos-tagger (clojure.java.io/resource "nlp/en-pos-maxent.bin")))
(def name-find (nlp/make-name-finder (clojure.java.io/resource "nlp/en-ner-person.bin")))
;(def chunker (nlptb/make-treebank-chunker (clojure.java.io/resource "nlp/en-chunker.bin")))

(defn sentence->nvc [sentence]
  (let [parts-of-speech                      (pos-tag (tokenize sentence))
        verbs-only                           (nlpf/verbs parts-of-speech)
        with-distance-to-causal-attributions (remove nil?
                                                     (map
                                                       (fn [[verb pos]]
                                                         (let [rec (causal-attribution->primary-feelings-and-underlying-needs
                                                                     verb
                                                                     :max-distance 0.5)]
                                                           (if rec
                                                             {:verb    verb
                                                              :helpers rec})))
                                                       verbs-only))

        recommendation-sentences             (map (fn [{:keys [verb helpers]}]
                                                    {:recommendation (str "You said: "
                                                                          verb
                                                                          ".  Do any of the following words describe your feelings better? "
                                                                          (get-in helpers [:values :primary-feelings])
                                                                          ". Do any of the following words describe your needs better? "
                                                                          (get-in helpers [:values :underlying-needs]))
                                                     :verb           verb
                                                     :helpers        helpers})
                                                  with-distance-to-causal-attributions)]
    ;(pp/pprint parts-of-speech)
    ;(pp/pprint verbs-only)
    ;(pp/pprint with-distance-to-causal-attributions)
    (pp/pprint recommendation-sentences)

    recommendation-sentences))

(defn text->nvc [text]
  (let [sentences (get-sentences text)]
    (map sentence->nvc sentences)))



