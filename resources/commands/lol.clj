(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn lol
  "Laugh out loud; optionally at someone"
  [args]
  (let [target (first args)]
    (if (nil? target)
      (do
        (rooms/tell-others-in-room (str player/*name* " laughs out loud."))
        (str "You laughed out loud."))
      (let [name (str/capitalize target)]
        (if (contains? @(:inhabitants @player/*current-room*) name)
          (if (= name player/*name*)
            (do
              (rooms/tell-others-in-room (str player/*name* " laughs at ")
                  (if (= "male" (:sex player/*player*)) (str "himself") (str "herself" )) ".")
              (str "You laugh at yourself."))
            (do
              (rooms/tell-room @player/*current-room* (str player/*name* " laughs at " name ".") #{name})
              (player/tell-player name (str player/*name* " laughs at you."))
              (str "You laughed at " name ".")))
          (str name " isn't here."))))))
