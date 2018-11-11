(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))


(defn grab
  "Pick something up."
  [args]
  (dosync
    (let [thing (str/join " " args)]
      (if (rooms/room-contains? @player/*current-room* thing)
        (let [item (util/get-item-in-ref @player/*current-room* thing)]
          (do
            (util/move-between-refs item
                                    (:items @player/*current-room*)
                                    player/*inventory*)
            (rooms/tell-room @player/*current-room* (str player/*name* " picked up a " thing "."))
            (str "You picked up the " thing ".")))
        (str "There isn't any " thing " here.")))))
