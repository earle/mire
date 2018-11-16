(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))


;; Need to handle variations:
;;   put axe in box
;;   put battle axe in box
;;   put axe box
;;   put battle axe box
;; box can be in the room, or in the players inventory

(defn put
  "Put something in something else."
  [args]
  (if (> (count args) 0)
    (let [target (last args)
          thing (str/replace (str/join " " (butlast args)) #"(?i)\s+(in|into)$" "")]
      ;; does this container item exist in the room or inventory?
      (if-let [to (items/get-item (first (util/get-local target)))]
        ;; make sure the item is a container
        (if (items/container? to)
          ;; does the item we're moving exist in the room or inventory?
          (if-let [[from from-ref] (util/get-local thing)]
            (let [item (items/get-item from)]
              (dosync
                (util/move-between-refs from
                                        (:items from-ref)
                                        (:items to))

                (rooms/tell-room @player/*current-room*
                                 (str player/*name* " put a " (items/item-name item)
                                      " into the " (items/item-name to) "."))
                (str "You put a " (items/item-name item) " into the " (items/item-name to) ".")))
            (str "There isn't any " thing " here."))
          (str "You can't put things into the " (items/item-name to) "."))
        (str "There isn't any " target " here.")))))
