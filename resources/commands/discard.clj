(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn discard
  "Put something down that you're carrying."
  [args]
  (dosync
    (let [thing (first args)]
      (if (player/carrying? thing)
        (let [item (player/get-from-inventory thing)
              name (items/item-name item)]
          (do
            (util/move-between-refs item
                               player/*inventory*
                               (:items @player/*current-room*))
            (rooms/tell-room @player/*current-room* (str player/*name* " dropped " name "."))
            (str "You dropped the " name ".")))
        (str "You're not carrying a " thing ".")))))
