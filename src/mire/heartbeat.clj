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

      ;; Generated mobs need to leave once in awhile
      (doseq [[k v] @mobs/mobs :when (:generated v)]
        ;; if mob has been here more than two minutes, have a 5% chance of leaving
        (if (> (System/currentTimeMillis) (+ (:generated v) 120000))
          (if (< (rand-int 100) 5)
            (let [room (v :current-room)]
              (dosync
                ;; remove from room
                (alter (room :mobs) disj k)
                (rooms/tell-room @room (str "The " (mobs/mob-name v) " left."))
                (log/debug "remove-mob: " k "from" (room :id))
                ;; remove instance from the world
                (alter mobs/mobs dissoc k))))))

      ;; generate mobs in rooms with players
      (doseq [[k v] @player/players]
        ;; in every room with a player in it, which has :generate,
        (let [room (v :current-room)]
          (doseq [[m gen] (room :generate)]
            ;; are there already max of this mob in this room?
            (if (< (count (util/find-mobs-in-room @room (name m))) (:max gen))
              ;; roll dice to see if we should generate one
              (if (< (rand-int 1000) (:rate gen))
                (let [id (mobs/clone-mob m @room)
                      mob (mobs/get-mob id)]
                  (dosync
                    ;; add generation time to the mob instance
                    (alter mobs/mobs assoc-in [id :generated] (System/currentTimeMillis))
                    (alter (@room :mobs) conj id)
                    (rooms/tell-room @room (str "A " (mobs/mob-name mob) " arrived."))
                    (log/debug "generate-mob:" id "in" (:id @room)))))))))


      ;; move mobs
      (doseq [[k v] @mobs/mobs :when (:moves v)]
        (let [r (rand-int 1000)]
          (if (< r (:moves v))
            (util/mob-walk k)))))))
