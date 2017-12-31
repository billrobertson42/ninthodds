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

(defn wound? [stats hit-on wound-on hit-roll hit-reroll wound-roll wound-reroll]
  (or (and (:poison stats) (math/success (:reroll-hits stats) 6 hit-roll hit-reroll))
      (and (math/success (:reroll-hits stats) hit-on hit-roll hit-reroll)
           (math/success (:reroll-wounds stats) wound-on wound-roll wound-reroll))))

(defn wound-p [stats]
  (let [hit-on (hit-chart stats)
        wound-on (wound-chart stats)]

    (cond      
      (not (or (:reroll-hits stats) (:reroll-wounds stats)))
      (let [n (count (for [hit-roll (range 1 7)
                           wound-roll (range 1 7)
                           :when (wound? stats hit-on wound-on hit-roll nil wound-roll nil)] 1))]
        (/ n (float 36)))

      (and (:reroll-hits stats) (:reroll-wounds stats))
      (let [n (count (for [hit-roll (range 1 7)
                           hit-reroll (range 1 7)
                           wound-roll (range 1 7)
                           wound-reroll (range 1 7)
                           :when (wound? stats hit-on wound-on hit-roll hit-reroll wound-roll wound-reroll)] 1))]
        (/ n (float 1296)))

      (:reroll-hits stats)
      (let [n (count (for [hit-roll (range 1 7)
                           hit-reroll (range 1 7)
                           wound-roll (range 1 7)
                           :when (wound? stats hit-on wound-on hit-roll hit-reroll wound-roll nil)] 1))]
        (/ n (float 216)))
      
      (:reroll-wounds stats)
      (let [n (count (for [hit-roll (range 1 7)     
                           wound-roll (range 1 7)
                           wound-reroll (range 1 7)
                           :when (wound? stats hit-on wound-on hit-roll nil wound-roll wound-reroll)] 1))]
        (/ n (float 216))))))

(defn tell-me-the-odds! [stats]
  (let [wp (wound-p stats)
        sp (save/save-p stats)
        p (* wp (- 1 sp))]
    (println stats)
    (println wp sp (- 1 sp) p)
    (math/binomial (:att stats) p)))
      
