(ns user
  (:require [clojure.string :as str]
            [mire.object :as object]
            [mire.rooms :as rooms]
            [mire.player :as player]))


(defn grab
  "Pick something up."
  [args]
  (dosync
    (let [thing (first args)]
      (if (rooms/room-contains? @player/*current-room* thing)
        (do (object/move-between-refs (keyword thing)
                               (:items @player/*current-room*)
                               player/*inventory*)
            (rooms/tell-room @player/*current-room* (str player/*name* " picked up a " thing "."))
            (str "You picked up the " thing "."))
        (str "There isn't any " thing " here.")))))
