(ns mire.rooms
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.player :as player]
            [mire.items :as items]
            [mire.mobs :as mobs]))

;; state for all rooms
(def rooms (ref {}))

(defn- update-mobs-in-room
  "Update :current-room in Mobs after Room creation"
  []
  (doseq [[k v] @mobs/mobs]
    (let [r ((mobs/mobs k) :current-room)]
      (ref-set r (rooms @r)))))

(defn- create-room
  "Create a room from a object"
  [rooms file obj]
  (let [id (keyword (:id obj))
        items (ref (or (into #{} (remove nil? (map items/clone-item (:items obj)))) #{}))
        ;; clone the mobs, set their :current-room to the keyword of this room which
        ;; we will post-process to assign the room's ref after the rooms are added.
        mobs (ref (or (into #{} (remove nil? (map #(mobs/clone-mob % id) (:mobs obj)))) #{}))
        room {id {:id id
                  :area (str/replace (.getName file) ".clj" "")
                  :desc (:desc obj)
                  :exits (ref (:exits obj))
                  :inhabitants (ref #{})
                  :mobs mobs
                  :items items}}]
    (conj rooms room)))

(defn- load-room [rooms file]
  "Load a list of room objects from a file."
  (log/debug "Loading Rooms from: " (.getAbsolutePath file))
  (let [objs (read-string (slurp (.getAbsolutePath file)))]
    (into {} (map #(create-room rooms file %) objs))))

(defn- load-rooms
  "Given a dir, return a map with an entry corresponding to each file
  in it. Files should be lists of maps containing room data."
  [rooms dir]
  (dosync
    (reduce load-room rooms (-> dir
                                java.io.File.
                                 .listFiles))))

(defn add-rooms
  "Look through all the files in a dir for files describing rooms and add
  them to the mire.rooms/rooms map."
  [dir]
  (dosync
    (alter rooms load-rooms dir)
    ;; update mobs in room to set their :current-room properly
    (update-mobs-in-room)))


(defn others-in-room
  "Other people in the current room"
  []
  (disj @(:inhabitants @player/*current-room*) player/*name*))

(defn mobs-in-room
  "Mobs in the current room"
  []
  @(:mobs @player/*current-room*))

(defn tell-room
  "Send a message to all other inhabitants in a room; optionally exclude"
  [room message & exclude]
  (doseq [inhabitant (remove (set exclude) @(:inhabitants room))]
    (binding [*out* (player/streams inhabitant)]
      (println message))))

(defn tell-others-in-room
  "Send a message to everyone else in the room"
  [message]
  (tell-room @player/*current-room* message player/*name*))
