(ns user
  (:require [clojure.string :as str]
            [mire.items :as items]
            [mire.player :as player]
            [mire.util :as util]))

(defn inventory
  "See what you've got."
  [args]
  (str "You are carrying:\n"
    (if (> (count @player/*inventory*) 0)
       (str/join "\n"
         ;;(map util/count-and-pluralize (frequencies (map items/item-name (seq @player/*inventory*)))))
         (->> @player/*inventory*
             seq
             (map items/get-item)
             (map items/item-name)
             frequencies
             (map util/count-and-pluralize)))
       (str "nothing."))))
