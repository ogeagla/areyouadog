(ns oj.nvc.dictionary)

(def feeling-words
  [{:needs-not-met {:summary :hostile :synonyms [:animosity :antagonistic :appalled :aversion :cold :contempt :disguisted :dislike :distain :hate :horrified :repulsed :scorn :surly :vengeful :vindictive]}
    :needs-are-met {:summary :exhilarated :synonyms [:ecstatic :elated :enthralled :exuberant :giddy :silly :slap-happy]}}

   {:needs-not-met {:summary :angry :synonyms [:enraged :furious :incensed :indignant :irate :livid :mad :outraged :resentful :ticked-off]}
    :needs-are-met {:summary :excited :synonyms [:alive :amazed :animated :eager :energetic :enthusiastic :invigorated :lively :passionate]}}

   {:needs-not-met {:summary :annoyed :synonyms [:aggravated :bitter :cranky :cross :dismayed :disgruntled :displeased :exasperated :frustrated :grouchy :impatient :irked :irritated :miffed :peeved :resentful :sullen :uptight]}
    :needs-are-met {:summary :inspired :synonyms [:amazed :astonished :awed :dazzled :radiant :rapturous :surprised :thrilled :uplifted :wonder]}}

   {:needs-not-met {:summary :upset :synonyms [:agitated :alarmed :discombobulated :disconcerted :disturbed :disquieted :perturbed :rattled :restless :troubled :turbulent :turmoil :uncomfortable :uneasy :unnerved :unsettled]}
    :needs-are-met {:summary :joyful :synonyms [:amused :buoyant :delighted :elated :ecstatic :glad :gleeful :happy :jubilant :merry :mirthful :overjoyed :pleased :radiant :tickled]}}

   {:needs-not-met {:summary :tense :synonyms [:antsy :anxious :bitter :distressed :distraught :edgy :fidgety :frazzled :irritable :jittery :nervous :overwhelmed :pressured :restless :stressed-out :uneasy]}
    :needs-are-met {:summary :relaxed :synonyms [:at-east :carefree :comfortable :open]}}

   {:needs-not-met {:summary :afraid :synonyms [:apprehensive :concerned :dread :fearful :foreboding :frightened :hesitant :mistrustful :panicked :petrified :scared :suspicious :terrified :timid :trepidation :unnerved :wary :worried :reserved :sensitive :shaky :unsteady]}
    :needs-are-met {:summary :curious :synonyms [:adventurous :alert :interested :intrigued :inquisitive :fascinated :spellbound :stimulated]}}

   {:needs-not-met {:summary :vulnerable :synonyms [:cautious :fragile :guarded :helpless :insecure :leery :reluctant]}
    :needs-are-met {:summary :confident :synonyms [:empowered :proud :safe :secure :self-assured]}}

   {:needs-not-met {:summary :confused :synonyms [:ambivalent :baffled :bewildered :dazed :flustered :hesitant :lost :mystified :perplexed :puzzled :skeptical :torn]}
    :needs-are-met {:summary :engaged :synonyms [:absorbed :alert :ardent :curious :engrossed :enchanted :entranced :involved]}}

   {:needs-not-met {:summary :embarrassed :synonyms [:ashamed :chagrined :contrite :guilty :disgraced :humiliated :mortified :remorse :regretful :self-conscious]}
    :needs-are-met {:summary :hopeful :synonyms [:expectant :encouraged :optimistic]}}

   {:needs-not-met {:summary :longing :synonyms [:envious :jealous :nostalgic :pining :wistful :yearning]}
    :needs-are-met {:summary :grateful :synonyms [:appreciative :moved :thankful :touched]}}

   {:needs-not-met {:summary :tired :synonyms [:beat :burned-out :depleted :exhausted :fatigued :lethargic :listless :sleepy :weary :worn-out]}
    :needs-are-met {:summary :refreshed :synonyms [:enlivened :rejuvenated :renewed :rested :restored :revived :energetic]}}

   {:needs-not-met {:summary :disconnected :synonyms [:alienated :aloof :apathetic :bored :cold :detached :disengaged :disinterested :distant :distracted :indifferent :lethargic :listless :lonely :numb :removed :uninterested :withdrawn]}
    :needs-are-met {:summary :affectionate :synonyms [:closeness :compassionate :friendly :loving :openhearted :sympathetic :tender :trusting :warm]}}

   {:needs-not-met {:summary :sad :synonyms [:blue :depressed :dejected :despair :despondent :disappointed :discouraged :disheartened :downcast :downhearted :forlorn :gloomy :grief :heavy-hearted :hopeless :melancholy :sorrow :unhappy]}
    :needs-are-met {:summary :peaceful :synonyms [:blissful :calm :centered :clear-headed :mellow :quiet :serene :tranquil]}}

   {:needs-not-met {:summary :shocked :synonyms [:appalled :disbelief :dismay :horrified :mystified :startled :surprised]}
    :needs-are-met {:summary :relieved :synonyms [:complacent :composed :cool :trusting]}}

   {:needs-not-met {:summary :pain :synonyms [:agony :anguished :bereaved :devastated :heartbroken :hurt :miserable :wretched]}
    :needs-are-met {:summary :content :synonyms [:glad :cheerful :fulfilled :satisfied]}}])

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
                        :underlying-needs [:belonging :connection :acknowledgement]}

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