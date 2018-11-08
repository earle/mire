(ns user
  (:require [clojure.string :as str]
            [mire.object :as object]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn detect
  "If you have the detector, you can see which room an item is in."
  [args]
  (let [item (first args)]
    (if (@player/*inventory* :detector)
      (if-let [room (first (filter #((:items %) (keyword item))
                                   (vals @rooms/rooms)))]
        (str item " is in " (:name room))
        (str item " is not in any room."))
      "You need to be carrying the detector for that.")))
