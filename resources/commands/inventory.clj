(ns user
  (:require [clojure.string :as str]
            [mire.player :as player]))

(defn inventory
  "See what you've got."
  [args]
  (str "You are carrying:\n"
       (str/join "\n" (seq @player/*inventory*))))
