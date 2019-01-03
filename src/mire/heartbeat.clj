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
    (let [now (quot (System/currentTimeMillis) 1000)]
      ;; handle time sensitive tasks; combat, healing players, moving npcs, etc.

      ;; Generated mobs need to leave once in awhile
      (doseq [[k v] @mobs/mobs :when (:generated v)]
        ;; if mob has been here more than two minutes, have a 5% chance of leaving
        (if (> now (+ (:generated v) 120))
          (if (< (rand-int 100) 5)
            (let [room (v :current-room)]
              (dosync
                (log/debug "remove-mob: " k "from" (room :id))
                ;; remove from room
                (alter (room :mobs) disj k)
                (rooms/tell-room @room (str "The " (mobs/mob-name v) " left."))
                ;; remove instance from the world
                (alter mobs/mobs dissoc k))))))

      ;; randomly move mobs that move
      (doseq [[k v] @mobs/mobs :when (:moves v)]
        (if (< (rand-int 1000) (:moves v))
          (util/mob-walk k)))

      ;; decay items
      (doseq [[k v] @items/items :when (:decay v)]
        ;; if item has decayed 40% then mark it as decaying and tell room
        ;; if item has decayed 80% then mark it as rotten and tell room
        ;; at 100% disintigrate corpse and contents and tell room
        (let [halflife (:decay v)
              age (+ (- now (:created v)) 1)
              pct (float (* (/ age halflife) 100.0))]

          ;; this item has decayed, inform room, remove from world.
          (if (> age halflife)
            (do
              (if-let [room (util/find-room-for-item v)]
                (rooms/tell-room room (str "The " (items/item-name v) " has decomposed.")))
              (log/debug "decay: " k "has decomposed")
              (util/destroy-item k))

            ;; decay and rot
            (if (and (> pct 80.0) (nil? (:rotten v)))
              ;; mark as rotten, and tell room
              (dosync
                (alter items/items assoc-in [k :rotten] true)
                (alter items/items assoc-in [k :sdesc] (str "rotten " (items/item-name v)))
                (if-let [room (util/find-room-for-item v)]
                  (rooms/tell-room room (str "The " (items/item-name v) " has begun to rot.")))
                (log/debug "decay:" k "has rotted"))

              (if (and (> pct 40.0) (nil? (:decayed v)))
                ;; mark as decayed, and tell room
                (dosync
                  (alter items/items assoc-in [k :decayed] true)
                  (alter items/items assoc-in [k :sdesc] (str "decaying " (items/item-name v)))
                  (if-let [room (util/find-room-for-item v)]
                    (rooms/tell-room room (str "The " (items/item-name v) " has started to decay.")))
                  (log/debug "decay:" k "has decayed")))))))

      ;; generate mobs in rooms with players
      (doseq [[k v] @player/players]
        (let [room (v :current-room)]
          ;; in every room with a player in it, which has :generate...
          (doseq [[m gen] (room :generate)]
            ;; are there already max of this mob in this room?
            (if (< (count (util/find-mobs-in-room @room (name m))) (:max gen))
              ;; roll dice to see if we should generate one
              (if (< (rand-int 1000) (:rate gen))
                (let [id (mobs/clone-mob m @room)
                      mob (mobs/get-mob id)]
                  (dosync
                    ;; add generation time to the mob instance
                    (alter mobs/mobs assoc-in [id :generated] now)
                    (alter (@room :mobs) conj id)
                    (rooms/tell-room @room (str "A " (mobs/mob-name mob) " arrived."))
                    (log/debug "generate-mob:" id "in" (:id @room))))))))))


    ;; Wait for interval...
    (Thread/sleep interval)))
