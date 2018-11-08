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


;; Command functions

(defn help
  "Show available commands and what they do."
  []
  (str/join "\n" (map #(str (key %) ": " (:doc (meta (val %))))
                      (dissoc (ns-publics 'mire.commands)
                              'execute 'commands))))
;; Command data
;;(def new_commands {"move" move,})
                   ;;"n" (fn [] (move :north)),
                   ;;"north" (fn [] (move :north)),
                   ;;"s" (fn [] (move :south)),
                   ;;"south" (fn [] (move :south)),
                   ;;"e" (fn [] (move :east)),
                   ;;"east" (fn [] (move :east)),
                   ;;"w" (fn [] (move :west)),
                   ;;"west" (fn [] (move :west)),
                   ;;"grab" grab
                   ;;"get" grab
                   ;;"discard" discard
                   ;;"drop" discard
                   ;;"inventory" inventory
                   ;; "i" inventory
                   ;;"detect" detect
                   ;;"look" look
                   ;;"l" look
                   ;;"say" say
                   ;;"help" help})

;; Command handling
(defn execute
  "Execute a command that is passed to us."
  [input]
  (try (let [[command & args] (.split input " +")]
         (if (contains? @commands command)
           ((resolve (symbol (str "user/" command))) args)
           ;;(apply (@commands command) args)
           (if-not (str/blank? command)
             (str "You can't do that!"))))

       (catch Exception e
         (.printStackTrace e (new java.io.PrintWriter *err*))
         "Ooops! Something went terribly wrong.")))
