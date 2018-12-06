(ns mire.util
  (:require [clojure.string :as str]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.player :as player]
            [zprint.core :as zp]))


(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn count-and-pluralize
  "Count and pluralize for display"
  [frequency]
  (let [[name number] frequency]
    (if (> number 1)
      (str number " " name "s")
      (str "a " name))))

(defn comma-and-period
  "a one, 4 twos, and a three."
  [things]
  (let [ids (frequencies things)]
    (if (> (count ids) 1)
      (str (str/join ", " (map count-and-pluralize (butlast ids)))
          ", and " (count-and-pluralize (last ids)) ".")
      (str (count-and-pluralize (first ids)) "."))))

(defn items-in-ref
  "Items in this object"
  [obj]
  (select-keys @items/items @(:items obj)))

(defn find-items-in-ref
  "Find items by name or alias in an obj"
  [id thing]
  ;;(println "find-items-in-ref: " (items-in-ref obj))
  (for [[k i] (items-in-ref id) :when (or (= thing (:name i)) (in? (:aliases i) thing))] k))

(defn find-item-in-ref
  "Get the first item in obj by name"
  [id thing]
  (keyword (first (find-items-in-ref id thing))))

(defn mobs-in-room
  "Mobs in this room"
  [room]
  (select-keys @mobs/mobs @(:mobs room)))

(defn find-mobs-in-room
  "Find mobs by name or alias in a room"
  [room name]
  ;;(println "find-mobs-in-room: " (items-in-ref obj))
  (for [[k i] (mobs-in-room room) :when (or (= name (:name i)) (in? (:aliases i) name))] k))

(defn find-mob-in-room
  "Find the first mob in a room bny name"
  [room name]
  (keyword (first (find-mobs-in-room room name))))

(defn ref-contains?
  "Does an ref's :items contain an object by this name?"
  [id thing]
  (> (count (find-items-in-ref id thing)) 0))

(defn carrying?
   "Is this player carrying something?"
  [thing]
  (ref-contains? player/*player* thing))
  ;;(> (count (find-in-inventory thing)) 0))

(defn room-contains?
  "Does this room contain something?"
  [room thing]
  (ref-contains? room thing))

(defn get-local
  "Get an item from player inventory or current room"
  [thing]
  (if (carrying? thing)
    [(find-item-in-ref player/*player* thing) player/*player*]
    (if (room-contains? @player/*current-room* thing)
      [(find-item-in-ref @player/*current-room* thing) @player/*current-room*])))

(defn flatten-items
  [obj]
  (if (:items obj)
    (assoc obj :items (map #(items/get-item %) @(:items obj)))
    obj))

(defn flatten-mobs
  [obj]
  (if (:mobs obj)
    (assoc obj :mobs (map #(flatten-items (mobs/get-mob %)) @(:mobs obj)))
    obj))

(defn inspect-object
  "Inspect an Object"
  [obj]
  (zp/zprint-str (flatten-items (flatten-mobs obj)) {:map {:key-order [:id :name]}}))

(defn move-between-refs
  "Move instance of obj between from and to. Must call in a transaction."
  [obj from to]
  ;;(println "OBJ:" obj "FROM:" from "TO:" to)
  (alter from disj obj)
  (alter to conj obj))
