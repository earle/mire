(ns mire.rooms
  (:require [mire.player :as player]
            [mire.items :as items]))

;; state for all rooms
(def rooms (ref {}))

(defn- create-room
  "Create a room from a object"
  [rooms file obj]
  (let [items (ref (or (into #{} (remove nil? (map items/clone-item (:items obj)))) #{}))
        room {(keyword (:name obj)) {:name (keyword (:name obj))
                                     :file (keyword (.getName file))
                                     :desc (:desc obj)
                                     :exits (ref (:exits obj))
                                     :items items
                                     :inhabitants (ref #{})}}]
    (conj rooms room)))

(defn- load-room [rooms file]
  "Load a list of room objects from a file."
  (println "Loading Rooms from: " (.getAbsolutePath file))
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
    (alter rooms load-rooms dir)))

(defn others-in-room
  "Other people in the current room"
  []
  (disj @(:inhabitants @player/*current-room*) player/*name*))

(defn tell-room
  "Send a message to all inhabitants in a room; optionally exclude"
  [room message & exclude]
  (doseq [inhabitant (disj (others-in-room) (first exclude))]
    (binding [*out* (player/streams inhabitant)]
      (println message))))
