(ns ninthodds.melee
  (:require [clojure.string :as str]
            [ninthodds.math :as math]))

(defn hit-chart [stats]
  (let [delta (- (:off stats) (:def stats))]
    (cond
      (>= delta 4) 2
      (#{1 2 3} delta) 3
      (#{-3 -2 -1 0} delta) 4
      (#{-7 -6 -5 -4} delta) 5
      :else 6)))

(defn wound-chart [stats]
  (println "*" (:str stats) (:res stats) (- (:res stats) (:str stats) ))
  (math/clamp (+ 4 (- (:res stats) (:str stats))) 2 6))

(defn success [reroll-fn required roll reroll]
  (or (>= roll required)
      (and reroll reroll-fn (reroll-fn roll) (>= reroll required))))

(defn poison-wound [stats hit-roll hit-reroll]
  (and (:poison stats)
       (success (:reroll-hits stats) 6 hit-roll hit-reroll)))

(defn wound? [stats hit-on wound-on hit-roll hit-reroll wound-roll wound-reroll]
  (or (poison-wound stats hit-roll hit-reroll)
      (and (success (:reroll-hits stats) hit-on hit-roll hit-reroll)
           (success (:reroll-wounds stats) wound-on wound-roll wound-reroll))))

(defn wound-p [stats]
  (let [hit-on (hit-chart stats)
        wound-on (wound-chart stats)]

    (println "*** hit on" hit-on)
    (println "*** wound on" wound-on)
    
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
  (let [p (wound-p stats)]
    (println "p" p)
    (math/binomial (:att stats) p)))
        
      
      
