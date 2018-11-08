(ns user
  (:require [clojure.string :as str]))

(defn help
  "Show available commands and what they do."
  [& args]
  (str "Commands:\n  "
    (str/join "\n  "
      (map #(str (key %) ": " (:doc (meta (val %))))
        (ns-publics 'user)))))
