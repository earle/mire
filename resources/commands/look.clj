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
      ; Is this thing an item in the inventory or room?
      (if-let [[id item-ref] (util/get-local thing)]
        (let [item (items/get-item id)]
          ; Is it a container?  What's inside?
          (if (items/container? item)
            (str (if (= item-ref player/*player*)
                   (str "You are carrying ")
                   (str "You see "))
                 (items/item-name item)
                 ", which contains:\n"
                 (if (> (count (items/contents item)) 0)
                   (util/comma-and-period (map #(items/item-name (items/get-item %)) (items/contents item)))
                   (str "nothing.")))
            ; If not a container, what is it?
            (str (if (= item-ref player/*player*)
                   (str "You are carrying ")
                   (str "You see "))
                 (items/item-desc item) ".")))
        ; If its not an item, is it a Player?
        (str "There is no " thing " here.")))
    ; Otherwise, what's in the room?
    (let [exits (map name (keys @(:exits @player/*current-room*)))
          others (rooms/others-in-room)
          mobs  (map #(mobs/mob-name (mobs/get-mob %)) @(:mobs @player/*current-room*))
          items (map #(items/item-name (items/get-item %)) @(:items @player/*current-room*))]
      (str (:desc @player/*current-room*)
        "\nExits: " (str/join ", " exits) ".\n"
        (if (> (count (concat mobs items)) 0)
          (str "You see " (util/comma-and-period (concat mobs items)) "\n"))
        (if (> (count others) 0)
          (str "Also here: " (str/join ", " others) ".\n"))))))
