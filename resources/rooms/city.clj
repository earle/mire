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
   :exits { :north :closet :south :hallway}}]
 
