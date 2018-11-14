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
    (let [thing (str/replace (str/join " " args) #"(?i)^(in|into)\s+" "")]
      (if-let [[item item-ref] (util/get-local thing)]
        (let [name (items/item-name item)]
          (if (items/container? item)
            (str (if (= item-ref player/*player*)
                   (str "You are carrying ")
                   (str "You see "))
                 (items/item-name item)
                 ", which contains:\n"
                 (if (> (count (items/contents item)) 0)
                   (util/comma-and-period (map items/item-name (items/contents item)))
                   (str "nothing.")))
            (str (if (= item-ref player/*player*)
                   (str "You are carrying ")
                   (str "You see "))
                 (items/item-name item) ".")))
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
          (str "\nAlso here: " (str/join "," others) ".\n")
          (str ""))))))
