(ns user
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.player :as player]))

(defn emote
  "Emote someting, optionally at someone"
  [args]
  (if (> (count args) 2)
    (let [emote (first args)
          prep (first (rest args))
          target (str/join " " (rest (rest args)))]
      (if (not= target "")
        (let [name (str/capitalize target)]
          (if (contains? @(:inhabitants @player/*current-room*) name)
            (if (= name player/*name*)
              (do
                (rooms/tell-room @player/*current-room*
                  (str player/*name* " " emote "s " prep " themself."))
                (str "You " emote " " prep " yourself."))
              (do
                (rooms/tell-room @player/*current-room* (str player/*name* " " emote "s " prep " " name ".") name)
                (player/tell-player name (str player/*name* " " emote "s " prep " you."))
                (str "You " emote " " prep " " name ".")))
            (str name " isn't here.")))))
    (if (> (count args) 0)
      (let [emote (first args)]
        (rooms/tell-room @player/*current-room* (str player/*name* " " emote "s."))
        (str "You " emote "."))
      (str "What do you want to emote?"))))
