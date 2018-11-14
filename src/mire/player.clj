(ns mire.player
  (:require [clojure.string :as str]
            [mire.items :as items]))

(def ^:dynamic *player*)
(def ^:dynamic *current-room*)
(def ^:dynamic *inventory*)
(def ^:dynamic *name*)

(def prompt "> ")

(def players (ref {}))
(def streams (ref {}))

(defn get-player
  "Get a Player by name"
  [name]
  ((keyword (str/capitalize name)) @players))

(defn create-player
  "Create a player"
  [name]
  {(keyword name) {:name name
                   :sex "male"
                   :last-command (ref #{})
                   :current-room (ref #{})
                   :items (ref #{})}})

(defn items-in-inventory
  "Items in this players inventory"
  []
  (select-keys @items/items @*inventory*))

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
