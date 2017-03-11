(ns oj.nvc.dictionary)

(def feeling-words
  [{:needs-not-met {:summary :hostile :synonyms [:animosity :antagonistic :appalled :aversion :cold :contempt :disguisted :dislike :distain :hate :horrified :repulsed :scorn :surly :vengeful :vindictive]}
    :needs-are-met {:summary :exhilarated :synonyms [:ecstatic :elated :enthralled :exuberant :giddy :silly :slap-happy]}}

   {:needs-not-met {:summary :angry :synonyms [:enraged :furious :incensed :indignant :irate :livid :mad :outraged :resentful :ticked-off]}
    :needs-are-met {:summary :excited :synonyms [:alive :amazed :animated :eager :energetic :enthusiastic :invigorated :lively :passionate]}}

   ;{:needs-not-met {:# []}
   ; :needs-are-met {:# []}}
   ])

(def causal-attributions
  {:attacked           {:primary-feelings [:scared :angry]
                        :underlying-needs [:safety :respect]}

   :belittled          {:primary-feelings [:indignant :distressed :tense :embarrassed :outraged]
                        :underlying-needs [:respect :autonomy :to-be-seen :acknowledgement :appreciation]}

   :blamed             {:primary-feelings [:angry :scared :antagonistic :bewildered :hurt]
                        :underlying-needs [:fairness :justice :understanding]}

   :betrayed           {:primary-feelings [:stunned :outraged :hurt :disappointed]
                        :underlying-needs [:trust :dependability :honesty :commitment]}

   :boxed-in           {:primary-feelings [:frustrated :scared :anxious]
                        :underlying-needs [:autonomy :choice :freedom :self-efficacy]}

   :coerced            {:primary-feelings [:angry :frustrated :scared :anxious]
                        :underlying-needs [:autonomy :choice :freedom :self-efficacy]}

   :criticized         {:primary-feelings [:humiliated :irritate :scared :anxious :embarrassed]
                        :underlying-needs [:understanding :acknowledgement :recognition]}

   :disrespected       {:primary-feelings [:furious :hurt :embarrassed :frustrated]
                        :underlying-needs [:respect :trust :acknowledgement]}

   :harassed           {:primary-feelings [:angry :aggravated :pressured :frightened :exasperated]
                        :underlying-needs [:respect :consideration :ease]}

   :hassled            {:primary-feelings [:irritated :irked :distressed :frustrated]
                        :underlying-needs [:autonomy :ease :calm :space]}

   :insulted           {:primary-feelings [:angry :embarrassed :incensed]
                        :underlying-needs [:respect :consideration :acknowledgement :recognition]}

   :interrupted        {:primary-feelings [:irritated :hurt :resentful]
                        :underlying-needs [:respect :consideration :acknowledgement :recognition]}

   :intimidated        {:primary-feelings [:frightened :scared :vulnerable]
                        :underlying-needs [:safety :power :self-efficacy :independence]}

   :left-out           {:primary-feelings [:sad :lonely :anxious]
                        :underlying-needs [:belonging :community :connection :to-be-seen]}

   :manipulated        {:primary-feelings [:resentful :vulnerable :sad :angry]
                        :underlying-needs [:autonomy :consideration :choice :power]}

   :misunderstood      {:primary-feelings [:upset :dismayed :frustrated]
                        :underlying-needs [:understanding :to-be-heard :clarity]}

   :overworked         {:primary-feelings [:angry :tired :frustrated :resentful]
                        :underlying-needs [:respect :consideration :rest :caring :ease]}

   :pressured          {:primary-feelings [:overwhelmed :anxious :resentful]
                        :underlying-needs [:relaxation :ease :clarity :space :consideration]}

   :rejected           {:primary-feelings [:hurt :scared :angry :defiant]
                        :underlying-needs [:belonging :conection :acknowledgement]}

   :taken-advantage-of {:primary-feelings [:angry :powerless :frustrated]
                        :underlying-needs [:autonomy :power :trust :choice :connection :acknowledgement]}

   :taken-for-granted  {:primary-feelings [:hurt :disappointed :angry]
                        :underlying-needs [:appreciation :acknowledgement :recognition :consideration]}

   :tricked            {:primary-feelings [:indignant :embarrassed :furious]
                        :underlying-needs [:integrity :honesty :trust]}

   :unappreciated      {:primary-feelings [:sad :hurt :frustrated :irritated]
                        :underlying-needs [:appreciation :respect :acknowledgement]}

   :unsupported        {:primary-feelings [:sad :hurt :resentful]
                        :underlying-needs [:support :understanding]}

   :violated           {:primary-feelings [:outraged :agitated :anxious :sad]
                        :underlying-needs [:safety :trust :space :respect]}})

(def all-primary-feelings
  (->> causal-attributions
       vals
       (map :primary-feelings)
       flatten
       (apply hash-set)))

(def all-underlying-needs
  (->> causal-attributions
       vals
       (map :underlying-needs)
       flatten
       (apply hash-set)))

(def all-causal-words
  (->> causal-attributions
       keys))