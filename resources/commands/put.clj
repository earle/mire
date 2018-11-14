(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))


(defn put
  "Put something in something else."
  [args]
  ;; Need to handle variations:
  ;;   put axe in box
  ;;   put battle axe in box
  ;;   put axe box
  ;;   put battle axe box
  ;; box can be in the room, or in the players inventory
  (if (> (count args) 0)
    (let [target (last args)
          thing (str/replace (str/join " " (butlast args)) #"(?i)\s+(in|into)$" "")]
      (if-let [to (first (util/get-local target))]
        (if (items/container? to)
          (if-let [[from from-ref] (util/get-local thing)]
            (dosync
              (util/move-between-refs from
                                      (:items from-ref)
                                      (:items (items/get-item to)))

              (rooms/tell-room @player/*current-room*
                               (str player/*name* " put a " (items/item-name from)
                                    " into a " (items/item-name to) "."))
              (str "You put a " (items/item-name from) " into a " (items/item-name to) "."))
            (str "There isn't any " thing " here."))
          (str "You can't put things into a " (items/item-name to) "."))
        (str "There isn't any " target " here.")))))
