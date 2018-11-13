(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.util :as util]
            [mire.player :as player]))

(defn look
  "Get a description of the surrounding environs and its contents."
  [& args]
  (let [exits (map name (keys @(:exits @player/*current-room*)))
        others (rooms/others-in-room)
        items @(:items @player/*current-room*)]

    (str (:desc @player/*current-room*)
      "\nExits: " (str/join ", " exits) ".\n"
      (if (> (count items) 0)
        (str "You see " (util/comma-and-period (map items/item-name items)))
        (str ""))
      (if (> (count others) 0)
        (str "Also here: " (str/join "," others) ".\n")
        (str "")))))
