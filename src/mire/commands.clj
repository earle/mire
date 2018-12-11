(ns mire.commands
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [mire.player :as player]))

;; Commands (dynamically loaded from individual files)
(def commands (ref {}))

;; Aliases for commands.  This needs to live somewhere else eventually
(def aliases { "n" "move north",
               "north" "move north",
               "s" "move south",
               "south" "move south"
               "e" "move east",
               "east" "move east"
               "w" "move west",
               "west" "move west"
               "get" "grab",
               "drop" "discard",
               "i" "inventory"
               "l" "look"
               "alter" "!alter"})


(defn load-command
  "Load command from files"
  [commands file]
  (let [filename (.getName file)
        command (first (str/split filename #"\."))]
    (log/debug "Loading Command:" command "from:" (.getAbsolutePath file))
    (-> file
        .getAbsolutePath
        load-file)
    (conj commands { command (symbol (str "user/" command))})))

(defn load-commands
  "Load commands from file tree."
  [commands dir]
  (dosync
    (reduce load-command commands
      (-> dir
          java.io.File.
          .listFiles))))

(defn add-commands
  "Look through all the files in a dir for files and add
  them to the available commands map."
  [dir]
  (dosync
    (alter commands load-commands dir)))

;; Last Command -- prevent infinite recursion
(defn set-last-command!
  "Update last command"
  [cmd]
  (if (and (not (str/blank? cmd)) (not= "!!" (str/lower-case cmd)))
    (dosync
      (ref-set (:last-command player/*player*) cmd))))

;; Command handling
(defn execute
  "Execute a command that is passed to us."
  [input]
  (try (let [[command & args] (.split input " +")
             alias (aliases command)]
         ;; save last command
         (set-last-command! input)

         (if alias
           (let [[alias-command & alias-args] (.split alias " +")]
             ;; is there args in this alias?
             (if alias-args
               ((resolve (@commands alias-command)) alias-args)
               ((resolve (@commands alias)) args)))
           (if (contains? @commands command)
             ((resolve (@commands command)) args)
             (if-not (str/blank? command)
               (str "You can't do that!")))))

       (catch Exception e
         (.printStackTrace e (new java.io.PrintWriter *err*))
         "Ooops! Something went terribly wrong.")))
