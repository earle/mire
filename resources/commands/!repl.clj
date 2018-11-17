(ns user
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [mire.items :as items]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [mire.commands :as commands]
            [mire.util :as util]
            [nrepl.core :as nrepl]
            [reply.main :as reply]))

(defn !repl
  "Launch a REPL"
  [args]
  (binding [*in* (io/input-stream player/*input-stream*)
            *err* *out*]

    (let [options {:skip-default-init false,
                   :color true,
                   :standalone true,
                   :help false}]

      (reply/launch options))))
