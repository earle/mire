(ns mire.items
  (:require [clojure.string :as str]))

; All items that exist, and the database of items to clone instances from
(def items (ref {}))
(def items-db (ref {}))

(defn get-item
  "Get an item.  We sort map keys"
  [id]
  (if (contains? @items id)
    (into (sorted-map) (id @items))
    nil))

(defn container?
  "Is this item a container of other items?"
  [item]
  (if (contains? item :container)
    (:container item)
    false))

(defn contents
  "Contents of this item"
  [item]
  (if-let [objs @(:items item)]
    objs
    []))

(defn moveable?
  "Is this item moveable?"
  [item]
  (if (contains? item :moveable)
    (:moveable item)
    true))

(defn item-name
  "Get the short description  of an item if it exists"
  [item]
  (if (contains? item :sdesc)
    (:sdesc item)
    (:name item)))

(defn item-desc
  "Get the description of an item, if it exists"
  [item]
  (if (contains? item :desc)
    (:desc item)
    (item-name item)))

(defn valid-item?
  "Is this a valid item?"
  [thing]
  (items-db (keyword thing)))

(defn clone-item
  "Clone an Item"
  [k]
  (if-let [item (items-db k)]
    (let [name (str/replace-first k ":" "")
          id (keyword (str name "-" (count (filter #(= (:name item) (:name %)) (vals @items)))))]
      (dosync
        (alter items conj { id (assoc item :ID id)})
        id))
    (println "items/clone-item: Can't find " k)))

(defn- create-item
  "Create an item from a object"
  [items file obj]
  (if (:container obj)
    ;; If this is a container, clone default items into it
    ;; XXX: have to post-process this since the items references may not be created yet
    (conj items {(keyword (:name obj)) (assoc obj :items (ref #{}))})
    (conj items {(keyword (:name obj)) obj})))

(defn- load-item
  "Load a list of item objects from a file."
  [items file]
  (println "Loading Items from: " (.getAbsolutePath file))
  (let [objs (read-string (slurp (.getAbsolutePath file)))]
    (into {} (map #(create-item items file %) objs))))

(defn- load-items
  "Given a dir, return a map with an entry corresponding to each file
  in it. Files should be lists of maps containing room data."
  [items dir]
  (dosync
   (reduce load-item items (-> dir
                               java.io.File.
                               .listFiles))))

(defn add-items
  "Look through all the files in a dir for files describing items and add
  them to the mire.items/items map."
  [dir]
  (dosync
   (alter items-db load-items dir)))
