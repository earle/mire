(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.util :as util]
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
          ;; Did we specify an item keyword?
          (if (= (first thing) \:)
            (if-let [item (items/get-item (keyword (str/replace thing ":" "")))]
              (str thing " " (pprint/write item :stream nil))
              (str "Can't find " thing " to inspect.")))))
    ;; inspecting a room instead
    (let [items-in-room (str/join "\n    "
                          (map #(str (first %) " " (last %))
                               (util/items-in-ref @player/*current-room*)))]
      (str "Room: " (pprint/write (dissoc @player/*current-room* :items) :stream nil)
           "\n :items "
           @(:items @player/*current-room*)
           "\n    " items-in-room "\n"))))
           
