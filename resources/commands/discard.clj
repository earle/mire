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
        (do (util/move-between-refs (keyword thing)
                               player/*inventory*
                               (:items @player/*current-room*))
            (rooms/tell-room @player/*current-room* (str player/*name* " dropped a " thing "."))
            (str "You dropped the " thing "."))
        (str "You're not carrying a " thing ".")))))
