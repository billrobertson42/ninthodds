(ns ninthodds.core
  (:require [goog.string :as gstr]
            [goog.string.format]
            [ninthodds.input :as input]
            [ninthodds.melee :as melee]
            [ninthodds.mu :as ui]
            [ninthodds.rc :as rc]
            [reagent.core :as reagent :refer [atom]]
            ))

(set! *warn-on-infer* true)
(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defn compute-melee-odds [melee-form]
  (if-let [stats (input/stats melee-form)]
    (melee/tell-me-the-odds! stats)))

(defn base-state []
  (let [initial {:show :melee
                 :melee-form
                 {:att "10" :off "3"  :str "3" :ap  ""
                  :def "3" :res "3"  :armor "" :special ""
                  :reroll-hits :none :reroll-wounds :none
                  :reroll-armor :none :reroll-special :none
                  :poison false}}]
    (assoc initial :melee-odds (compute-melee-odds (:melee-form initial)))))

(defonce app-state
  (atom (base-state)))

(def blue "#336699")
(def dark-blue "#1E3C5B")

(defonce theme
  {:muiTheme
   (ui/getMuiTheme
    (-> ui/lightBaseTheme
        (js->clj :keywordize-keys true)
        (update :palette merge {:primary1Color blue
                                :primary2Color dark-blue})
        clj->js))})

(defn text-field [hint value-acceptor post-action & ks]
  (let [value (str (get-in @app-state ks))]
    [ui/TextField
     {:floatingLabelText hint
      :value value
      :onChange #(let [val (-> % .-target .-value value-acceptor)]
                   (swap! app-state assoc-in ks (or val value))
                   (post-action))
      }]))

(defn shooting[]
  [ui/Paper
   [:div.flex.center
    [:div.flex.column
     [:div.red "Shooting"]
     ]]])

(defn recompute-melee []
  (if-let [odds (compute-melee-odds (:melee-form @app-state))]
    (swap! app-state assoc :melee-odds odds)))

(defn simple-melee-option-checked [ks]
  (let [orig-value (get-in @app-state ks)]
    (swap! app-state assoc-in ks (not orig-value))
    (recompute-melee)))

(defn set-reroll [ks value]
  (swap! app-state assoc-in ks value)
  (recompute-melee))

(defn reroll-component [label ks]  
  (let [value (get-in @app-state ks)]
    [:div
     [ui/SelectField {:floatingLabelText label
                      :value value
                      :onChange (fn [event key value]
                                  (set-reroll ks (if (string? value) (keyword value) value)))}
      [ui/MenuItem {:value :none :primaryText "No reroll"}]
      [ui/MenuItem {:value :fail :primaryText "Reroll failures"}]
      [ui/MenuItem {:value :success :primaryText "Reroll successes"}]
      [ui/MenuItem {:value 1 :primaryText "Reroll ones"}]
      [ui/MenuItem {:value 2 :primaryText "Reroll twos"}]
      [ui/MenuItem {:value 3 :primaryText "Reroll threes"}]
      [ui/MenuItem {:value 4 :primaryText "Reroll fours"}]
      [ui/MenuItem {:value 5 :primaryText "Reroll fives"}]
      [ui/MenuItem {:value 6 :primaryText "Reroll sixes"}]
      ]]))

(defn melee-attack-form []
  (let [div-base
        [:div.flex.column
         [ui/Subheader "Offensive Stats"]
         [text-field "Number of attacks" input/simple-number recompute-melee :melee-form :att]
         [text-field "Offensive Skill" input/simple-number recompute-melee :melee-form :off]
         [text-field "Strength" input/simple-number recompute-melee :melee-form :str]
         [text-field "Armor Penetration" input/simple-number recompute-melee :melee-form :ap]
         [ui/Subheader "Offensive Options"]
         [reroll-component "Reroll Hits" [:melee-form :reroll-hits]]
         [reroll-component "Reroll Wounds" [:melee-form :reroll-wounds]]
         [ui/Checkbox {:label "Poison Attacks"
                       :checked (get-in @app-state [:melee-form :poison])
                       :onCheck #(simple-melee-option-checked [:melee-form :poison])}]
         ]]
    div-base))

(defn melee-defense-form[]
  [:div.flex.column
   [ui/Subheader "Defensive Stats"]
   [text-field "Defensive Skill" input/simple-number recompute-melee :melee-form :def]
   [text-field "Resilience" input/simple-number recompute-melee :melee-form :res]
   [text-field "Armor Save" input/armor-save recompute-melee :melee-form :armor]
   [text-field "Special Save" input/special-save recompute-melee :melee-form :special]
   [ui/Subheader "Defensive Options"]
   [reroll-component "Reroll Armor Save" [:melee-form :reroll-armor]]
   [reroll-component "Reroll Special Save" [:melee-form :reroll-special]]
   
   ])

(defn chart-data [odds]
  (clj->js
   (map (fn [[num-wounds o] datum]
          {:name (str num-wounds " wound" (if (= 1 num-wounds) "" "s"))
           :odds (* 100 o)}) odds)))

(defn format-tooltip [value]
  (if (< value 0.001)
    "< 0.001%"
    (let [fmt-str (cond (< value 0.01) "%.3f%" :else "%.2f%")]
      (gstr/format fmt-str value))))

(defn melee-results[odds]
  (let [data (chart-data odds)]
    [:div.flex.column
     [ui/Subheader "Wounds in Melee"]
     [rc/BarChart {:data data :width 300 :height 250}
      [rc/XAxis {:dataKey "name"}]
      [rc/YAxis]
      [rc/CartesianGrid {:strokeDasharray "3 3"}]
      [rc/Tooltip {:formatter format-tooltip}
       ]
      [rc/Bar {:dataKey "odds" :fill blue}]]]))

(defn melee[odds]
  [ui/Paper
   [:div.flex.center.wrap
    [:div.flex.wrap
     [melee-results odds]
     [melee-attack-form]
     [melee-defense-form]
    ]]])

(defn panel [melee-odds]
  (let [p (:show @app-state)]
    (condp = p
      :melee [melee melee-odds]
      :shooting [shooting])))

(defn hello-world []
  (let [melee-odds (:melee-odds @app-state)]
    [ui/MuiThemeProvider theme
     [:div
      [ui/AppBar {:title "Ninth Age Calculator"
                  :showMenuIconButton false}]
      [panel melee-odds]
      ]
     ]
    ))

(reagent/render-component
 [hello-world]
 (. js/document (getElementById "app")))

(defn on-js-reload []
)
