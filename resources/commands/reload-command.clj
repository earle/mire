(ns user
  (:require [clojure.string :as str]
            [mire.player :as player]))

(defn reload-command
   "Reload a command from file"
  [args]
  (let [command (first args)
        filename (str "resources/commands/" command ".clj")]
    (load-file filename)
    (str "Re-loaded: '" command "' from: " filename)))
