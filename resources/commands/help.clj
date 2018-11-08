(ns user
  (:require [clojure.string :as str]))

(defn help
  "Show available commands and what they do."
  [& args]
  (str/join "\n" (map #(str (key %) ": " (:doc (meta (val %))))
                      (dissoc (ns-publics 'user)
                              'execute 'commands))))
