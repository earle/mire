(ns mire.server
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [server.socket :as socket]
            [mire.player :as player]
            [mire.items :as items]
            [mire.util :as util]
            [mire.commands :as commands]
            [mire.rooms :as rooms]))

(def players (ref {}))

(defn- cleanup []
  "Drop all inventory and remove player from room and player list."
  (dosync
   (doseq [item @player/*inventory*]
     (commands/execute (str "discard " item)))
   (commute player/streams dissoc player/*name*)
   (commute (:inhabitants @player/*current-room*)
            disj player/*name*)))

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
                player/*current-room* (:current-room (@player/players (keyword name)))
                player/*inventory* (:items (@player/players (keyword name)))]
        (dosync
          (ref-set player/*current-room* (@rooms/rooms :start))
          (commute (:inhabitants @player/*current-room*) conj player/*name*)
          (commute player/streams assoc player/*name* *out*)
          (rooms/tell-room @player/*current-room* (str player/*name* " entered the world.")))

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
  (println "Added Items:" (keys @items/all-items))
  (rooms/add-rooms (str dir "/rooms"))
  (commands/add-commands (str dir "/commands")))


(defn -main
  ([port dir]
   (init dir)
   (defonce server (socket/create-server (Integer. port) mire-handle-client))
   (println "Launching Mire server on port" port))

  ([port] (-main port "resources"))

  ([] (-main 3333)))
