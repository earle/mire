(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))


(defn grab
  "Pick something up."
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      (if (rooms/room-contains? @player/*current-room* thing)
        (let [item (util/get-item-in-ref @player/*current-room* thing)
              name (items/item-name item)]
          (if (items/moveable? item)
            (dosync
              (util/move-between-refs item
                                      (:items @player/*current-room*)
                                      player/*inventory*)
              (rooms/tell-room @player/*current-room* (str player/*name* " picked up " name "."))
              (str "You picked up " name "."))
            (do
              (rooms/tell-room @player/*current-room* (str player/*name* " tried to pick up " name ", and failed."))
              (str "You can't pick up " name "."))))
        (if (= thing "all")
          (str/join "\n" (for [[k obj] (util/items-in-ref @player/*current-room*)] (grab [(:name obj)])))
          (str "There isn't any " thing " here."))))
    (str "What do you want to get?")))
