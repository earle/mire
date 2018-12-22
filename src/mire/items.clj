(ns mire.items
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]))

; All items that exist, and the database of items to clone instances from
(def items (ref {}))
(def items-db (ref {}))

(defn get-item
  "Get an item.  We sort map keys"
  [id]
  (if (contains? @items id)
    (id @items)
    nil))

(defn remove-item
  "Remove item k from object"
  [obj k]
  (dosync
    (ref-set (:items obj) (set (remove #{k} @(:items obj))))))

(defn generate-id
  "Generate IDs for Items"
  [obj]
  (let [n (atom 0)
        name (str/replace-first obj ":" "")
        k (atom (keyword (str name "-" @n)))]
    (while (get-item @k)
      (do
        (swap! n inc)
        (reset! k (keyword (str name "-" @n)))))
    @k))

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
  [k parent]
  (if-let [item (items-db k)]
    (let [id (generate-id k)]
      (dosync
        (if (:container item)
          (alter items conj { id (assoc item :id id
                                             :items (ref #{})
                                             :created (quot (System/currentTimeMillis) 1000)
                                             :parent (ref parent))})

          (alter items conj { id (assoc item :id id
                                             :created (quot (System/currentTimeMillis) 1000)
                                             :parent (ref parent))}))
        id))))

(defn- create-item
  "Create an item from a object"
  [items file obj]
  (let [id (keyword (:name obj))]
    (conj items {id (assoc obj :category (str/replace (.getName file) ".clj" ""))})))

(defn- load-item
  "Load a list of item objects from a file."
  [items file]
  (log/debug "Loading Items from: " (.getAbsolutePath file))
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
