(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.util :as util]
            [mire.commands :as commands]
            [mire.player :as player]))

(defn unfollow
  "Stop following someone."
  [args]
  (dosync)
  (if-let [who @(:following player/*player*)]
    (dosync
      (alter (:followers ((keyword who) @player/players)) disj player/*name*)
      (ref-set (:following player/*player*) nil)
      (player/tell-player who (str player/*name* " stopped following you."))
      (str "You stopped following " who "."))
    (str "You aren't following anyone.")))
