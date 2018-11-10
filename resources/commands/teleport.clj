(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.commands :as commands]
            [mire.util :as util]
            [mire.player :as player]))

(defn teleport
  "Teleport to a Room"
  [args]
  (dosync
    (let [target (@rooms/rooms (keyword (first args)))]
      (if target
        (do
          (util/move-between-refs player/*name*
                             (:inhabitants @player/*current-room*)
                             (:inhabitants target))
          (rooms/tell-room @player/*current-room* (str player/*name* " disappeared in a cloud of smoke."))
          (ref-set player/*current-room* target)
          (rooms/tell-room @player/*current-room* (str player/*name* " arrived in a cloud of smoke."))
          (commands/execute "look"))

       "No such room."))))
