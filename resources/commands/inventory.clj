(ns user
  (:require [clojure.string :as str]
            [mire.items :as items]
            [mire.player :as player]))

(defn inventory
  "See what you've got."
  [args]
  (str "You are carrying:\n"
    (if (> (count @player/*inventory*) 0)
       (str/join "\n"
           (map items/item-name (seq @player/*inventory*)))
       (str "nothing."))))
