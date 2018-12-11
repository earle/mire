(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pprint]
            [mire.items :as items]
            [mire.mobs :as mobs]
            [mire.util :as util]))


;; alter :dagger-3 :sdesc this is an axe

(defn !alter
  "Alter the instance of an item or mob"
  [args]
  (if (> (count args) 0)
    (let [thing (first args)
          cmd (rest args)]

      ;; Is this thing a keyword or the name of something in the room/inventory?
      (if-let [k (if (= (str/starts-with? thing ":"))
                   (keyword (str/replace thing ":" ""))
                   (first (util/get-local thing)))]

        ;; is this an item?
        (if-let [item (items/get-item k)]
          (if (< (count args) 3)
            (str k " " (pprint/write item :stream nil))

            (let [field (-> cmd first (str/replace ":" "") keyword)
                  value (read-string (str/join " " (next cmd)))]
              ;; update the item instance
              (dosync
                (rooms/tell-others-in-room (str player/*name* " edited the " (items/item-name item) "."))
                (if (nil? value)
                  (str k " " (pprint/write (k (alter items/items assoc k (dissoc item field))) :stream nil))
                  (str k " " (pprint/write (k (alter items/items assoc-in [k field] value)) :stream nil))))))

          ;; is this a mob?
          (if-let [mob (mobs/get-mob k)]
            (if (< (count args) 3)
              (str k " " (pprint/write mob :stream nil))

              (let [field (-> cmd first (str/replace ":" "") keyword)
                    value (read-string (str/join " " (next cmd)))]
                ;; update the mob instance
                (dosync
                  (rooms/tell-others-in-room (str player/*name* " edited the " (mobs/mob-name mob) "."))
                  (if (nil? value)
                    (str k " " (pprint/write (k (alter mobs/mobs assoc k (dissoc mob field))) :stream nil))
                    (str k " " (pprint/write (k (alter mobs/mobs assoc-in [k field] value)) :stream nil))))))

            (str "The keyword " k " doesn't exist.")))

        (str "There isn't a " thing " to alter.")))

    (str "What do you want to alter?")))
