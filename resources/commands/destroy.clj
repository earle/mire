(ns user
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

; destroy :dagger-0

(defn destroy
  "Destroy an instance of something"
  [args]
  (if (> (count args) 0)
    (let [thing (str/replace-first (str/join " " args) ":" "")
          k (keyword thing)]
      ;; Is this thing an Item?
      (if-let [item (items/get-item k)]
        (dosync
          ;; locate this item (rooms, players, other items)
          ;; remove it from its parent and destroy the instance
          (rooms/tell-others-in-room (str player/*name* " destroyed the "
                                          (items/item-name item) "."))
          (str "You destroyed " (util/inspect-object item) "."))

        ;; ....or is it a Mob?
        (if-let [mob (mobs/get-mob k)]
          ;; locate this mob, remove it from its rooms and destroy the instance
          (dosync
            ;; make sure the mobs current room is a real ref
            (if (instance? clojure.lang.Ref (mob :current-room))
              (if-let [room (rooms/rooms @(mob :current-room))]
                (do
                  ;; remove mob from room
                  (alter (room :mobs) disj k)
                  (rooms/tell-room room (str player/*name* " destroyed the "
                                                  (mobs/mob-name mob) ".") player/*name*))))
            ;; remove instance from the world
            (alter mobs/mobs dissoc k)
            (str "You destroyed:\n" (util/inspect-object mob) "."))
          ;; What is it?
          (str "Specify a valid thing to destroy."))))
    (str "Usage: 'destroy :keyword'")))
