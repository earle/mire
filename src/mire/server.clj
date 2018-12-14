(ns mire.server
  (:require [nrepl.server :refer [start-server stop-server]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.commands :as commands]
            [mire.heartbeat :as heartbeat]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [mire.util :as util]
            [reply.eval-modes.nrepl :as eval-modes.nrepl]
            [server.socket :as socket]))


(def players (ref {}))

(defn- cleanup []
  "Player has disconnected."
  (dosync
    ;; tell the room
    (rooms/tell-others-in-room (str player/*name* " disconnected."))

    ;; Remove player stream, remove from room.
    (commute player/streams dissoc player/*name*)
    (commute (:inhabitants @player/*current-room*) disj player/*name*)))

(defn- get-unique-player-name [s]
  (let [name (str/capitalize s)]
    (if (@player/streams name)
      (do (print "That name is in use; try again: ")
        (flush)
        (recur (read-line)))
      (if (< (count name) 2)
        (do (print "Choose a longer name; try again: ")
          (flush)
          (recur (read-line)))
        name))))

(defn- mire-handle-client [in out]
  (binding [*in* (io/reader in)
            *out* (io/writer out)
            *err* (io/writer System/err)]

    (print "\nWhat is your name? ") (flush)

    (let [name (get-unique-player-name (read-line))
          obj (player/create-player name)]

      ;; Add this player to the game
      (player/add-player name obj)

      ;; We have to nest this in another binding call instead of using
      ;; the one above so *in* and *out* will be bound to the socket
      (binding [player/*player* (@player/players (keyword name))
                player/*name* name
                player/*input-stream* in
                player/*output-stream* out
                player/*current-room* (:current-room (@player/players (keyword name)))
                player/*inventory* (:items (@player/players (keyword name)))]
        (dosync
          (ref-set player/*current-room* (@rooms/rooms :start))
          (commute (:inhabitants @player/*current-room*) conj player/*name*)
          (commute player/streams assoc player/*name* *out*)
          (rooms/tell-others-in-room (str player/*name* " entered the world.")))

        (println (commands/execute "look")) (print player/prompt) (flush)

        ;; Main REPL loop
        (try (loop [input (read-line)]
               (when input
                 (if-let [s (commands/execute input)]
                   (println s))
                 (.flush *err*)
                 (print player/prompt)
                 (flush)
                 (recur (read-line))))
             (finally (cleanup)))))))

(defn- init
  [dir]
  (items/add-items (str dir "/items"))
  (log/debug "Added Items:" (keys @items/items-db))
  (mobs/add-mobs (str dir "/mobs"))
  (log/debug "Added Mobs:" (keys @mobs/mobs-db))
  (rooms/add-rooms (str dir "/rooms"))
  (commands/add-commands (str dir "/commands")))

(defn -main
  ([port dir]
   (init dir)

   (defonce nrepl-server (start-server :port 7888))
   (log/info "Launching nREPL server on port 7888")

   (defonce server (socket/create-server (Integer. port) mire-handle-client))
   (log/info "Launching Mire server on port" port)

   (heartbeat/heartbeat 4000))

  ([port] (-main port "resources"))

  ([] (-main 3333)))
