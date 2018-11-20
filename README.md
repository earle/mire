# Mire

Hacking on this for fun -- originally forked from https://github.com/technomancy/mire

## Usage

Install [Leiningen](http://leiningen.org) if you haven't already:

    $ curl -O ~/bin/lein http://github.com/technomancy/leiningen/raw/stable/bin/lein
    $ chmod 755 bin/lein

Then do `lein run` inside the Mire directory to launch the Mire
server. Then players can connect by telnetting to port 3333.

## Design

Rooms and Items are defined in files in `resources/`. Rooms are loaded into the
`@rooms/rooms` reference. Items are loaded into `@items/items-db`
and individual instances of items are cloned into `@items/items`.

## Commands

In-game commands are defined in `resources/commands` with each command having
it's own file and loaded in the `user` namespace. A command returns a string
which is output to the user.

Command aliases are temporarily defined in `src/mire/commands.clj`

Example command `discard`:

```Clojure
(defn discard
  "Discard an item that you're carrying"
  [args]
  (if (> (count args) 0)
    (let [thing (str/join " " args)]
      (if (util/carrying? thing)
        (let [id (util/find-item-in-ref player/*player* thing)
              item (items/get-item id)
              name (items/item-name item)]
          (dosync
            (util/move-between-refs id
                                    player/*inventory*
                                    (:items @player/*current-room*))
            (rooms/tell-room @player/*current-room* (str player/*name* " dropped a " name "."))
            (str "You dropped the " name ".")))
        (if (= thing "all")
          (str/join "\n" (for [[k obj] (util/items-in-ref player/*player*)] (discard [(:name obj)])))
          (str "You're not carrying a " thing "."))))
    (str "What do you want to drop?")))
```

## Players

## Items

Items are defined inside of files in `resources/items/`. Each file contains a
list of objects. Each Object has a `name` key which will be used as a `keyword`
in the combined database of items -- these names should be unique. An item can have
aliases, and will render by it's `sdesc` field, or `name` if it doesnt exist.

If an item has `:container true` set then it can hold other items.

```Clojure
[
 { :name "dagger" :sdesc "a dagger"}
 { :name "battle-axe" :aliases [ "axe", "battle axe" ] :sdesc "a bronze battle axe"}
 { :name "fountain" :sdesc "a fountain" :moveable false}
]
```

## Rooms

Rooms are defined as objects inside of files in `resources/rooms`. Rooms are
keyed by their `:name` property across all files and have `:exits` to navigate
to other rooms. Example room file content:

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

## Wiz

To create an item, use `clone`.

```Clojure
> clone battle-axe
You cloned a bronze battle axe {:ID :10, :aliases ["axe" "battle axe"], :name "battle-axe", :sdesc "bronze battle axe"}
```

To inspect an item or player in the current room or your inventory: `inspect dagger` or `inspect Alice` &ndash; to inspect a specific item instance: `inspect :4`

```Clojure
> inspect axe
Carrying:
({:ID :10,
  :aliases ["axe" "battle axe"],
  :name "battle-axe",
  :sdesc "bronze battle axe"})
```

To inspect everything in the room: `inspect`:

```clojure
> inspect
{:ID :closet,
 :file :city.clj,
 :desc "You are in a cramped closet.",
 :exits #<Ref@799ac05e: {:south :start}>,
 :inhabitants #<Ref@2800c4f9: #{"Alice"}>,
 :items
 ({:ID :7, :aliases ["red rose"], :name "rose", :sdesc "red rose"}
  {:ID :2,
   :container true,
   :items
   ({:ID :1, :name "dagger", :sdesc "small dagger"}
    {:ID :5, :name "dagger", :sdesc "small dagger"}
    {:ID :3, :name "dagger", :sdesc "small dagger"}),
   :moveable false,
   :name "trunk",
   :sdesc "large trunk"})}
```

To modify the instance of an item:

```Clojure
> alter :3 :sdesc "a magic dagger"
:3 {:name "dagger", :sdesc "a magic dagger", :ID :3}
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
