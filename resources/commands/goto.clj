(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.commands :as commands]
            [mire.util :as util]
            [mire.player :as player]))

(defn goto
  "Teleport to something"
  [args]
  (let [name (str/replace-first (first args) ":" "")
        k (keyword name)
        target (atom nil)]
    (do
      ;; what is this exactly?
      (if-let [room (rooms/rooms k)]
        (reset! target room)
        (if-let [mob (mobs/get-mob k)]
          (reset! target @(mob :current-room))
          (if-let [item (items/get-item k)]
            (reset! target (util/find-room-for-item item))
            (if-let [player (player/get-player name)]
              (reset! target @(player :current-room))))))

      ;; if we found a match, move the current player here
      (if @target
        (dosync
          (util/move-between-refs player/*name*
                             (:inhabitants @player/*current-room*)
                             (:inhabitants @target))
          (rooms/tell-others-in-room (str player/*name* " disappeared in a cloud of smoke."))
          (ref-set player/*current-room* @target)
          (rooms/tell-others-in-room (str player/*name* " arrived in a cloud of smoke."))
          (commands/execute "look"))

        ;; Otherwise...
        "No such thing in this world."))))
