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
    (pprint/write (items/inspect-item @player/*current-room*) :stream nil)

    ;; is this a keyword?
    (if (str/starts-with? (first args) ":")
      (let [k (keyword (str/replace (first args) ":" ""))]
        ;; is this keyword an item instance?
        (if-let [item (items/get-item k)]
          (pprint/write (items/inspect-item item) :stream nil)
          ;; is this keyword from the item database?
          (if-let [item (k @items/items-db)]
            (pprint/write (items/inspect-item item) :stream nil)
            (str "Can't find a " k " to inspect."))))

      ;; does this thing exist in the inventory or current room?
      (let [name (first args)
            carrying-ids (util/find-items-in-ref player/*player* name)
            inroom-ids (util/find-items-in-ref @player/*current-room* name)]
        (if (or (> (count carrying-ids) 0) (> (count inroom-ids) 0))
          (str
            (if (> (count carrying-ids) 0)
              (str "Carrying:\n" (pprint/write (->> carrying-ids (map items/get-item)) :stream nil) "\n"))
            (if (> (count inroom-ids) 0)
              (str "In Room:\n" (pprint/write (->> inroom-ids (map items/get-item)) :stream nil) "\n")))

          ;; did we specify the current room explicitly?
          (if (= name "room")
            (pprint/write (items/inspect-item @player/*current-room*) :stream nil)
            (if-let [p ((keyword (str/capitalize name)) @player/players)]
              (pprint/write (items/inspect-item p) :stream nil)
              (str "There isnt a " name " here."))))))))
