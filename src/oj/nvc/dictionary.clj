(ns oj.nvc.dictionary)

(def causal-attributions
  {
   :attacked     {:primary-feelings [:scared :angry]
                  :underlying-needs [:safety :respect]}

   :belittled    {:primary-feelings [:indignant :distressed :tense :embarrassed :outraged]
                  :underlying-needs [:respect :autonomy :to-be-seen :acknowledgement :appreciation]}

   :blamed       {:primary-feelings [:angry :scared :antagonistic :bewildered :hurt]
                  :underlying-needs [:fairness :justice :understanding]}

   :betrayed     {:primary-feelings [:stunned :outraged :hurt :disappointed]
                  :underlying-needs [:trust :dependability :honesty :commitment]}

   :boxed-in     {:primary-feelings [:frustrated :scared :anxious]
                  :underlying-needs [:autonomy :choice :freedom :self-efficacy]}

   :coerced      {:primary-feelings [:angry :frustrated :scared :anxious]
                  :underlying-needs [:autonomy :choice :freedom :self-efficacy]}

   :criticized   {:primary-feelings [:humiliated :irritate :scared :anxious :embarrassed]
                  :underlying-needs [:understanding :acknowledgement :recognition]}

   :disrespected {:primary-feelings [:furious :hurt :embarrassed :frustrated]
                  :underlying-needs [:respect :trust :acknowledgement]}

   :harassed     {:primary-feelings [:angry :aggravated :pressured :frightened :exasperated]
                  :underlying-needs [:respect :consideration :ease]}

   :hassled      {:primary-feelings [:irritated :irked :distressed :frustrated]
                  :underlying-needs [:autonomy :ease :calm :space]}

   :insulted     {:primary-feelings [:angry :embarrassed :incensed]
                  :underlying-needs [:respect :consideration :acknowledgement :recognition]}

   :interrupted  {:primary-feelings [:irritated :hurt :resentful]
                  :underlying-needs [:respect :consideration :acknowledgement :recognition]}


   ;: {:primary-feelings []
   ;   :underlying-needs []}

   })
