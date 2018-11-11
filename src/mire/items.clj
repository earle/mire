(ns mire.items)

(def all-items (ref {}))
(def items (ref {}))

(defn item-name
  "Get the display name of an item"
  [item]
  (:sdesc (item @items)))

(defn valid-item?
  "Is this a valid item?"
  [thing]
  (all-items (keyword thing)))

(defn clone-item
  "Clone an Item"
  [thing]
  (if (valid-item? thing)
    (let [item (all-items (keyword thing))
           k (keyword (str (count @items)))]
      (dosync
        (alter items conj { k item})
        (keyword k)))
    (println "Can't find " thing)))

(defn- create-item
  "Create an item from a object"
  [db file obj]
  (conj db {(keyword (:name obj)) obj}))

(defn- load-item
  "Load a list of item objects from a file."
  [db file]
  (println "Loading Items from: " (.getAbsolutePath file))
  (let [objs (read-string (slurp (.getAbsolutePath file)))]
    (into {} (map #(create-item db file %) objs))))

(defn- load-items
  "Given a dir, return a map with an entry corresponding to each file
  in it. Files should be lists of maps containing room data."
  [db dir]
  (dosync
   (reduce load-item db (-> dir
                               java.io.File.
                               .listFiles))))

(defn add-items
  "Look through all the files in a dir for files describing items and add
  them to the mire.items/items map."
  [dir]
  (dosync
   (alter all-items load-items dir)))
