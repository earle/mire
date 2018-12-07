(ns mire.mobs
  (:require [clojure.string :as str]
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
  "Clone a Mob"
  [k]
  (if-let [mob (mobs-db k)]
    (let [name (str/replace-first k ":" "")
          items (ref (or (into #{} (remove nil? (map items/clone-item (:items mob)))) #{}))
          id (keyword (str name "-" (count (filter #(= (:name mob) (:name %)) (vals @mobs)))))]
      (dosync
        (alter mobs conj { id (assoc mob :id id :items items)})
        id))))

(defn- create-mob
  "Create a mob database entry from a object"
  [mobs file obj]
  (let [mob {(keyword (:name obj)) (assoc obj :file (.getName file))}]
    (conj mobs mob)))

(defn- load-mob
  "Load a list of mob objects from a file."
  [mobs file]
  (println "Loading Mobs from: " (.getAbsolutePath file))
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
