(ns mire.heartbeat
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.commands :as commands]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [mire.util :as util]))


(defn heartbeat
  "The main heartbeat background process."
  [interval]
  (log/info "heartbeat starting," (/ interval 1000) "second interval.")

  (while true
    (do
      ;; handle time sensitive tasks; combat, healing players, moving npcs, etc.
      (Thread/sleep interval)

      ;; generate mobs in rooms with players
      ;; in every room with a player in it, which has :generate,
      ;;   if number of mobs in room < max to generate:
      ;;      roll dice, compare it generation rate
      ;;        if successful, clone mob into room.
      (doseq [[k v] @player/players]
        (let [room (v :current-room)]
          (doseq [[m gen] ((v :current-room) :generate)]
            ;; are there already max of this mob in this room?
            ;; else, roll dice
            (if (< (count (util/find-mobs-in-room @room (name m))) (:max gen))
              (let [r (rand-int 1000)]
                (if (< r (:rate gen))
                  (let [mob (mobs/get-mob (mobs/clone-mob m room))]
                    (dosync
                      (alter (@room :mobs) conj (:id mob))
                      (rooms/tell-room @room (str "A " (mobs/mob-name mob) " arrived."))
                      (log/debug "generate:" m "in" (:id @room))))))))))

      ;; move mobs
      (doseq [[k v] @mobs/mobs :when (:moves v)]
        (let [r (rand-int 1000)]
          (if (< r (:moves v))
            (util/mob-walk k)))))))
