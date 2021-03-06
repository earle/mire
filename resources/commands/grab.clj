(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))

;; Need to handle:
;; get key
;; get golden key
;; get key from box
;; get key from big box
;; get golden key in box
;; get key from big box
;; get all
;; get all from box
;; -- where box can be in the room, or in the players inventory

(defn grab
  "Pick something up"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      ;; Is this thing in the room?
      (if (util/room-contains? @player/*current-room* thing)
        (let [id (util/find-item-in-ref @player/*current-room* thing)
              item (items/get-item id)
              name (items/item-name item)]
          (if (items/moveable? item)
            (dosync
              (util/move-between-refs id
                                      (:items @player/*current-room*)
                                      player/*inventory*)
              (ref-set (:parent item) player/*name*)
              (rooms/tell-others-in-room (str player/*name* " picked up a " name "."))
              (str "You picked up the " name "."))
            (do
              (rooms/tell-others-in-room (str player/*name* " tried to pick up a " name ", and failed."))
              (str "You can't pick up the " name "."))))

        ;; is this thing in a container?
        (if (re-find #"(?i)\s+from\s+" thing)
          (let [things (str/split thing #"(?i)\s+from\s+")
                what (str/join "" (butlast things))
                [from-id from-ref] (util/get-local (last things))]
            (if-let [from (items/get-item from-id)]
              ;; does this item exist in this container?
              (if-let [id (util/find-item-in-ref (items/get-item from-id) what)]
                (let [item (items/get-item id)]
                  (dosync
                    (util/move-between-refs id
                                            (:items from)
                                            player/*inventory*)
                    (ref-set (:parent item) player/*name*)
                    (rooms/tell-others-in-room (str player/*name* " got a " (items/item-name item)
                                                 " out of a " (items/item-name from) "."))
                    (str "You got a " (items/item-name item) " out of a " (items/item-name from) ".")))
                (str "There isn't a " what " in the " (items/item-name from) "."))
              (str "There isn't any " (last things) " to get things out of.")))

          ;; getting all from the room?
          (if (= thing "all")
            (str/join "\n" (for [[k obj] (util/items-in-ref @player/*current-room*)] (grab [(:name obj)])))
            (str "There isn't any " thing " here.")))))

    (str "What do you want to get?")))
