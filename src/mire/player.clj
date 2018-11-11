(ns mire.player
  (:require [mire.util :as util]
            [mire.items :as items]))

(def ^:dynamic *player*)
(def ^:dynamic *current-room*)
(def ^:dynamic *inventory*)
(def ^:dynamic *name*)

(def prompt "> ")

(def players (ref {}))
(def streams (ref {}))

(defn items-in-inventory
  "Items in this players inventory"
  []
  (select-keys @items/items @*inventory*))

(defn carrying?
   "Is this player carrying something an item"
  [thing]
  (util/ref-contains? *player* thing))
  ;;(> (count (find-in-inventory thing)) 0))

(defn tell-player
  "Send a message to a specific player."
  [player message]
  (binding [*out* (streams player)]
    (println message)))

(defn add-player
  "Add a Player to the Game"
  [name obj]
  (dosync
    (alter players conj obj)))

(defn remove-player
  "Remove a Player from the Game"
  [name obj]
  (dosync
    (alter players disj obj)))
