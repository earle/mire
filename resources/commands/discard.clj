(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn discard
  "Discard an item that you're carrying"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      (if (util/carrying? thing)
        (let [id (util/find-item-in-ref player/*player* thing)
              item (items/get-item id)
              name (items/item-name item)]

          (if (items/wielded? item)
            (str "You must unwield it first.")
            (dosync
              (util/move-between-refs id
                                      player/*inventory*
                                      (:items @player/*current-room*))
              (ref-set (:parent item) (player/*current-room* :id))
              (rooms/tell-others-in-room (str player/*name* " dropped a " name "."))
              (str "You dropped the " name "."))))
        (if (= thing "all")
          (str/join "\n" (for [[k obj] (util/items-in-ref player/*player*)] (discard [(:name obj)])))
          (str "You're not carrying a " thing "."))))
    (str "What do you want to drop?")))
