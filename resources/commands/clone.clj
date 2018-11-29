(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

(defn clone
  "Clone an item"
  [args]
  (if (> (count args) 0)
    (let [thing (str/replace-first (str/join " " args) ":" "")]
      (if (items/valid-item? thing)
        (let [id (items/clone-item thing)
              item (items/get-item id)]
          (dosync
            (alter player/*inventory* conj id)
            (rooms/tell-room @player/*current-room* (str player/*name* " cloned a " (items/item-name item) "."))
            (str "You cloned a " (items/item-name item) " " item)))
        (str "Specify a valid item to clone.")))))
