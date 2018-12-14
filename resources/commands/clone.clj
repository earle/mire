(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.player :as player]))

(defn clone
  "Clone an item"
  [args]
  (if (> (count args) 0)
    (let [thing (str/replace-first (str/join " " args) ":" "")
          k (keyword thing)]
      (if-let [id (items/clone-item k)]
        (dosync
          (alter player/*inventory* conj id)
          (rooms/tell-others-in-room (str player/*name* " cloned a "
                                       (items/item-name (items/get-item id)) "."))
          (str "You cloned " (util/inspect-object (items/get-item id)) "."))
        (if-let [id (mobs/clone-mob k (player/*current-room* :id))]
          (dosync

            (alter (player/*current-room* :mobs) conj id)
            (rooms/tell-others-in-room (str player/*name* " cloned a "
                                         (mobs/mob-name (mobs/get-mob id)) "."))
            (str "You cloned a " (util/inspect-object (mobs/get-mob id)) "."))
          (str "Specify a valid thing to clone."))))
    (str "Usage: 'clone :keyword'")))
