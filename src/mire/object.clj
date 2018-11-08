(ns mire.object
  (:require [clojure.string :as str]
            [mire.player :as player]
            [mire.rooms :as rooms]))

(defn move-between-refs
  "Move one instance of obj between from and to. Must call in a transaction."
  [obj from to]
  (alter from disj obj)
  (alter to conj obj))
