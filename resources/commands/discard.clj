(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn discard
  "Put something down that you're carrying"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      (if (util/carrying? thing)
        (let [item (util/find-item-in-ref player/*player* thing)
              name (items/item-name item)]
          (dosync
            (util/move-between-refs item
                               player/*inventory*
                               (:items @player/*current-room*))
            (rooms/tell-room @player/*current-room* (str player/*name* " dropped " name "."))
            (str "You dropped the " name ".")))
        (if (= thing "all")
          (str/join "\n" (for [[k obj] (util/items-in-ref player/*player*)] (discard [(:name obj)])))
          (str "You're not carrying a " thing "."))))
    (str "What do you want to drop?")))
