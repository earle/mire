(ns mire.items)

(def all-items (ref {}))
(def items (ref {}))

(defn get-item
  "Get an item"
  [item]
  (item @items))

(defn container?
  "Is this item a container of other items?"
  [k]
  (if-let [item (get-item k)]
    (if (contains? item :container)
      (:container item)
      false)))

(defn contents
  "Contents of this item"
  [k]
  (if-let [item (get-item k)]
    (if-let [objs @(:items item)]
      objs)))

(defn moveable?
  "Is this item moveable?"
  [k]
  (if-let [item (get-item k)]
    (if (contains? item :moveable)
      (:moveable item)
      true)))

(defn item-name
  "Get the short description  of an item if it exists"
  [item]
  (if-let [item (item @items)]
    (if (contains? item :sdesc)
      (:sdesc item)
      (:name item))
    (:name item)))

(defn item-desc
  "Get the description of an item, if it exists"
  [item]
  (if-let [item (item @items)]
    (if (contains? item :desc)
      (:desc item)
      (item-name item))
    item))

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
    (println "items/clone-item: Can't find " thing)))

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
   (alter all-items load-items dir)))
