(ns mire.mobs
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.items :as items]))

; All mobs that exist, and the database of items to mob instances from
(def mobs (ref {}))
(def mobs-db (ref {}))

(defn get-mob
  "Get a mob.  We sort map keys so inspect looks nice."
  [id]
  (if (contains? @mobs id)
    (into (sorted-map) (id @mobs))
    nil))

(defn generate-id
  "Generate IDs for Mobs"
  [obj]
  (let [n (atom 0)
        name (str/replace-first obj ":" "")
        k (atom (keyword (str name "-" @n)))]
    (while (get-mob @k)
      (do
        (swap! n inc)
        (reset! k (keyword (str name "-" @n)))))
    @k))

(defn mob-name
  "Get the short description of a mob if it exists"
  [mob]
  (if (contains? mob :sdesc)
    (:sdesc mob)
    (:name mob)))

(defn mob-desc
  "Get the description of a mob, if it exists"
  [mob]
  (if (contains? mob :desc)
    (:desc mob)
    (mob-name mob)))

(defn valid-mob?
  "Is this a valid mob?"
  [name]
  (mobs-db (keyword name)))

(defn clone-mob
  "Clone a Mob into a Room"
  [k room]
  (if-let [mob (mobs-db k)]
    (let [items (ref (or (into #{} (remove nil? (map items/clone-item (:items mob)))) #{}))
          id (generate-id k)]
      (dosync
        (alter mobs conj { id (assoc mob :id id :created (quot (System/currentTimeMillis) 1000)
                                         :current-room (ref room) :items items)})
        id))))

(defn- create-mob
  "Create a mob database entry from a object"
  [mobs file obj]
  (let [mob {(keyword (:name obj)) (assoc obj :category (str/replace (.getName file) ".clj" ""))}]
    (conj mobs mob)))

(defn- load-mob
  "Load a list of mob objects from a file."
  [mobs file]
  (log/debug "Loading Mobs from: " (.getAbsolutePath file))
  (let [objs (read-string (slurp (.getAbsolutePath file)))]
    (into {} (map #(create-mob mobs file %) objs))))

(defn- load-mobs
  "Given a dir, return a map with an entry corresponding to each file
  in it. Files should be lists of maps containing room data."
  [mobs dir]
  (dosync
   (reduce load-mob mobs (-> dir
                             java.io.File.
                             .listFiles))))

(defn add-mobs
  "Look through all the files in a dir for files describing mobs and add
  them to the mire.mobs/mobs map."
  [dir]
  (dosync
   (alter mobs-db load-mobs dir)))
