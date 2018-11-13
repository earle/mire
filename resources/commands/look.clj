(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.util :as util]
            [mire.player :as player]))

(defn look
  "Get a description of the surrounding environs and its contents."
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      (if (rooms/room-contains? @player/*current-room* thing)
        (let [item (util/get-item-in-ref @player/*current-room* thing)
              name (items/item-name item)]
          (if (items/container? item)
            (str "You see " (items/item-name item)
                 ", which contains:\n"
                 (if (> (count (items/contents item)) 0)
                   (str "count:" (count @(:items @(items/get-item item))) ": " (:items @(items/get-item item)))
                   (str "nothing.")))
            (str "You see a " (items/item-name item) ".")))
        (str "There is no " thing " here.")))
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
          (str ""))))))
