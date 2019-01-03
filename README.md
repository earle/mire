# Mire

Hacking on this for fun -- originally forked from https://github.com/technomancy/mire

## TODO

-   Support nth item commands to reference a specific item: `get dagger 3`
-   An in-game editor (maybe: https://github.com/rohitpaulk/simple-editor) for editing objects
-   Redis backend -- store and re-load world state
-   User Accounts; login/signup/password reset
-   Item based commands
-   Character schemas
-   Combat

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
are cloned upon game startup and placed within the room. Rooms can also _generate_
mobs when there are players in the room using the `:generate` keyword described below
in _Mob Generation_.

`/resources/rooms/city.clj:`

```clojure
[{ :id :start
   :desc "You are in a round room with a pillar in the middle."
   :exits {:north :closet :south :hallway}
   :items [:fountain :dagger]
   :mobs [:guard :guard :guard :rat :rat]}

 { :id :closet
   :desc "You are in a cramped closet."
   :exits {:south :start}
   :items [:dagger :trunk]}

 { :id :hallway
   :desc "You are in a long, low-lit hallway that turns to the east."
   :exits {:north :start :east :promenade}}

 { :id :promnade
   :desc "The promenade stretches out before you."
   :exits {:west :hallway :east :forest}}]
```

#### Mob Generation

Rooms can generate mobs while `:inhabitants` contains at least one _Player_. Mob generation
is handled as part of the system _Heartbeat_. The `:generate` keyword takes a map describing
which mobs to generate, the rate at which they are generated (1000 sided dice roll), and the
maximum number that should be created in the room. Mobs will randomly leave the room after a
period of time, which is handled in _Heartbeat_ as well.

To generate mobs in a room:

```Clojure
[{:id :forest
  :desc "You are in the forest."
  :exits {:west :promenade}
  :generate {:rat {:max 3 :rate 45}
             :deer {:max 2 :rate 15}
             :raccoon {:max 3 :rate 45}}}]
```

#### Room Attributes

-   `:id`: the keyword to uniquely identify a room
-   `:area`: a string to name the area of the world a room is in (_calculated from filename_)
-   `:desc`: a description of the room
-   `:exits`: a map to provide links to adjoining rooms
-   `:inhabitants`: any players that are currently in the room
-   `:items`: items that should be cloned into this room when its initialized
-   `:mobs`: mobs that should be cloned into this room when its initialized
-   `:generate`: map describing mobs to generate in this room

### Items

Items are defined in files in `resources/items/`. Items are loaded into the
`@items/items-db` reference. Each file can contain multiple item objects so they can
be organized by specific categories. Each item should have at a minimum a `:name`.

`/resources/items/weapons.clj`

```Clojure
[{ :name "dagger" :sdesc "small dagger"}
 { :name "battle-axe" :aliases [ "axe" "battle axe" ] :sdesc "bronze battle axe"}]
```

Individual instances of items are cloned into `@items/items`. Upon cloning, each item
is given an `id` keyword based on the items name, and the current number of items in the game.

```Clojure
user=> (:dagger-0 @items/items)
{:name "dagger", :sdesc "small dagger", :id :dagger-0}
```

#### Item Attributes

-   `:id`: a keyword to uniquely identify a specific instance of an item (_generated upon cloning_) _required_
-   `:sdesc`: a short description of the item _required_
-   `:aliases`: a list of strings to access an instance of this item in gameplay
-   `:category`: a string to categorize an item (_calculated from filename_)
-   `:container`: boolean if this item can contain other Items
-   `:decay`: seconds that it takes for this item to decay &ndash; _corpses, food, etc_
-   `:name`: a string and unique name for this item
-   `:moveable`: boolean if this item can be picked up or not

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

`/resources/mobs/basic.clj`

```Clojure
[{ :name "guard"
   :aliases ["city guard" "cop"]
   :sdesc "city guard"
   :items [:battle-axe]
   :moves 20},
 { :name "rat" :aliases [ "rat", "rodent"] :sdesc "small rat"}]
```

#### Mob Attributes

-   `:id`: a keyword to uniquely identify a specific instance (_generated upon cloning_)
-   `:name`: a string and unique name for this mob
-   `:aliases`: a list of strings to access an instance of this mob in gameplay
-   `:category`: a string to categorize mobs (_calculated from filename_)
-   `:sdesc`: a short description of the mob
-   `:items`: a ref set of item instances carried by this mob
-   `:moves`: optional integer determining the rate this mob moves around

#### Mob corpses

When mobs are killed in game play, a corpse is created which contains the inventory
of the mob. Corpse decay is handled as part of _Heartbeat_.

```Clojure
(defn kill-mob
  "Kill a mob, creating a corpse"
  [k]
  (if-let [mob (mobs/get-mob k)]
    (let [room (mob :current-room)
          corpse (items/clone-item :corpse (room :id))]
      (dosync
        ;; Move items from Mob to corpse
        (ref-set (:items (items/get-item corpse)) @(:items mob))

        ;; Remove from :current-room, create a corpse in :current-room containing :items
        (alter (room :mobs) disj k)
        (alter items/items assoc-in [corpse :sdesc] (str (mobs/mob-name mob) " corpse"))
        (alter items/items assoc-in [corpse :aliases] [(str (:name mob) " corpse"), (str (mobs/mob-name mob) " corpse")])
        (alter (room :items) conj corpse)

        ;; inform the room the mob has died
        (rooms/tell-room @room (str "The " (mobs/mob-name mob) " has died from its wounds."))
        ;; remove mob instance from world
        (alter mobs/mobs dissoc k)))))
```

## Heartbeat

The game heartbeat function is the main event driver for game activity. The
heartbeat is set to run every 4 seconds by default.

Currently the _Heartbeat_ controls:

1.  Mob movement
2.  Mob Generation for rooms with Players, and periodic removal of those mobs.
3.  Item Decay (corpses, food, etc)

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
            (rooms/tell-others-in-room (str player/*name* " dropped a " name "."))
            (str "You dropped the " name ".")))
        (if (= thing "all")
          (str/join "\n" (for [[k obj] (util/items-in-ref player/*player*)] (discard [(:name obj)])))
          (str "You're not carrying a " thing "."))))
    (str "What do you want to drop?")))
```

## Wiz

### Cloning

To create an item, use `clone`. Cloning takes a keyword representing either an entry in `@items/items-db`
or `@mobs/mobs-db`, and instantiates an object into the `@items/items` or `@mobs/mobs` reference. The cloned
item is given a unique `:id` based on the name of the object, and the number of existing instances in play.

Cloning a _mob_ places it in the current room of the player, and cloning an _item_ places it in the inventory
of the player.

```Clojure
> clone :dagger
You cloned {:id :dagger-2, :name "dagger", :sdesc "small dagger"}.
```

To inspect an item, mob, or player in the current room or your inventory: `inspect dagger`
or `inspect guard` &ndash; to inspect a specific item instance: `inspect :dagger-0`.  
To inspect everything in the room it's simply `inspect` &ndash; for everything
you are carrying it's `inspect inventory`.

### Inspecting

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
 :area "city"
 :inhabitants
   #object[clojure.lang.Ref 0x3115ee96 {:status :ready, :val #{"Alice"}}],
 :items ({:id :rose-3, :name "rose", :aliases ["red rose"], :sdesc "red rose"}),
 :mobs
   ({:id :guard-3,
     :name "guard",
     :category "basic",
     :items ({:id :battle-axe-6,
              :name "battle-axe",
              :aliases ["axe" "battle axe"],
              :sdesc "bronze battle axe"}),
     :sdesc "city guard"})}
```

### Modifying instances

To modify the instance of an object, use `alter`.

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
