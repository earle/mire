(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.player :as player]))

(defn who
  "Who's online."
  [args]
  (str "Currently Online:\n" (pprint/write @player/players :stream nil) "\n"))
