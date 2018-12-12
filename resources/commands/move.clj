(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.util :as util]
            [mire.commands :as commands]
            [mire.player :as player]))

(defn move
  "\"♬ We gotta get out of this place... ♪\" Give a direction."
  [args]
  (let [direction (first args)
        target-name ((:exits @player/*current-room*) (keyword direction))
        target (rooms/rooms target-name)
        previous-room @player/*current-room*
        following player/*name*]

    (if target
      (do
        ; move this player
        (util/move-player player/*name* player/*current-room* target direction)
        ; move followers, inform previous room and new room.
        (doseq [p @(:followers player/*player*)]
          (if (contains? @(:inhabitants previous-room) p)
            (binding [player/*player* (@player/players (keyword p))
                      player/*name* p
                      player/*current-room* (:current-room (@player/players (keyword p)))
                      *out* (player/streams p)]
              (player/tell-player p (str "You follow " following " " direction "."))
              (util/move-player player/*name* player/*current-room* target direction)))))

      "You can't go that way.")))
