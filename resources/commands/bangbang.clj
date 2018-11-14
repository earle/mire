(ns user
  (:require [clojure.string :as str]
            [mire.commands :as commands]))

(defn bangbang
  "!!; Execute the last command again"
  [args]
  (if-let [last-command @(:last-command player/*player*)]
    ;; prevent an infinite loop
    (do
      (commands/set-last-command! last-command)
      (str (commands/execute last-command)))))
