(ns mire.heartbeat
  (:require [clojure.string :as str]
            [mire.player :as player]
            [mire.items :as items]
            [mire.util :as util]
            [mire.commands :as commands]
            [mire.rooms :as rooms]))

(defn heartbeat
  "The main heartbeat background process."
  [interval]
  (println "heartbeat: starting, interval:" interval " seconds.")
  (while true
    ; handle time sensitive tasks; combat, healing players, moving npcs, etc.
    (Thread/sleep interval)))
