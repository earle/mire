(defproject mire "0.13.1"
  :description "A multiuser text adventure game/learning project."
  :main ^:skip-aot mire.server
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [nrepl "0.4.5"]
                 [proto-repl "0.3.1"]
                 [reply "0.4.3"]
                 [server-socket "1.0.0"]
                 [zprint "0.4.13"]])
