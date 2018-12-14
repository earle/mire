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

 { :id :promenade
   :desc "The promenade stretches out before you."
   :exits {:west :hallway :east :forest}}]
