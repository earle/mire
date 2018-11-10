(ns mire.player
  (:require [mire.util :as util]
            [mire.items :as items]))

(def ^:dynamic *current-room*)
(def ^:dynamic *inventory*)
(def ^:dynamic *name*)

(def prompt "> ")
(def streams (ref {}))

(defn items-in-inventory
  "Items in this players inventory"
  []
  (select-keys @items/items @*inventory*))

(defn find-in-inventory
  "Find items by name in this players inventory"
  [thing]
  (filter #(= thing (:name (val %))) (items-in-inventory)))

(defn get-from-inventory
  "Get the first item in obj by name"
  [thing]
  (keyword (first (first (find-in-inventory thing)))))

(defn carrying?
   "Is this player carrying something an item"
  [thing]
  (> (count (find-in-inventory thing)) 0))

(defn tell-player
  "Send a message to a specific player."
  [player message]
  (binding [*out* (streams player)]
    (println message)))
