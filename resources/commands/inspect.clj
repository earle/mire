(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

(defn inspect
  "Inspect an item"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)
          in-inventory (util/find-items-in-ref player/*player* thing)
          in-room (util/find-items-in-ref @player/*current-room* thing)]
        (if (> (count (concat in-inventory in-room)) 0)
          (str
            (str/join "\n" (map #(str "In Room: " % " "  (% @items/items)) in-room))
            (if (> (count in-room) 0)
              (str "\n") (str ""))
            (str/join "\n" (map #(str "Carrying: " % " " (% @items/items)) in-inventory)))
          (str "Can't find " thing " to inspect.")))
    (str "Room: " (pprint/write @player/*current-room* :stream nil))))
