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
    (if-let [who (player/get-player (last args))]
      (if (contains? (rooms/others-in-room) (:name who))
        (let [thing (str/replace (str/join " " (butlast args)) #"(?i)\s+to$" "")]
          (if (util/carrying? thing)
            (let [id (util/find-item-in-ref player/*player* thing)
                  item (items/get-item id)
                  name (items/item-name item)]
              (dosync
                (util/move-between-refs item
                                        player/*inventory*
                                        (:items who))
                (rooms/tell-room @player/*current-room*
                                 (str player/*name* " gave a " name
                                      " to " (:name who) ".") (:name who))
                (player/tell-player (:name who)
                                    (str player/*name* " gave you a "
                                         name "."))
                (str "You gave a " name " to " (:name who) ".")))
            (str "You dont have a " thing " to give.")))

        (str (:name who) " isnt here."))
      (str "No such player " (last args) "."))
    (str "What do you want to give, to whom?")))