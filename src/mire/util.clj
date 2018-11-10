(ns mire.util
  (:require [clojure.string :as str]
            [mire.items :as items]))


(defn ref-contains?
  [obj thing]
  (some #(= thing %) (map #(:name (% @items/items)) @(:items obj))))

(defn items-in-ref
  "Items in this object"
  [obj]
  (select-keys @items/items @(:items obj)))

(defn find-items-in-ref
  "Find items by name in an obj"
  [obj thing]
  (filter #(= thing (:name (val %))) (items-in-ref obj)))

(defn get-item-in-ref
  "Get the first item in obj by name"
  [obj thing]
  (keyword (first (first (find-items-in-ref obj thing)))))

(defn move-between-refs
  "Move instance of obj between from and to. Must call in a transaction."
  [obj from to]
  ;;(println "OBJ:" obj "FROM:" from "TO:" to)
  (alter from disj obj)
  (alter to conj obj))
