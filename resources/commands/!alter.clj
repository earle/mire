(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.util :as util]
            [mire.items :as items]))

;; Need to handle variations:
;;   alter axe :sdesc this is an axe
;;   alter :5 :sdesc this is an axe

(defn !alter
  "Alter the instance of an item"
  [args]
  (if (> (count args) 0)
    (let [thing (first args)
          cmd (rest args)]
      ;; Is this thing a keyword or the name of something in the room/inventory?
      (if-let [k (if (= (str/starts-with? thing ":"))
                   (keyword (str/replace thing ":" ""))
                   (first (util/get-local thing)))]
        ;; grab this item and update field to value
        (if-let [item (items/get-item k)]
          (if (< (count args) 3)
            (str k " " (pprint/write item :stream nil))

            (let [field (-> cmd first (str/replace ":" "") keyword)
                  value (read-string (str/join " " (next cmd)))]
              ;; update the item instance
              (dosync
                (rooms/tell-room player/*current-room* (str player/*name* " edited the " (items/item-name item) "."))
                (if (nil? value)
                  (str k " " (pprint/write (k (alter items/items assoc k (dissoc item field))) :stream nil))
                  (str k " " (pprint/write (k (alter items/items assoc-in [k field] value)) :stream nil))))))
          (str "There item " k " doesn't exist."))
        (str "There isn't a " thing " to alter.")))
    (str "What do you want to alter?")))
