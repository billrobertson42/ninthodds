(ns ninthodds.save
  (:require [ninthodds.math :as math]))

(defn save? [save armor-roll armor-reroll special-roll special-reroll]
  (or (and (:armor save) (math/success (:reroll-armor save) (:armor save) armor-roll armor-reroll))
      (and (:special save) (math/success (:reroll-special save) (:special save) special-roll special-reroll))))

(defn compute-saves [stats]
  (let [armor (max 2 (+ (:armor stats) (:ap stats)))
        special (:special stats)]
    {:armor (if (<= armor 6) armor)
     :special (if (<= special 6) special)
     :reroll-armor (:reroll-armor stats)
     :recoll-special (:reroll-special stats)}))
                     
(defn save-p [stats]
  (let [save (compute-saves stats)]
    (cond
      ;; no saves
      (and (not (:armor save)) (not (:special save))) 0

      ;; armor saves - no special
      (and (:armor save) (not (:special save)) (not (:reroll-armor save)))
      (let [n (count (for [armor-roll (range 1 7)
                           :when (save? save armor-roll nil nil nil)] 1))]
        (/ n (float 6)))

      (and (:armor save) (not (:special save)) (:reroll-armor save))
      (let [n (count (for [armor-roll (range 1 7)
                           armor-reroll (range 1 7)
                           :when (save? save armor-roll armor-reroll nil nil)] 1))]
        (/ n (float 36)))

      ;; special saves - no armor
      (and (not (:armor save)) (:special save) (not (:reroll-special save)))
      (let [n (count (for [special-roll (range 1 7)
                           :when (save? save nil nil special-roll nil)] 1))]
        (/ n (float 6)))

      (and (not (:armor save)) (:special save) (:reroll-special save))
      (let [n (count (for [special-roll (range 1 7)
                           special-reroll (range 1 7)
                           :when (save? save nil nil special-roll special-reroll)] 1))]
        (/ n (float 36)))
      
      ;; both
      (and (:armor save) (:special save) (not (:reroll-armor save)) (not (:reroll-special save)))
      (let [n (count (for [armor-roll (range 1 7)
                           special-roll (range 1 7)
                           :when (save? save armor-roll nil special-roll nil)] 1))]
        (/ n (float 36)))
      
      (and (:armor save) (:special save) (:reroll-armor save) (not (:reroll-special save)))
      (let [n (count (for [armor-roll (range 1 7)
                           armor-reroll (range 1 7)
                           special-roll (range 1 7)
                           :when (save? save armor-roll armor-reroll special-roll nil)] 1))]
        (/ n (float 216)))
      
      (and (:armor save) (:special save) (not (:reroll-armor save)) (:reroll-special save))
      (let [n (count (for [armor-roll (range 1 7)
                           special-roll (range 1 7)
                           special-reroll (range 1 7)
                           :when (save? save armor-roll nil special-roll special-reroll)] 1))]
        (/ n (float 216)))

      (and (:armor save) (:special save) (:reroll-armor save) (:reroll-special save))
      (let [n (count (for [armor-roll (range 1 7)
                           armor-reroll (range 1 7)
                           special-roll (range 1 7)
                           special-reroll (range 1 7)
                           :when (save? save armor-roll armor-reroll special-roll special-reroll)] 1))]
        (/ n (float 1296)))
      
      )))
