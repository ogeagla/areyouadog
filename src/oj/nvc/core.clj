(ns oj.nvc.core
  (:require [clj-fuzzy.metrics :as fuzzy]
            [oj.nvc.dictionary :as dictionary]
            [opennlp.nlp :as nlp]
            [opennlp.tools.filters :as nlpf]
    ;[opennlp.treebank :as nlptb]
            [clojure.pprint :as pp]
            [clojure.set :as set]))

(defn- get-distance [one two & {:keys [algo] :or {algo :jw}}]
  "returns distance between strings,
  0.0 meaning identical,
  1.0 meaning completely different"
  (let [jacc (fn [one two] (fuzzy/jaccard (str one) (str two)))
        jw   (fn [one two] (- 1.0 (fuzzy/jaro-winkler (str one) (str two))))]
    (case algo
      :jw (jw one two)
      :jacc (jacc one two)
      (jw one two))))

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
    (println "provided feeling: " target-primary-feeling)
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
                                           :max-distance 0.25)]
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

(defn- feeling-word-map->recommendations [{:keys [needs-not-met needs-are-met]} feeling-word threshold]
  (let [not-met-summary-word       (:summary needs-not-met)
        are-met-summary-word       (:summary needs-are-met)
        not-met-synonyms           (:synonyms needs-not-met)
        are-met-synonyms           (:synonyms needs-are-met)
        summary-not-distance       (get-distance not-met-summary-word feeling-word)
        summary-are-distance       (get-distance are-met-summary-word feeling-word)
        not-met-synonyms-distances (->> not-met-synonyms
                                        (map (fn [not-val]
                                               {:val      not-val
                                                :distance (get-distance not-val feeling-word)}))
                                        (filter (fn [{:keys [val distance]}]
                                                  (<= distance threshold))))
        are-met-synonyms-distances (->> are-met-synonyms
                                        (map (fn [are-val]
                                               {:val      are-val
                                                :distance (get-distance are-val feeling-word)}))
                                        (filter (fn [{:keys [val distance]}]
                                                  (<= distance threshold))))
        not-rec                    (if (or (not-empty not-met-synonyms-distances)
                                           (<= summary-not-distance threshold))
                                     {:word             feeling-word
                                      :not-met-summary  not-met-summary-word
                                      :not-met-synonyms not-met-synonyms
                                      :recommendation   (str "You said: "
                                                             feeling-word
                                                             ". Are you trying to say your need to feel "
                                                             are-met-summary-word
                                                             " is NOT being met? Do these other words describe your needs better? "
                                                             are-met-synonyms
                                                             " Do these other words better describe how you are feeling? "
                                                             not-met-synonyms)})
        are-rec                    (if (or (not-empty are-met-synonyms-distances)
                                           (<= summary-are-distance threshold))
                                     {:word             feeling-word
                                      :are-met-summary  are-met-summary-word
                                      :are-met-synonyms are-met-synonyms
                                      :recommendation   (str "You said: "
                                                             feeling-word
                                                             ". Are you trying to say your need to feel "
                                                             are-met-summary-word
                                                             " is being met? Or do these other words describe it better? "
                                                             are-met-synonyms)})]


    ;(into {} (filter (comp some? val) {:are-met-rec are-rec
    ;                                   :not-met-rec not-rec}))
    (merge not-rec are-rec)
    ))



(defn- feeling-word-means-needs-met-or-not [feeling-word]
  (let [threshold 0.15
        stuff     (remove nil?
                          (map (fn [the-map]
                                 (let [recs-map         (feeling-word-map->recommendations the-map feeling-word threshold)
                                       recs-map-cleaned (if (or (contains? recs-map :are-met-summary)
                                                                (contains? recs-map :not-met-summary))
                                                          recs-map)]
                                   recs-map-cleaned))
                               dictionary/feeling-words))]
    stuff))

(defn- needs-not-being-met-recommender [tokens-w-pos]
  (let [words         (filter (fn [[t pos]] (< 1 (.length t))) tokens-w-pos)
        feeling-words (flatten (remove nil? (map (fn [[word pos]]
                                                   (let [recs (feeling-word-means-needs-met-or-not word)]
                                                     (if-not (empty? recs)
                                                       {:word            word
                                                        :recommendations recs})))
                                                 words)))]
    feeling-words))

(defn sentence->nvc [sentence]
  (let [parts-of-speech          (pos-tag (tokenize sentence))
        causal-recs              (causal-attribution-rephrasing-recommender parts-of-speech)
        needs-not-being-met-recs (needs-not-being-met-recommender parts-of-speech)
        recs-document            {:causal-attr-recs causal-recs
                                  :needs-recs       needs-not-being-met-recs
                                  :sentence         sentence}
        just-caus-recs           (map :recommendation causal-recs)
        just-needs-recs          (->> needs-not-being-met-recs
                                      (map :recommendations)
                                      flatten
                                      (map :recommendation))
        ]
    (println "BEGIN SENTENCE RECOMMENDATIONS ***************************************************")
    (pp/pprint just-caus-recs)
    (pp/pprint just-needs-recs)
    (println "END SENTENCE RECOMMENDATIONS ***************************************************")
    ;(println "BEGIN SENTENCE RECOMMENDATIONS DETAILS ***************************************************")
    ;(pp/pprint recs-document)
    ;(println "END SENTENCE RECOMMENDATIONS DETAILS ***************************************************")
    recs-document))

(defn text->nvc [text]
  (let [sentences (get-sentences text)
        analysis  (map sentence->nvc sentences)]
    analysis))

