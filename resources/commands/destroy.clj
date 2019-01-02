(ns user
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

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
          (do
            (if (instance? clojure.lang.Ref (mob :current-room))
              (if-let [room (mob :current-room)]
                (rooms/tell-room @room (str player/*name* " destroyed the "
                                                    (mobs/mob-name mob) ".") player/*name*)))
            ;; locate this mob, remove it from its rooms and destroy the instance
            (util/destroy-mob k)
            (str "You destroyed:\n" (util/inspect-object mob) "."))

          ;; What is it?
          (str "Specify a valid thing to destroy."))))
    (str "Usage: 'destroy :keyword'")))
