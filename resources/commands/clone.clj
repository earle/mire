(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

; clone item :dagger
; clone mob :guard

(defn clone
  "Clone an item"
  [args]
  (if (> (count args) 1)
    (let [what (str/lower-case (first args))
          thing (str/replace-first (str/join " " (rest args)) ":" "")
          k (keyword thing)]
      (if (= what "item")
        (if-let [id (items/clone-item k)]
          (dosync
            (alter player/*inventory* conj id)
            (rooms/tell-room @player/*current-room*
                             (str player/*name* " cloned a "
                                  (items/item-name (items/get-item id)) "."))
            (str "You cloned a " (items/item-name (items/get-item id)) ", " id))
          (str "Specify a valid item to clone."))
        (if (= what "mob")
          (if-let [id (mobs/clone-mob k)]
            (dosync

              (alter (player/*current-room* :mobs) conj id)
              (rooms/tell-room @player/*current-room*
                               (str player/*name* " cloned a "
                                    (mobs/mob-name (mobs/get-mob id)) "."))
              (str "You cloned a " (mobs/mob-name (mobs/get-mob id)) ", " id))
            (str "Specify a valid mob to clone."))
          (str "Usage: 'clone <item|mob> :keyword'"))))
    (str "Usage: 'clone <item|mob> :keyword'")))