(def i-pronouns
  [:i :i'm :im])

(def you-pronouns
  [:you :she :he :they :yall :y'all])

(def sentence-heuristics
  ;TODO max-gap is the gap between the words to still be considered touching
  ;TODO order matters right?
  ;TODO 'e' off ends of words so can use contains?
  {
   ::order       {:max-gap   4
                  :min-match 2
                  :words     {0 you-pronouns
                              1 [:need :should :ought :might :better :want :expect :wish]}}

   ::need        {:max-gap   2
                  :min-match 2
                  :words     {0 i-pronouns
                              1 [:need :want :expect :wish :desir :crav]}}

   ::feeling     {:max-gap   2
                  :min-match 2
                  :words     {0 i-pronouns
                              1 [:feel]}}

   ::observation {:max-gap   3
                  :min-match 2
                  :words     {0 [:when]
                              1 you-pronouns}}

   ::request     {:max-gap   2
                  :min-match 2
                  :words     {0 [:would :can :will :may]
                              1 you-pronouns}}
   })

(defn sanitze-word [word]
  (clojure.string/lower-case (name word)))

(defn words-match [word1 word2]
  (let [contains1 (.contains word1 word2)
        contains2 (.contains word2 word1)
        distance  (get-distance word1 word2)]
    ;(println "match test: " word1 word2 distance contains1 contains2)
    (or
      (<= distance 0.075)
      (and (or contains1 contains2)
           (<= distance 0.25)))))

(defn indexes-present-with-max-gap [vec-of-maps max-gap min-match]
  "for input: ({:s-i 0}{:s-i 1} nil {:s-i 3} nil ...)
  if a min number of indexes are in order within gap
  tolerance, return true; else floss"

  (let [matches*      (atom 0)
        matched-maps* (atom #{})
        stride        (+ (* max-gap min-match) (- min-match max-gap))
        map-w-uuids   (vec (map #(assoc % :id (java.util.UUID/randomUUID)) vec-of-maps))
        strides       (max 1 (+ 1 (- (count map-w-uuids) stride)))
        ;_             (println "map w uuids: " map-w-uuids)
        _ (println "max gap, min match, stride: " max-gap " , " min-match " , " stride)
        results       (doall (map
                               (fn [stride-start]
                                 ;(println "stride start index: " (+ 1 stride-start) " of " strides)
                                 (let [sub-list     (subvec map-w-uuids stride-start (min (count map-w-uuids) (+ stride stride-start)))
                                       wo-nils      (vec (keep #(if (contains? % :sentence-index) %) sub-list))
                                       sent-indexes (map :sentence-index wo-nils)]
                                   ;(println "wo niles indexes: " (map :sentence-index wo-nils))
                                   (if (and (not (empty? sent-indexes)) (apply <= sent-indexes))
                                     (do
                                       (let [matches-set        (set wo-nils)
                                             uniq-matches       (set/difference matches-set @matched-maps*)
                                             uniq-matches-count (count uniq-matches)]
                                         ;(println "uniq-matches-count: " uniq-matches-count)
                                         (swap! matched-maps* #(set (concat % uniq-matches)))
                                         (swap! matches* #(+ % uniq-matches-count))
                                         (println "matches so far: " @matches*)
                                         ))
                                     (do
                                       ;(println "no match: " stride-start)
                                       ))))
                               (range strides)))]
    (println "matches total, min, matches>=min: " @matches* min-match (>= @matches* min-match))
    {:does-match? (>= @matches* min-match)
     :results     @matched-maps*
     :matches     @matches*}))


(defn classify-sentence-using-heuristics [sentence]
  (let [tokens (tokenize sentence)]

    (println "the tokens: " tokens)

    (map
      (fn [[class bank-map]]
        ;for each class in the heuristics map...
        (let [sentence-matched (map
                                 (fn [token]
                                   ;for each token in the sentence...
                                   (let [best-match-for-token
                                         (first
                                           (sort-by
                                             :distance
                                             (flatten
                                               (remove #(or (nil? %) (empty? %))
                                                       (map
                                                         (fn [[sentence-index word-bank]]
                                                           (remove nil?
                                                                   (map
                                                                     (fn [word]
                                                                       ;for each word in the word bank for this sentence class
                                                                       (let [word-sanitas  (sanitze-word word)
                                                                             token-sanitas (sanitze-word token)]
                                                                         (if (words-match token-sanitas word-sanitas)
                                                                           {:word           word
                                                                            :token          token
                                                                            :distance       (get-distance token-sanitas word-sanitas)
                                                                            :sentence-index sentence-index})))
                                                                     word-bank)))
                                                         (:words bank-map))))))]
                                     ;(println "class, token, best match in bank: " class " , " token " , " best-match-for-token)
                                     best-match-for-token))
                                 tokens)]
          (let [is-class (indexes-present-with-max-gap (vec sentence-matched) (:max-gap bank-map) (:min-match bank-map))]
            (println "sentence data: " is-class)
            (println
              (if (:does-match? is-class)
                (str "SENTENCE IS CLASS: " class)
                (str "sentence is not class: " class))))
          sentence-matched)
        )
      sentence-heuristics)))

(def nvc-helper-tree

  {

   :ingest-text           {:actions [:provide-nvc-recommendations
                                     :needs-are-met-classification ; TODO this can a binary classifier
                                     :is-nvc-classification ; TODO this can a binary classifier
                                     ]}
   :four-questions        {:questions
                           [{:prompt       "What do you observe?"
                             :response-key :observations}
                            {:prompt       "How does that make you feel?"
                             :response-key :feelings}
                            {:prompt       "What needs/values/desires create those feelings?"
                             :response-key :needs}
                            {:prompt       "What concrete actions would you like to request?"
                             :response-key :requests}]}

   :augment-4qs-responses {:observations ()
                           :feelings     ()
                           :needs        ()
                           :requests     ()}

   })

(defn nvc-helper [])
