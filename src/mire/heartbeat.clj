(ns mire.heartbeat
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.commands :as commands]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [mire.util :as util]))


(defn heartbeat
  "The main heartbeat background process."
  [interval]
  (log/info "heartbeat starting," (/ interval 1000) "second interval.")

  (while true
    (do
      ;; handle time sensitive tasks; combat, healing players, moving npcs, etc.
      (Thread/sleep interval)

      ;; move mobs
      (doseq [[k v] @mobs/mobs :when (:moves v)]
        (let [r (rand-int 1000)]
          (if (< r (:moves v))
            (util/mob-walk k)))))))
