# Mire

Hacking on this for fun -- originally forked from https://github.com/technomancy/mire

## Usage

Install [Leiningen](http://leiningen.org) if you haven't already:

    $ curl -O ~/bin/lein http://github.com/technomancy/leiningen/raw/stable/bin/lein
    $ chmod 755 bin/lein

Then do `lein run` inside the Mire directory to launch the Mire
server. Then players can connect by telnetting to port 3333.

## Design

There are four main types of in-game objects: _Rooms_, _Items_, _Mobs_, and _Players_.

### Rooms

Rooms are defined in files in `resources/rooms/`. Rooms are loaded into the
`@rooms/rooms` reference. Each file can contain multiple room objects so rooms can
be organized by specific areas. Rooms link to other rooms via keywords in the `exits`
map reference inside the room object. Rooms can contain items, and mobs both which
are cloned upon game startup and placed within the room.

```clojure
{ :name "start"
   :desc "You are in a round room with a pillar in the middle."
   :exits {:north :closet :south :hallway}
   :items [:fountain :dagger]
   :mobs [:guard :guard :guard :rat :rat]}

 { :name "closet"
   :desc "You are in a cramped closet."
   :exits {:south :start}
   :items [:dagger :trunk]}

 { :name "hallway"
   :desc "You are in a long, low-lit hallway that turns to the east."
   :exits {:north :start :east :promenade}}

 { :name "promenade"
   :desc "The promenade stretches out before you."
   :exits {:west :hallway :east :forest}}]
```

The `:inhabitants` keyword contains any players that are currently in the room.

### Items

Items are defined in files in `resources/items/`. Items are loaded into the
`@items/items-db` reference. Each file can contain multiple item objects so they can
be organized by specific types. Each item should have at a minimum a `:name`.

```Clojure
[{ :name "dagger" :sdesc "small dagger"}
 { :name "battle-axe" :aliases [ "axe" "battle axe" ] :sdesc "bronze battle axe"}
 { :name "trunk" :sdesc "large trunk" :moveable false :container true}]
```

Individual instances of items are cloned into `@items/items`. Each item is cloned given
a keyword based on the items name, and the current number of items in the game.

```Clojure
user=> (:dagger-0 @items/items)
{:name "dagger", :sdesc "small dagger", :id :dagger-0}
```

Item's can be containers to hold other items by setting the `:container` flag to true.

```Clojure
> look
You are in a cramped closet.
Exits: south.
You see a large trunk, and a small dagger.

> look in trunk
You see large trunk, which contains:
2 red roses.
```

### Mobs

Mobs are defined in files in `resources/mobs/`. Mobs are loaded into the
`@mobs/mobs` reference. Each file can contain multiple mob objects so mobs can
be organized as you see fit.

Mobs can be set to move around using the keyword `moves`, the value of which is
the chance that it moves (out of a 1000) during any given _Heartbeat_.

```Clojure
[{ :name "guard"
   :aliases ["city guard" "cop"]
   :sdesc "city guard"
   :items [:battle-axe]
   :moves 20},
 { :name "rat" :aliases [ "rat", "rodent"] :sdesc "small rat"}]
```

## Heartbeat

The game heartbeat function is the main event driver for game activity. The
heartbeat is set to run every 4 seconds by default.

Currently the heatbeat controls:

1.  Mob movement

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

## Wiz

To create an item, use `clone`.

```Clojure
> clone :dagger
You cloned {:id :dagger-2, :name "dagger", :sdesc "small dagger"}.
```

To inspect an item or player in the current room or your inventory: `inspect dagger`
or `inspect guard` &ndash; to inspect a specific item instance: `inspect :dagger-0`.  
To inspect everything in the room it's simply `inspect` &ndash; for everything
you are carrying it's `inspect inventory`.

```Clojure
> inspect axe
Carrying:
({:id :battle-axe-0,
  :aliases ["axe" "battle axe"],
  :name "battle-axe",
  :sdesc "bronze battle axe"})
```

To inspect everything in the room: `inspect`:

```clojure
> inspect
{:id :hallway,
 :desc "You are in a long, low-lit hallway that turns to the east.",
 :exits
   #object[clojure.lang.Ref 0x3b20c8f2 {:status :ready, :val {:north :start, :east :promenade}}],
 :file :city.clj,
 :inhabitants
   #object[clojure.lang.Ref 0x3115ee96 {:status :ready, :val #{"Alice"}}],
 :items ({:id :rose-3, :name "rose", :aliases ["red rose"], :sdesc "red rose"}),
 :mobs
   ({:id :guard-3,
     :name "guard",
     :file "basic.clj",
     :items ({:id :battle-axe-6,
              :name "battle-axe",
              :aliases ["axe" "battle axe"],
              :sdesc "bronze battle axe"}),
     :sdesc "city guard"})}
```

To modify the instance of an item:

```Clojure
> clone :dagger
You cloned {:id :dagger-2, :name "dagger", :sdesc "small dagger"}.
> alter :dagger-2 :sdesc "a magic dagger"
:dagger-2 {:name "dagger", :sdesc "a magic dagger", :id :dagger-2}
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
