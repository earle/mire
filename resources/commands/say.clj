(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn say
  "Say something out loud so everyone in the room can hear."
  [args]
  (let [message (str/join " " args)]
    (rooms/tell-others-in-room (str player/*name* " says: " message))
    (str "You said: " message)))
