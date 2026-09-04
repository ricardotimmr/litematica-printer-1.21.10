# Litematica Easy Place Extension — Minecraft 1.21.10 Context

## Goal

I want to improve **Litematica Easy Place** in Minecraft **1.21.10 Fabric** so that blocks with quantity/count-based block states are placed correctly.

The immediate problem is **Wildflowers**.

A schematic can contain Wildflowers with states such as:

```text
wildflowers[facing=north,flower_amount=1]
wildflowers[facing=north,flower_amount=2]
wildflowers[facing=north,flower_amount=3]
wildflowers[facing=north,flower_amount=4]
```

Normal Litematica Easy Place correctly handles the orientation/facing, but it only places **one Wildflower**.

For example, if the schematic expects:

```text
wildflowers[facing=west,flower_amount=3]
```

Easy Place currently results in:

```text
wildflowers[facing=west,flower_amount=1]
```

The facing is correct, but `flower_amount` is not.

---

## Why This Happens

Wildflowers work similarly to other blocks where repeated placement changes a block-state value.

The first placement creates the block with an amount of `1`.

Further right-clicks / placements on the same block increase the amount:

```text
1 -> 2 -> 3 -> 4
```

Normal Litematica Easy Place appears to handle the initial placement but does not continue interacting with the already placed Wildflower until its block state matches the schematic.

So the desired behavior is:

```text
Schematic expects flower_amount=3

1. Place Wildflowers once
   -> flower_amount=1

2. Detect that the placed block does not yet match the schematic

3. Interact/place again
   -> flower_amount=2

4. Interact/place again
   -> flower_amount=3

5. Stop because the block state now matches the schematic
```

---

## Existing Addon of Interest

The project I want to investigate / port is:

**Litematica Printer Easyplace Extension**

Modrinth:

```text
https://modrinth.com/mod/litematica-printer-easyplace-extension
```

GitHub project associated with it:

```text
https://github.com/aria1th/litematica-printer
```

The important part is that this addon extends **Litematica's normal Easy Place workflow** rather than replacing it with a completely separate printer system.

That is exactly what I want.

I want to continue using:

```text
Litematica
+ Easy Place
+ Easy Place Extension
```

rather than switching to a Meteor Client based printer or a fully automatic schematic printer.

---

## Why This Addon Looks Promising

The addon already contains additional placement logic for blocks whose state can require multiple interactions.

Examples mentioned by the project include things such as:

- Snow layers
- Sea pickles
- Composters
- Other blocks with placement/state behavior that vanilla Easy Place does not completely handle

This is conceptually the same problem as Wildflowers.

For example:

```text
sea_pickle[pickles=4]
```

and:

```text
wildflowers[flower_amount=4]
```

both require repeated placement/interactions to reach the desired state.

Therefore the existing repeated-placement logic is probably the best place to add Wildflower support.

---

## Version Problem

My Minecraft version is:

```text
Minecraft 1.21.10
Fabric
```

My existing setup includes Litematica / MaLiLib and I want the solution to remain compatible with that setup.

The Easy Place Extension does not currently have an official Minecraft **1.21.10** release.

Therefore the likely task is:

1. Clone/fork the project.
2. Update/port it to Minecraft 1.21.10.
3. Update Fabric/Loom/mappings/dependencies as necessary.
4. Fix compile errors caused by Minecraft/Litematica/MaLiLib API changes.
5. Add explicit support for Wildflowers and other newer count-based blocks if the existing generic logic does not already support them.
6. Build a Fabric `.jar` that can be dropped into my normal `mods` folder.

---

## Primary Feature to Implement

Support the Wildflower block-state property:

```text
flower_amount
```

Possible values:

```text
1
2
3
4
```

Pseudo behavior:

```java
if (schematicBlock is Wildflowers) {
    int wantedAmount = schematicState.get(FLOWER_AMOUNT);
    int currentAmount = worldState.get(FLOWER_AMOUNT);

    if (currentAmount < wantedAmount) {
        interactWithBlock();
    }
}
```

