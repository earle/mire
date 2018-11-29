(ns mire.mobs
  (:require [mire.items :as items]))

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
  "Clone an Item"
  [name]
  (if (valid-mob? name)
    (let [mob (mobs-db (keyword name))
          id (keyword (str "mob-" (count @mobs)))]
      (dosync
        (alter mobs conj { id (assoc mob :ID id)})
        id))
    (println "mobs/clone-mob: Can't find " name)))

(defn- create-mob
  "Create a mob from a object"
  [mobs file obj]
  (let [items (ref (or (into #{} (remove nil? (map items/clone-item (:items obj)))) #{}))
        mob {(keyword (:name obj)) (assoc obj :items items)}]
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
