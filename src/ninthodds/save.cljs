(ns ninthodds.save
  (:require [ninthodds.math :as math]))

(defn compute-saves [stats]
  (let [armor (max 2 (+ (:armor stats) (:ap stats)))
        special (:special stats)]
    {:armor (if (<= armor 6) (math/success (:reroll-armor stats) armor) 0.0)
     :special (if (<= special 6) (math/success (:reroll-special stats) special) 0.0)}))
                     
(defn save-p [stats]
  (let [save (compute-saves stats)]
    (+ (:armor save) (* (- 1 (:armor save)) (:special save)))))

