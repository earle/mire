(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

(defn clone
  "Clone an item into callers inventory"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      (if (items/valid-item? thing)
        (let [item (items/clone-item thing)
              name (:sdesc (items/get-item item))]
          (dosync
            (alter player/*inventory* conj item)
            (rooms/tell-room @player/*current-room* (str player/*name* " cloned " name "."))
            (str "You cloned " item " " name)))))))
