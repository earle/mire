(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.object :as object]
            [mire.commands :as commands]
            [mire.player :as player]))

(defn move
  "\"♬ We gotta get out of this place... ♪\" Give a direction."
  [args]
  (dosync
   (let [direction (first args)
         target-name ((:exits @player/*current-room*) (keyword direction))
         target (@rooms/rooms target-name)]
     (if target
       (do
         (object/move-between-refs player/*name*
                            (:inhabitants @player/*current-room*)
                            (:inhabitants target))
         (rooms/tell-room @player/*current-room* (str player/*name* " went " (name direction) "."))

         (ref-set player/*current-room* target)
         (rooms/tell-room @player/*current-room* (str player/*name* " arrived."))
         (commands/execute "look"))

       "You can't go that way."))))
