(ns mire.player)

(def ^:dynamic *current-room*)
(def ^:dynamic *inventory*)
(def ^:dynamic *name*)

(def prompt "> ")
(def streams (ref {}))

(defn carrying? [thing]
  (some #{(keyword thing)} @*inventory*))

(defn tell-player
  "Send a message to a specific player."
  [player message]
  (binding [*out* (streams player)]
    (println message)))
