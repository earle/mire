(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.util :as util]
            [mire.commands :as commands]
            [mire.player :as player]))

(defn follow
  "Start following someone."
  [args]
  (dosync)
  (if (> (count args) 0)
    (let [target (str/capitalize (first args))]
      (if-let [person ((keyword target) @player/players)]
        (if (contains? @(:followers person) player/*name*)
          (str "You are already following " target ", unfollow to stop.")
          (if (= target player/*name*)
            (str "You can't follow yourself!")
            (dosync
              ; if we're already following someone, stop, and remove from their followers list
              (if-let [who @(:following player/*player*)]
                (dosync
                  (alter (:followers ((keyword who) @player/players)) disj player/*name*)
                  (player/tell-player who (str player/*name* " stopped following you."))))
              ;; begin following someone, and inform them.
              (alter (:followers person) conj player/*name*)
              (ref-set (:following player/*player*) target)
              (player/tell-player target (str player/*name* " started following you."))
              (str "You start following " target "."))))
        (str "There isn't a " target " here to follow.")))
    (if (:following player/*player*)
      (str "You're currently following " @(:following player/*player*) ".")
      (str "Who do you want to follow?"))))
