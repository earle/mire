(ns user
  (:require [clojure.string :as str]
            [mire.items :as items]
            [mire.player :as player]))

(defn inventory
  "See what you've got."
  [args]
  (str "You are carrying:\n"
       (str/join "\n" (map items/item-name (seq @player/*inventory*)))))
