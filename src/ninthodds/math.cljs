(ns ninthodds.math)

; precomputed odds of "success" based on reroll method
(defonce the-odds
  {:none    [1.0  1.0  0.8333333333333334  0.6666666666666666  0.5                 0.3333333333333333  0.16666666666666666]
   :fail    [1.0  1.0  0.9722222222222222  0.8888888888888888  0.75                0.5555555555555556  0.3055555555555556]
   :success [1.0  1.0  0.6944444444444444  0.4444444444444444  0.25                0.1111111111111111  0.027777777777777776]
   1        [1.0  1.0  0.9722222222222222  0.7777777777777778  0.5833333333333334  0.3888888888888889  0.19444444444444445]
   2        [1.0  1.0  0.8055555555555556  0.7777777777777778  0.5833333333333334  0.3888888888888889  0.19444444444444445]
   3        [1.0  1.0  0.8055555555555556  0.6111111111111112  0.5833333333333334  0.3888888888888889  0.19444444444444445]
   4        [1.0  1.0  0.8055555555555556  0.6111111111111112  0.4166666666666667  0.3888888888888889  0.19444444444444445]
   5        [1.0  1.0  0.8055555555555556  0.6111111111111112  0.4166666666666667  0.2222222222222222  0.19444444444444445]
   6        [1.0  1.0  0.8055555555555556  0.6111111111111112  0.4166666666666667  0.2222222222222222  0.027777777777777776]})

(defn success
  ([reroll-type required]
   (get-in the-odds [(or reroll-type :none) required]))
  ([reroll-type required roll reroll]
   (cond
     (nil? reroll-type) (>= roll required)
     (= reroll-type :fail) (and (>= roll required) (>= reroll required))
     (= reroll-type :success) (or (>= roll required) (>= reroll required))
     (number? reroll-type) (if (not= roll reroll-type) (>= roll required) (>= reroll required)))))

(defn exact-odds
  "Given the set of numbers to be rerolled, what are the odds of getting a specific number"
  [required reroll-set]
  (if (reroll-set required)
    (/ (+ 1 (dec (count reroll-set))) 36.0)
    (/ (+ 6 (count reroll-set)) 36.0)))

(defn reroll-set [reroll-type required]
  (cond 
        (= reroll-type :none) #{}
        (= reroll-type :fail) (into #{} (range 1 required))
        (= reroll-type :success) (into #{} (range required 7))
        (number? reroll-type) #{reroll-type}))

(defn clamp [n min-val max-val]
  (-> n (max min-val) (min max-val)))

(defn bc
  [n]
  "Returns the binomial coefficients for a given n."
  (let [r (inc n)]
     (loop [c 1
	    f (list 1)]
       (if (> c n)
	 f
	 (recur (inc c) (cons (* (/ (- r c) c) (first f)) f))))))
 
(defn binomial
  [n p]
  "Returns the binomial distribution, which is the distribution of the
   number of successes in a series of n experiments whose individual
   success probability is p."
  (let [q (- 1 p)
	n1 (inc n)
	k (range n1)
	pk (take n1 (iterate #(* p %) 1))
	ql (reverse (take n1 (iterate #(* q %) 1)))
	f (bc n)]
    (map vector k (map * f pk ql))))
