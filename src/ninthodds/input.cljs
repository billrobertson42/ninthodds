(ns ninthodds.input
  (:require [clojure.string :as str]))

(defn check-regex[value re]
  (if-let [value (-> value str str/trim)]
    (cond   
      (re-matches re value) value
      (str/blank? value) value)))
  
(defn simple-number [value]
  (check-regex value #"[0-9]+"))

(defn armor-save [value]
  (check-regex value #"[0-6]"))

(defn special-save [value]
  (check-regex value #"[1-6]"))

(defn reroll-specific [value]
  (check-regex value #"[1-6]"))

(defn parse-int [value]
  (if-not (str/blank? value) (js/parseInt value)))

(defn default-vals [amap kvs]
  (let [[k default-value] kvs]
    (cond
      (and k default-value (not (str/blank? (amap k))))
      (recur amap (drop 2 kvs))
      
      (and k default-value)
      (recur (assoc amap k default-value) (drop 2 kvs))

      :else amap)))

(defn required-present? [amap ks]
  (let [subset (select-keys amap ks)]
    (every? #(not (str/blank? %)) (vals subset))))

(defn stats [melee-form]
  (let [melee-form (default-vals melee-form [:ap 0 :armor 7 :special 7])]
    (if (required-present? melee-form [:att :off :str :ap :def :res])
      (reduce #(update %1 %2 parse-int) melee-form [:att :off :str :ap :def :res :armor :special]))))
