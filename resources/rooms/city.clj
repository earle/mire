[{ :name "start"
   :desc "You are in a round room with a pillar in the middle."
   :exits {:north :closet :south :hallway}
   :items [:fountain]}

 { :name "closet"
   :desc "You are in a cramped closet."
   :exits {:south :start}
   :items [:battle-axe :trunk]}

 { :name "hallway"
   :desc "You are in a long, low-lit hallway that turns to the east."
   :exits {:north :start :east :promenade}}

 { :name "promenade"
   :desc "The promenade stretches out before you."
   :exits {:west :hallway :east :forest}}]
