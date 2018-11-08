(ns user
  (:require [clojure.string :as str]
            [mire.player :as player]))

(defn echo
  "Test Command"
  [& args]
  (str "hello there, " player/*name*))
