(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.commands :as commands]
            [mire.player :as player]))

(defn look
  "Get a description of the surrounding environs and its contents."
  [& args]
  (let [exits (map name (keys @(:exits @player/*current-room*)))
        others (rooms/others-in-room)
        items @(:items @player/*current-room*)]

    (str (:desc @player/*current-room*)
      "\nExits: " (str/join ", " exits) ".\n"
      (str/join (map #(str "There is a " % " here.\n") items))
      (if (> (count others) 0) (str "Also here: " (str/join "," others) ".\n") (str "")))))
