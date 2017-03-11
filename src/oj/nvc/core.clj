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

(defn- causal-attribution-rephrasing-recommender [tokens-w-pos]
  (let [verbs-only (nlpf/verbs tokens-w-pos)
        w-distance (remove nil?
                           (map
                             (fn [[verb pos]]
                               (let [rec (causal-attribution->primary-feelings-and-underlying-needs
                                           verb
                                           :max-distance 0.5)]
                                 (if rec
                                   {:verb    verb
                                    :helpers rec})))
                             verbs-only))
        recs       (map (fn [{:keys [verb helpers]}]
                          {:recommendation (str "You said: "
                                                verb
                                                ".  Do you feel? "
                                                (get-in helpers [:values :primary-feelings])
                                                ". Do you need? "
                                                (get-in helpers [:values :underlying-needs]))
                           :verb           verb
                           :helpers        helpers})
                        w-distance)]
    recs))

(defn- feeling-word-means-needs-met-or-not [feeling-word]
  (let [threshold 0.3
        stuff (map (fn [the-map]

                     (let [not-met-map            (:needs-not-met the-map)
                           are-met-map            (:needs-are-met the-map)
                           not-key                (:summary not-met-map)
                           are-key                (:summary are-met-map)
                           not-vals               (:synonyms not-met-map)
                           are-vals               (:synonyms are-met-map)
                           summary-not-distance   (get-distance not-key feeling-word)
                           summary-are-distance   (get-distance are-key feeling-word)
                           synonyms-not-distances (map (fn [not-val]
                                                         {:val      not-val
                                                          :distance (get-distance not-val feeling-word)}) not-vals)
                           synonyms-are-distances (map (fn [are-val]
                                                         {:val      are-val
                                                          :distance (get-distance are-val feeling-word)}) are-vals)
                           ]

                       (if (<= summary-not-distance threshold)
                         (println "NOT summary: " not-key " word: " feeling-word))
                       (if (<= summary-are-distance threshold)
                         (println "ARE summary: " are-key " word: " feeling-word))

                       (doseq [syn-not synonyms-not-distances]
                         (if (<= (:distance syn-not) threshold)
                           (println "NOT syn: " syn-not " word: " feeling-word)))

                       (doseq [syn-are synonyms-are-distances]
                         (if (<= (:distance syn-are) threshold)
                           (println "ARE syn: " syn-are " word: " feeling-word)))
                       )

                     ) dictionary/feeling-words)]
    (do (println stuff))))

(defn- needs-not-being-met-recommender [tokens-w-pos]
  (let [words         (filter (fn [[t pos]] (< 1 (.length t))) tokens-w-pos)
        feeling-words (map (fn [[word pos]]
                             {:word            word
                              :result-of-stuff (feeling-word-means-needs-met-or-not word)}
                             ) words)
        ]
    feeling-words
    ))


(defn sentence->nvc [sentence]
  (let [parts-of-speech          (pos-tag (tokenize sentence))
        causal-recs              (causal-attribution-rephrasing-recommender parts-of-speech)
        needs-not-being-met-recs (needs-not-being-met-recommender parts-of-speech)]
    ;(pp/pprint parts-of-speech)
    ;(pp/pprint verbs-only)
    ;(pp/pprint with-distance-to-causal-attributions)
    ;(pp/pprint causal-recs)
    ;(pp/pprint needs-not-being-met-recs)

    {:causal-attr-recs   causal-recs
     :needs-not-met-recs needs-not-being-met-recs
     :sentence           sentence}))

(defn text->nvc [text]
  (let [sentences (get-sentences text)]
    (map sentence->nvc sentences)))



