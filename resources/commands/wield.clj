(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))

(defn wield
  "Wield a weapon"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      ;; Is this thing in the players inventory?
      (if (util/carrying? thing)
        (let [id (util/find-item-in-ref player/*player* thing)
              item (items/get-item id)
              name (items/item-name item)]
          ;; is this a weapon?
          (if (item :weapon)
            ;; Do we have a free hand?
            (if (or (and (item :two-handed) (player/both-hands-free?))
                    (player/one-hand-free?))
              (do
                (dosync
                  (alter items/items assoc-in [id :wielding] true))
                (rooms/tell-others-in-room (str player/*name* " wielded a " name "."))
                (str "You wield the " name "."))
              (str "You don't have a free hand."))
            (str "You can't wield the " name ".")))
        (str "You're not carrying a " thing ".")))
    (str "What do you want to wield?")))
