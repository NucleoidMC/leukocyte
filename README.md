# Leukocyte
Leukocyte is a simple world protection mod for Fabric providing optional integration with [player-roles](https://github.com/Gegy/player-roles/).

The basis of world protection with leukocyte is an "authority". An authority is responsible for applying specific rules
to players within it. An authority has a unique name, as well as a priority ("level"), and set of player exclusions. 

### creating authorities
To create an empty authority, run: `/protect add <name>`.  
Alternatively, an authority can be created to apply within a specific area like:
 - `/protect add <name> with universe` (applies universally)
 - `/protect add <name> with <dimension>` (applies within a specific dimension)
 - `/protect add <name> with <min> <max>` (applies within a cuboid between two block coordinates)

An authority can be later removed with `/protect remove <name>`.

### setting rules
Leukocyte provides various rules that can be applied within authorities. These rules are:
 - `break` controls whether players can break blocks
 - `place` controls whether players can place blocks
 - `block_drops` controls whether blocks drop items when broken
 - `interact_blocks` controls whether players can interact with blocks
 - `interact_entities` controls whether players can interact with entities
 - `interact` controls global interaction over blocks and entities
 - `attack` controls whether players can attack other entities
 - `pvp` controls whether players can attack other players
 - `spectate_entities` controls whether players can spectate entities from the entity's perspective in spectator mode
 - `portals` controls whether players can construct portals
 - `crafting` controls whether players can craft items
 - `fall_damage` controls whether players should receive fall damage
 - `hunger` controls whether players will become hungry
 - `activate_death_protection` controls whether entities should activate death protection items such as totems of undying to avoid dying
 - `throw_items` controls whether players can throw items from their inventory
 - `pickup_items` controls whether items can be picked up by players or other entities
 - `unstable_tnt` controls tnt automatically igniting when placed
 - `ignite_tnt` controls whether tnt can be ignited
 - `modify_flower_pots` controls whether plants can be placed in or removed from flower pots
 - `firework_explode` controls whether fireworks can explode instead of only fizzling out
 - `dispenser_activate` controls whether dispensers and droppers can be activated
 - `spawn_withers` controls whether withers can be summoned
 - `fire_tick` controls whether fire tick is enabled within the authority. Defaults to gamerule if not set
 - `fluid_flow` controls whether fluids flow
 - `ice_melt` controls whether ice and frosted ice melt
 - `snow_fall` controls whether snow can form on surfaces during snowfall
 - `coral_death` controls whether coral and coral fan blocks can die
 - `throw_projectiles` controls whether players can throw eggs, snowballs, or tridents
 - `shear_entities` controls whether entities such as sheep and mooshrooms can be sheared by players and dispensers
 - `block_random_tick` controls whether random ticks will apply to blocks
 - `fluid_random_tick` controls whether random ticks will apply to fluids
 
To set a rule as `allow` or `deny` on an authority, use `/protect set rule <authority> <rule> <result>`.  

For example: `/protect set example place deny` will disallow block placement within the authority named `example`.

### making shapes
Often, you may want to protect an area with a weird shape that is not just a simple box.
To achieve this, it is possible to combine multiple simple shapes into a more complex one which will be used to apply rules.

To start, run: `/protect shape start`. This will begin the construction of a shape.  
Next, to add primitives to this shape, run:
 - `/protect shape add universe` to add the universe into this shape
 - `/protect shape add <dimension>` to add a dimension into this shape
 - `/protect shape add <min> <max>` to add a cuboid between two block coordinates into this shape

These commands can be run multiple times to compose your shape.

Once you have finished composing a shape, you can run: `/protect shape finish <name> to <authority>`.
This will add a shape with the given name to the given authority.

This shape can be removed in the future with `/protect shape remove <name> from <authority>`.

### setting levels
When dealing with multiple authorities, you may want one to take priority over another. 
For example, you may want a global authority to disallow griefing everywhere *except* specific areas for building.

This is where levels come in: a level is just any number, where a higher level indicates higher priority, and a lower level indicates lower priority.
Given an authority with a level of -1, an overlapping authority with a level of 10 will override the rules of the first.

Levels can be set on an authority with: `/protect set level <authority> <level>`. 

### adding exclusions
It may not be desirable for the rules of an authority to apply to everyone.
In this case, it is possible to exclude specific players, or entire [roles](https://github.com/Gegy/player-roles/) from being affected by a given authority.

This is achieved through running: `/protect exclusion add <authority> player <name>` or `/protect exclusion add <authority> role <name>`.

These exclusions can additionally be later removed with `/protect exclusion remove`.

### putting it together: an example
That was a lot of things! Let's put this knowledge together on a simple example.

Our example server will want to have global grief protection, except in our survival dimension and free-build areas.
We additionally want to be able to exclude certain players from building in the free-build areas by use of a role.

Let's consider the two dimensions: `minecraft:overworld` and `example:survival`, as well as the role `builders`.

First, let's create an authority named `global`. Since we will mostly want protection everywhere, it makes sense to
globally apply protection and then specifically override this in specific areas.
 - `/protect add global with universe`
 
Next, we can set the rules on this authority:
 - `/protect set rule global place deny`
 - `/protect set rule global break deny`
 
Done! Now, let's override this behavior in our survival dimension.
 - `/protect add survival with example:survival`
 - `/protect set rule survival place allow`
 - `/protect set rule survival break allow`
 
But wait..! How will the mod know to apply the rules of `global` or `survival`, since `global` also applies in the survival dimension?
Here, we can make use of levels: the default level for an authority is `0`, and a larger value means higher priority.  
So: let's set the level of our `global` authority to `-1` such that anything we add in the future overrides it by default.
 - `/protect set level global -1`

Now we can similarly add our free build areas as exclusions too! Let's say we have two free build areas, creatively named `free_build_1` and `free_build_2`.
First, we should create an empty authority that applies to both of them, since they both have the same rules.
 - `/protect add free_build`
 - `/protect set rule free_build place allow`
 - `/protect set rule free_build break allow`

Next, let's compose the shape for `free_build_1`:
 - `/protect shape start`
 - `/protect shape add -10 0 -10 10 255 10`  
 ... and add it to our authority
 - `/protect shape finish free_build_1 to free_build`
 
And repeat the same for `free_build_2`:
  - `/protect shape start`
  - `/protect shape add -100 0 -10 90 255 10`  
  ... and add it to our authority
  - `/protect shape finish free_build_2 to free_build`

Done! Now the free-build areas should be editable by any player- except, we want to create exclusions such that we can 'ban' players from the areas.
Let's do that with a `free_build_banned` role:
 - `/protect exclusion add free_build role free_build_banned`
 
With that, all our protection should be set up nicely! :)

### testing! querying and checking rules
Okay, we've set up authorities, but how can we easily check which rules are set and by what?

A few commands may come in handy:
 - `/protect test`: tests all the rules that apply at a given location, and shows from which authority they originate
 - `/protect list`: lists all authorities in the world
 - `/protect display <authority>`: displays the rules and shape of the given authority
