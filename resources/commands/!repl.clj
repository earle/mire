(ns user
  (:require [clojure.string :as str]
            [mire.items :as items]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [mire.commands :as commands]
            [mire.util :as util]))

(defn !repl
  "Run an sandboxed REPL...."
  [args]
  (binding [*ns* (ns-name 'mire.server)]
    (println "Type 'quit' to exit.")
    (print (str *ns*) "=> ")
    (flush)
    (loop [input (str/trim (read-line))]
      (when (not (util/in? ["quit" "exit" "done"] input))
        (if (> (count input) 0)
          (println "eval: '" input "'"))
        (.flush *err*)
        (print (str *ns*) "=> ")
        (flush)
        (recur (read-line))))
    (str "done.")))
