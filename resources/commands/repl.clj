(ns user
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [mire.commands :as commands]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [mire.util :as util]
            [nrepl.core :as nrepl]
            [reply.main :as reply]))

(defn repl
  "Launch a REPL"
  [args]
  (binding [*err* *out*]
    ; not sure why this shows the prompt twice
    (let [options {:skip-default-init false,
                   :color true,
                   :standalone true,
                   :help false
                   :output-stream (io/output-stream player/*output-stream*)
                   :input-stream (io/input-stream player/*input-stream*)}]

      (reply/launch options))))
