# Mire

Hacking on this for fun -- originally forked from https://github.com/technomancy/mire

## Usage

Install [Leiningen](http://leiningen.org) if you haven't already:

    $ curl -O ~/bin/lein http://github.com/technomancy/leiningen/raw/stable/bin/lein
    $ chmod 755 bin/lein

Then do `lein run` inside the Mire directory to launch the Mire
server. Then players can connect by telnetting to port 3333.

## Commands

In game commands are defined in `resources/commands` with each command having
it's own file and loaded in the `user` namespace.

Command aliases are temporarily defined in `src/mire/commands.clj`

Example command `grab`:

```Clojure
(defn grab
  "Pick something up."
  [args]
  (dosync
    (let [thing (first args)]
      (if (rooms/room-contains? @player/*current-room* thing)
        (do (object/move-between-refs (keyword thing)
                               (:items @player/*current-room*)
                               player/*inventory*)
            (rooms/tell-room @player/*current-room* (str player/*name* " picked up a " thing "."))
            (str "You picked up the " thing "."))
        (str "There isn't any " thing " here.")))))
```

## Rooms

Rooms are defined as objects inside of files in `resources/rooms`. Rooms are
keyed by their `:name` property across all files. Example room file content:

```Clojure
[{ :name "start"
   :desc "You are in a round room with a pillar in the middle."
   :exits { :north :closet :south :hallway}}

 { :name "closet"
   :desc "You are in a cramped closet."
   :exits {:south :start}
   :items #{:key}}

 { :name "hallway"
   :desc "You are in a long, low-lit hallway that turns to the east."
   :items #{:detector}
   :exits {:north :start :east :promenade}}

 { :name "promenade"
   :desc "The promenade stretches out before you."
   :exits {:west :hallway :east :forest}
   :items #{:bunny :turtle}}

 { :name "start"
   :desc "You wake up and find yourself in a round room with a pillar in the middle."
   :exits { :north :closet :south :hallway}
 }]
```

## Motivation

This code is not that interesting as a game, though I suppose
something fun could be built using it as a base. The primary purpose
of it is as a demonstration of how to build a simple multithreaded app
in Clojure.

Mire is built up step-by-step, where each step introduces one or two
small yet key Clojure principles and builds on the last step. The
steps each exist in separate git branches. To get the most out of
reading Mire, you should start reading in the branch called
[step-01-echo-server](http://github.com/technomancy/mire/tree/01-echo-server)
and continue from there.

While you can learn from Mire on its own, it has been written
specifically for the [PeepCode screencast on
Clojure](http://peepcode.com/products/functional-programming-with-clojure).
A [blog post](http://technomancy.us/136) steps through the codebase
and shows how to make minor updates for a more recent version of Clojure.

Copyright © 2009-2012 Phil Hagelberg
Licensed under the same terms as Clojure.
