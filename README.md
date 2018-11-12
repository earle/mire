# Mire

Hacking on this for fun -- originally forked from https://github.com/technomancy/mire

## Usage

Install [Leiningen](http://leiningen.org) if you haven't already:

    $ curl -O ~/bin/lein http://github.com/technomancy/leiningen/raw/stable/bin/lein
    $ chmod 755 bin/lein

Then do `lein run` inside the Mire directory to launch the Mire
server. Then players can connect by telnetting to port 3333.

## Design

Rooms and Items are defined in files in `resources`. Rooms are loaded into the
`@rooms/rooms` reference. Items are loaded into `@items/all-items`
and individual instances of items are cloned into `@items/items`.

## Commands

In game commands are defined in `resources/commands` with each command having
it's own file and loaded in the `user` namespace. A command returns a string
which is output to the users stream.

Command aliases are temporarily defined in `src/mire/commands.clj`

Example command `grab`:

```Clojure
(defn grab
  "Pick something up."
  [args]
  (dosync
    (if (> (count args) 0)
      (let [thing (str/join " " args)]
        (if (rooms/room-contains? @player/*current-room* thing)
          (let [item (util/get-item-in-ref @player/*current-room* thing)
                name (items/item-name item)]
            (if (items/moveable? item)
              (do
                (util/move-between-refs item
                                        (:items @player/*current-room*)
                                        player/*inventory*)
                (rooms/tell-room @player/*current-room* (str player/*name* " picked up " name "."))
                (str "You picked up " name "."))
              (do
                (rooms/tell-room @player/*current-room* (str player/*name* " tried to pick up " name ", and failed."))
                (str "You can't pick up " name "."))))
          (if (= thing "all")
            (str/join "\n" (for [[k obj] (util/items-in-ref @player/*current-room*)] (grab [(:name obj)])))
            (str "There isn't any " thing " here."))))
      (str "What do you want to get?"))))
```

## Players

## Items

Items are defined inside of files in `resources/items`. Each file contains a
list of objects. Each Object has a `name` key which will be used as a `keyword`
in the combined database of items -- these must be unique. An item can have
aliases, and will render in rooms by it's `sdesc` field.

```Clojure
[
 { :name "dagger" :sdesc "a dagger"}
 { :name "battle-axe" :aliases [ "axe", "battle axe" ] :sdesc "a bronze battle axe"}
 { :name "fountain" :sdesc "a fountain" :moveable false}
]
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