The real implementation should preferably reuse the addon's existing logic for blocks such as sea pickles or snow layers instead of introducing a completely separate system.

---

## Generic Solution Preferred

Instead of hardcoding only Wildflowers, check whether the addon already has a generalized concept such as:

```text
desired state value > current state value
-> repeat placement
```

If possible, extend that system to support multiple counted properties.

Potential properties/block types worth checking include:

```text
Wildflowers
    flower_amount

Pink Petals
    flower_amount / amount equivalent

Leaf Litter
    segment_amount / amount equivalent

Sea Pickles
    pickles

Snow
    layers

Candles
    candles
```

Actual Minecraft 1.21.10 property names must be verified from mappings/source code rather than assumed.

---

## Important Behavior Requirements

### Keep normal Easy Place

The mod should not turn Easy Place into a fully automatic printer.

I still want the normal Litematica workflow:

```text
look at schematic block
hold required block
Easy Place places/interacts with it
```

The addon should simply make Easy Place smarter.

---

### Correct orientation must remain intact

For Wildflowers, the facing already works correctly.

For example:

```text
facing=west
```

must remain west while increasing:

```text
flower_amount=1
->
flower_amount=2
->
flower_amount=3
```

The extra interactions must not rotate or otherwise corrupt the block.

---

### Do not over-place

If the schematic expects:

```text
flower_amount=3
```

and the world already contains:

```text
flower_amount=3
```

there should be no interaction.

If the world contains:

```text
flower_amount=2
```

Easy Place should only interact once.

It should stop as soon as the world block state matches the schematic.

---

### Survival compatibility

The solution should work in normal survival gameplay.

It must use normal client-side placement/interact actions rather than commands or server-side cheats.

The server/world should see normal player placement interactions.

---

## Development Environment

Target:

```text
Minecraft: 1.21.10
Loader: Fabric
Client-side mod
Language: Java
IDE: IntelliJ IDEA
Build system: Gradle
```

Likely dependencies:

```text
Fabric Loader
Fabric API if required by the project
Litematica
MaLiLib
```

The exact versions should be selected based on currently available 1.21.10-compatible releases.

---

## Initial Investigation Tasks

When opening the repository, first locate the code responsible for special placement behavior.

Search for terms/classes related to:

```text
sea pickle
pickles
snow
layers
composter
EasyPlace
placement
interact
block state
properties
```

In particular, determine:

1. Where the addon intercepts Litematica Easy Place.
2. Where it reads the schematic target block state.
3. Where it reads the current world block state.
4. Where it decides whether another interaction is necessary.
5. Whether counted properties are handled generically or per block.
6. How Minecraft version-specific code is organized.

---

## Desired End Result

The final mod should allow this workflow:

```text
Schematic:
wildflowers[facing=south,flower_amount=4]

Player uses normal Litematica Easy Place.

Interaction 1:
wildflowers[facing=south,flower_amount=1]

Interaction 2:
wildflowers[facing=south,flower_amount=2]

Interaction 3:
wildflowers[facing=south,flower_amount=3]

Interaction 4:
wildflowers[facing=south,flower_amount=4]

Done.
```

From the player's perspective it should feel like normal Easy Place, except multi-placement decorative blocks now correctly match the schematic.

---

## Scope Priority

### Priority 1

Get the existing Easy Place Extension compiling and working on:

```text
Minecraft 1.21.10
```

### Priority 2

Make Wildflowers correctly support:

```text
flower_amount=1..4
```

### Priority 3

Check and support other newer Minecraft blocks that use similar count-based placement states, especially:

```text
Pink Petals
Leaf Litter
```

### Priority 4

Clean up the implementation so count-based block placement uses shared/generic logic where practical.

---

## Do Not Change Unnecessarily

Avoid rewriting the entire addon.

Prefer the smallest maintainable change:

```text
existing addon
-> update dependencies/API usage for 1.21.10
-> extend its existing special-placement handling
-> add Wildflowers/new counted blocks
```

The goal is a lightweight Easy Place enhancement, not a new schematic printer.
