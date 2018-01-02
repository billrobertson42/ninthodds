(ns ninthodds.melee
  (:require [clojure.string :as str]
            [ninthodds.math :as math]
            [ninthodds.save :as save]))

(defn hit-chart [stats]
  (let [delta (- (:off stats) (:def stats))]
    (cond
      (>= delta 4) 2
      (#{1 2 3} delta) 3
      (#{-3 -2 -1 0} delta) 4
      (#{-7 -6 -5 -4} delta) 5
      :else 6)))

(defn wound-chart [stats]
  (math/clamp (+ 4 (- (:res stats) (:str stats))) 2 6))

(defn poison-wound-odds [reroll-hits hit-on reroll-wounds wound-on]
  (let [hit-rerolls (math/reroll-set reroll-hits hit-on)
        wound-rerolls (math/reroll-set reroll-wounds wound-on)
        auto-wound-odds (math/exact-odds 6 hit-rerolls)
        non-poison-hit-odds (reduce + (map #(math/exact-odds % wound-rerolls) (range hit-on 6)))
        non-poison-wound-odds (* non-poison-hit-odds (math/success reroll-wounds wound-on))]
    (+ auto-wound-odds non-poison-wound-odds)))

(defn normal-wound-odds [hit-reroll hit-on wound-reroll wound-on]
  (let [hit-odds (math/success hit-reroll hit-on)
        wound-odds (math/success wound-reroll wound-on)]
    (* hit-odds wound-odds)))

(defn wound-p [stats]
  (let [hit-on (hit-chart stats)
        wound-on (wound-chart stats)]    
    (if (:poison stats)
      (poison-wound-odds (:reroll-hits stats) hit-on (:reroll-wounds stats) wound-on)
      (normal-wound-odds (:reroll-hits stats) hit-on (:reroll-wounds stats) wound-on))))

(defn tell-me-the-odds! [stats]
  (let [wp (wound-p stats)
        sp (save/save-p stats)
        p (* wp (- 1 sp))]
    (println stats)
    (println wp sp (- 1 sp)  p)
    (math/binomial (:att stats) p)))
      
