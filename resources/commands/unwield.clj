(ns user
  (:require [clojure.string :as str]
            [mire.util :as util]
            [mire.rooms :as rooms]
            [mire.items :as items]
            [mire.player :as player]))

(defn unwield
  "Unwield a weapon"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      ;; Is this thing in the players inventory?
      (if (util/carrying? thing)
        (let [id (util/find-item-in-ref player/*player* thing)
              item (items/get-item id)]
          ;; is this a weapon?
          (if (items/wielding? item)
            (if (not (items/cursed? item))
              (do
                (dosync
                  (alter items/items assoc-in [id :wielding] false))
                (let [name (items/item-name (items/get-item id))]
                  (rooms/tell-others-in-room (str player/*name* " unwielded a " name "."))
                  (str "You stop wielding the " name ".")))
              (str "You can't, it's cursed."))
            (str "You aren't wielding the " (items/item-name item) ".")))
        (str "You're not carrying a " thing ".")))
    (str "What do you want to unwield?")))
