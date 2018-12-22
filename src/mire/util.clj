(ns mire.util
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.commands :as commands]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.player :as player]
            [mire.rooms :as rooms]
            [zprint.core :as zp]))


(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn move-between-refs
  "Move instance of obj between from and to. Must call in a transaction."
  [obj from to]
  (alter from disj obj)
  (alter to conj obj))

(defn count-and-pluralize
  "Count and pluralize for display"
  [frequency]
  (let [[name number] frequency]
    (if (> number 1)
      (str number " " name "s")
      (str "a " name))))

(defn comma-and-period
  "a one, 4 twos, and a three."
  [things]
  (let [ids (frequencies things)]
    (if (> (count ids) 1)
      (str (str/join ", " (map count-and-pluralize (butlast ids)))
          ", and " (count-and-pluralize (last ids)) ".")
      (str (count-and-pluralize (first ids)) "."))))

(defn items-in-ref
  "Items in this object"
  [obj]
  (select-keys @items/items @(:items obj)))

(defn find-items-in-ref
  "Find items by name or alias in an obj"
  [id thing]
  ;;(println "find-items-in-ref: " (items-in-ref obj))
  (for [[k i] (items-in-ref id) :when (or (= thing (:name i)) (in? (:aliases i) thing))] k))

(defn find-item-in-ref
  "Get the first item in obj by name"
  [id thing]
  (keyword (first (find-items-in-ref id thing))))

(defn mobs-in-room
  "Mobs in this room"
  [room]
  (select-keys @mobs/mobs @(:mobs room)))

(defn find-mobs-in-room
  "Find mobs by name or alias in a room"
  [room name]
  ;;(println "find-mobs-in-room: " (items-in-ref obj))
  (for [[k i] (mobs-in-room room) :when (or (= name (:name i)) (in? (:aliases i) name))] k))

(defn find-mob-in-room
  "Find the first mob in a room bny name"
  [room name]
  (keyword (first (find-mobs-in-room room name))))

(defn ref-contains?
  "Does an ref's :items contain an object by this name?"
  [id thing]
  (> (count (find-items-in-ref id thing)) 0))

(defn carrying?
   "Is this player carrying something?"
  [thing]
  (ref-contains? player/*player* thing))
  ;;(> (count (find-in-inventory thing)) 0))

(defn room-contains?
  "Does this room contain something?"
  [room thing]
  (ref-contains? room thing))

(defn get-local
  "Get an item from player inventory or current room"
  [thing]
  (if (carrying? thing)
    [(find-item-in-ref player/*player* thing) player/*player*]
    (if (room-contains? @player/*current-room* thing)
      [(find-item-in-ref @player/*current-room* thing) @player/*current-room*])))

(defn flatten-items
  [obj]
  (if (:items obj)
    (assoc obj :items (map #(items/get-item %) @(:items obj)))
    obj))

(defn flatten-mobs
  [obj]
  (if (:mobs obj)
    (assoc obj :mobs (map #(flatten-items (mobs/get-mob %)) @(:mobs obj)))
    obj))

(defn inspect-object
  "Inspect an Object"
  [obj]
  (zp/zprint-str (flatten-items (flatten-mobs obj)) {:map {:key-order [:id :name]}}))

(defn move-player
  "Move a player from one Room to another"
  [p from to direction]
  (dosync
    ; move this player, inform the previous room and new room
    (move-between-refs p (from :inhabitants) (to :inhabitants))
    (rooms/tell-room @from (str p " went " (name direction) ".") p)
    (ref-set from to)
    (rooms/tell-room to (str p " arrived.") p)
    (player/tell-player p (commands/execute "look"))))


(defn kill-mob
  "Kill a mob, creating a corpse"
  [k]
  (if-let [mob (mobs/get-mob k)]
    (let [room (mob :current-room)
          corpse (items/clone-item :corpse (room :id))]
      (dosync
        ;; Move items from Mob to corpse
        (ref-set (:items (items/get-item corpse)) @(:items mob))
        ;; Remove from :current-room, create a corpse in :current-room containing :items
        (alter (room :mobs) disj k)
        (alter items/items assoc-in [corpse :sdesc] (str (mobs/mob-name mob) " corpse"))
        (alter items/items assoc-in [corpse :aliases] [(str (:name mob) " corpse"), (str (mobs/mob-name mob) " corpse")])
        (alter (room :items) conj corpse)
        ;; inform the room the mob has died
        (rooms/tell-room @room (str "The " (mobs/mob-name mob) " has died from its wounds."))
        ;; remove mob instance from world
        (alter mobs/mobs dissoc k)))))

(defn destroy
  "Destroy Something"
  [k]
  ;; Is this an Item?
  (if-let [item (items/get-item k)]
    (do
      ;; Remove any children
      (if-let [children (:items item)]
        (doseq [obj @children]
          (destroy obj)))

      ;; Remove from any Player, Room, Mob, or other Item (container)
      (if-let [parent @(item :parent)]
        (doseq [obj (remove nil? [(items/get-item parent)
                                  (mobs/get-mob parent)
                                  (player/players (keyword parent))
                                  (rooms/rooms parent)])]
          (items/remove-item obj k)))
      (dosync
        (alter items/items dissoc k)
        (log/debug "destroyed: " k))
      true)
    false))

(defn move-mob
  "Move a mob in a direction (must exist)"
  [m direction]
  (dosync
    (let [mob (@mobs/mobs m)
          current-room @(mob :current-room)
          from (mob :current-room)
          to (rooms/rooms (direction @(from :exits)))]
      (log/debug "move-mob:" m "from" (:id current-room) direction "to" (to :id))
      (move-between-refs m (from :mobs) (to :mobs))
      (rooms/tell-room @from (str "The " (mobs/mob-name mob) " went " (name direction) "."))
      (ref-set (:current-room mob) to)
      (rooms/tell-room to (str "A " (mobs/mob-name mob) " arrived.")))))

(defn mob-walk
  "Move a mob to a random exit"
  [m]
  (if-let [mob (@mobs/mobs m)]
    (if-let [room @(mob :current-room)]
      (move-mob m (rand-nth (keys @(room :exits)))))))
