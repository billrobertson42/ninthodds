(ns ninthodds.math)

(def p (into {} (map #(vector % (/ % 6)) (range 1 7))))

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
