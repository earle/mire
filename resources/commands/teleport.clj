(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.commands :as commands]
            [mire.util :as util]
            [mire.player :as player]))

(defn teleport
  "Teleport to a Room"
  [args]
  (if-let [target (@rooms/rooms (keyword (str/replace-first (first args) ":" "")))]
    (dosync
      (util/move-between-refs player/*name*
                         (:inhabitants @player/*current-room*)
                         (:inhabitants target))
      (rooms/tell-room @player/*current-room* (str player/*name* " disappeared in a cloud of smoke."))
      (ref-set player/*current-room* target)
      (rooms/tell-room @player/*current-room* (str player/*name* " arrived in a cloud of smoke."))
      (commands/execute "look"))
    
    ;; Otherwise...
    "No such room."))
