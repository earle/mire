(ns mire.util
  (:require [clojure.string :as str]
            [mire.items :as items]))


(defn count-and-pluralize
  "Count and pluralize for display"
  [frequency]
  (let [[name number] frequency]
    (if (> number 1)
      (str number " " name "s")
      (str "a " name))))

(defn comma-and-period
  "One, Two, and Three."
  [things]
  (let [objs (frequencies things)]
    (if (> (count objs) 1)
      (str (str/join ", "
             (map count-and-pluralize (butlast objs))) ", and " (count-and-pluralize (last objs)) ".")
      (str (count-and-pluralize (first objs)) "."))))

(defn items-in-ref
  "Items in this object"
  [obj]
  (select-keys @items/items @(:items obj)))

(defn find-items-in-ref
  "Find items by name or alias in an obj"
  [obj thing]
  ;;(println "find-items-in-ref: " (items-in-ref obj))
  (for [[k i] (items-in-ref obj) :when (or (= thing (:name i)) (some #(= thing %) (:aliases i)))] k))

(defn ref-contains?
  "Does an ref's :items contain an object by this name?"
  [obj thing]
  (> (count (find-items-in-ref obj thing)) 0))

(defn get-item-in-ref
  "Get the first item in obj by name"
  [obj thing]
  (keyword (first (find-items-in-ref obj thing))))

(defn move-between-refs
  "Move instance of obj between from and to. Must call in a transaction."
  [obj from to]
  ;;(println "OBJ:" obj "FROM:" from "TO:" to)
  (alter from disj obj)
  (alter to conj obj))
