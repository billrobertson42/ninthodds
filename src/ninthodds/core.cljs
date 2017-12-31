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
                  :def "3" :res "3"  :armor "" :special ""}}]
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

(defn melee-attack-form []
  [:div.flex.column
   [ui/Subheader "Offensive"]
   [text-field "Number of attacks in unit" input/simple-number recompute-melee :melee-form :att]
   [text-field "Offensive Skill" input/simple-number recompute-melee :melee-form :off]
   [text-field "Strength" input/simple-number recompute-melee :melee-form :str]
   [text-field "Armor Penetration" input/simple-number recompute-melee :melee-form :ap]])

(defn melee-defense-form[]
  [:div.flex.column
   [ui/Subheader "Defensive"]
   [text-field "Defensive Skill" input/simple-number recompute-melee :melee-form :def]
   [text-field "Resilience" input/simple-number recompute-melee :melee-form :res]
   [text-field "Armor Save" input/armor-save recompute-melee :melee-form :armor]
   [text-field "Special Save" input/special-save recompute-melee :melee-form :special]])

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
     [ui/Subheader "Odds to Wound in Melee"]
     [rc/BarChart {:data data :width 300 :height 250}
      [rc/XAxis {:dataKey "name"}]
      [rc/YAxis]
      [rc/CartesianGrid {:strokeDasharray "5 5"}]
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
      [ui/AppBar {:title "Tell Me the Odds!"}]
      [panel melee-odds]
      ]
     ]
    ))

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
