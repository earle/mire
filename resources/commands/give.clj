(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))

;; Need to handle variations:
;;   give axe to alice
;;   give battle axe to alice
;;   give axe alice
;;   give battle axe alice

(defn give
  "Give something to someone"
  [args]
  (if (> (count args) 0)
    (let [thing (str/replace (str/join " " (butlast args)) #"(?i)\s+to$" "")
          id (util/find-item-in-ref player/*player* thing)
          item (items/get-item id)
          name (items/item-name item)]

      ;; Are we carrying this thing?
      (if (util/carrying? thing)
        ;; Is this a player?
        (if-let [who (player/get-player (last args))]
          ;; Is this player in the current room?
          (if (contains? (rooms/others-in-room) (:name who))
            (dosync
              (util/move-between-refs id player/*inventory* (:items who))
              (rooms/tell-room @player/*current-room*
                               (str player/*name* " gave a " name
                                    " to " (:name who) ".") (:name who) player/*name*)
              (player/tell-player (:name who)
                                  (str player/*name* " gave you a " name "."))
              (str "You gave a " name " to " (:name who) "."))
            (str (:name who) " isn't here."))

          ;; It's not a Player, is it a mob?
          (if-let [mob (mobs/get-mob (util/find-mob-in-room @player/*current-room* (last args)))]
            (dosync
              (util/move-between-refs id player/*inventory* (:items mob))
              (rooms/tell-others-in-room (str player/*name* " gave a " name " to the " (:name mob) "."))
              (str "You gave a " name " to the " (:name mob) "."))
            (str "There isn't a " (last args) " here.")))
        (str "You dont have a " thing " to give.")))
    (str "What do you want to give, to whom?")))
