(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.util :as util]
            [mire.player :as player]))

;; Need to handle:
;; inspect -- inspects the current room and inventory
;; inspect room -- inspects the current room and inventory
;; inspect axe -- inspects every matching item in room and inventory
;; inspect Bob -- inspects person and their inventory
;; inspect :4 -- inspects a specific item instance
;; inspect :battle-axe -- inspects keyword in items-db

(defn inspect
  "Inspect an object"
  [args]
  (if (= (count args) 0)
    ;; inspect the current room
    (util/inspect-object @player/*current-room*)

    ;; is this a keyword?
    (if (str/starts-with? (first args) ":")
      (let [k (keyword (str/replace (first args) ":" ""))]
        ;; is this keyword an item instance?
        (if-let [item (items/get-item k)]
          (util/inspect-object item)
          ;; is this keyword from the item database?
          (if-let [item (k @items/items-db)]
            (util/inspect-object item)
            ;; is this keyword a mob instance?
            (if-let [mob (mobs/get-mob k)]
              (util/inspect-object mob)
              (str "Can't find a " k " to inspect.")))))

      ;; does this thing exist in the inventory or current room?
      (let [name (str/join " " args)
            carrying-ids (util/find-items-in-ref player/*player* name)
            inroom-ids (util/find-items-in-ref @player/*current-room* name)
            mob-ids (util/find-mobs-in-room @player/*current-room* name)]
        (if (or (> (count carrying-ids) 0) (> (count inroom-ids) 0) (> (count mob-ids) 0))
          (str
            (if (> (count mob-ids) 0)
              (str "Mobs:\n" (util/inspect-object (->> mob-ids (map mobs/get-mob))) "\n"))
            (if (> (count carrying-ids) 0)
              (str "Carrying:\n" (util/inspect-object (->> carrying-ids (map items/get-item))) "\n"))
            (if (> (count inroom-ids) 0)
              (str "In Room:\n" (util/inspect-object (->> inroom-ids (map items/get-item))) "\n")))

          ;; did we specify the current room explicitly?
          (if (= name "inventory")
            (str "Carrying:\n" (util/inspect-object (->> @player/*inventory* (map items/get-item))) "\n")
            ;; Is this a player?
            (if-let [p ((keyword (str/capitalize name)) @player/players)]
              (util/inspect-object p)
              (str "There isnt a " name " here."))))))))
