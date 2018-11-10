(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.commands :as commands]
            [mire.player :as player]))

(defn look
  "Get a description of the surrounding environs and its contents."
  [& args]
  (println "ROOM: " player/*current-room*)

  (let [exits (map name (keys @(:exits @player/*current-room*)))
        others (rooms/others-in-room)
        items @(:items @player/*current-room*)]

    (str (:desc @player/*current-room*)
      "\nExits: " (str/join ", " exits) ".\n"
      (if (> (count items) 0)
        (println "ITEMS: " (rooms/items-in-room @player/*current-room*))
        ;;(str "You see " (str/join ", " (map #(:sdesc (% @items/items)) (keys @items/items))))
        (str ""))
      (if (> (count others) 0)
        (str "Also here: " (str/join "," others) ".\n") (str "")))))
