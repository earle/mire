(ns mire.commands
  (:require [clojure.string :as str]
            [mire.rooms :as rooms]
            [mire.object :as object]
            [mire.player :as player]))

(def commands (ref {}))

(defn load-command
  "Load command from files"
  [commands file]
  (let [filename (.getName file)
        command (first (str/split filename #"\."))]
    (println (str "Loading: '" command "' from: " (.getAbsolutePath file)))
    (load-file (.getAbsolutePath file))
    (conj commands { command (symbol command)})))

(defn load-commands
  "Load commands from file tree."
  [commands dir]
  (dosync
    (reduce load-command commands
           (.listFiles (java.io.File. dir)))))

(defn add-commands
  "Look through all the files in a dir for files and add
  them to the mire.commands/commands map."
  [dir]
  (dosync
    (alter commands load-commands dir)))

;; Command aliases
(def aliases { "n" "move north",
               "s" "move south",
               "e" "move east",
               "w" "move west",
               "get" "grab",
               "drop" "discard",
               "i" "inventory"
               "l" "look"})

;; Command handling
(defn execute
  "Execute a command that is passed to us."
  [input]
  (try (let [[command & args] (.split input " +")
             alias (aliases command)]
         (if alias
           (let [[alias-command & alias-args] (.split alias " +")]
             ;; is there args in this alias?
             (if alias-args
               ((resolve (symbol (str "user/" alias-command))) alias-args)
               ((resolve (symbol (str "user/" alias))) args)))
           (if (contains? @commands command)
             ((resolve (symbol (str "user/" command))) args)
             (if-not (str/blank? command)
               (str "You can't do that!")))))

       (catch Exception e
         (.printStackTrace e (new java.io.PrintWriter *err*))
         "Ooops! Something went terribly wrong.")))
