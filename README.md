# Well Fed

A Fabric mod for Minecraft 26.3 that adds a Feeding Trough for livestock-style animals and a Food Bowl for tamed cats and wolves.

## Feeding Trough

- 5 inventory slots.
- Accepts only items in the `well_fed:trough_foods` item tag.
- Hoppers and Fabric Transfer API-compatible pipes can insert/extract.
- An animal is only affected when the trough contains an item that the animal itself considers food.
- Tamed animals are excluded from trough behavior.
- Influence area: 9×9×9 cube centered on the trough (4 blocks in each direction).
- Eating area: 5×5×5 cube centered on the trough (2 blocks in each direction).
- Animals near the edge of the influence area are biased back toward the trough.
- Breed-ready adults move toward the trough, consume one matching item once inside the eating area, and enter vanilla love mode.
- There is no population cap and no extra breeding cooldown beyond vanilla rules.
- The trough goal has lower priority than ordinary player temptation/breeding behavior, so animals can still be deliberately lured out.
- Inactive troughs do nothing to an animal when they contain no matching food.
- Villages have a deterministic 25% chance to receive one trough. Butcher/smoker placement is preferred when available; otherwise the trough falls back to a safe general-village location near a bed.

The default `trough_foods` tag is intentionally crop/seed focused. Datapacks and mods can extend the tag.

### Crafting

```text
WWW
SSS
```

- `W`: Wheat
- `S`: Any wooden slab

## Food Bowl

- 3 inventory slots.
- One block serves both tamed cats and tamed wolves.
- Does not tempt pets and never enables breeding.
- Pets only eat when already inside a 5×5×5 interaction cube and are injured.
- Cats consume only `minecraft:cat_food`; wolves consume only `minecraft:wolf_food`.
- Healing uses the consumed item's vanilla food nutrition value.
- Sitting pets remain sitting because the bowl never adds a movement goal.
- During new village generation, each village has a deterministic 40% chance to receive one bowl near a village bed.
- Naturally generated bowls start with 1–3 food stacks randomly distributed among their three slots. Fish and raw meats are common; rotten flesh is uncommon.

### Crafting

```text
 F
SSS
```

- `S`: Any wooden slab
- `F`: Any item in `well_fed:food_bowl_crafting_food`:
  - any vanilla fish
  - raw beef
  - raw chicken
  - raw porkchop
  - raw mutton
  - raw rabbit
  - rotten flesh

## Village chest loot

All vanilla village chest loot tables receive one additional independent loot pool:

- 1% chance per eligible village chest to contain one Well Fed block.
- When that 1% roll succeeds, it chooses equally between a Feeding Trough and a Food Bowl.
- Vanilla chest contents are preserved; the mod only adds this small extra pool.
- Datapack-overridden village loot tables are left untouched.

## World generation behavior

Village decorations are evaluated shortly after village chunks load into the server:

- Placement is deterministic from the world seed and village start, so chunk reloads cannot reroll placement.
- A persistent per-dimension record prevents duplicate decoration if additional chunks of the same village generate later.
- Food Bowls search for a safe floor location near a village bed.
- Feeding Troughs prefer a safe location near a butcher smoker, then fall back to a safe location near a village bed.
- Paths and farmland are not overwritten.
- Existing villages can be decorated when their chunks are loaded if that village has not already had its Well Fed decoration decision resolved.

## Development

- Minecraft: 26.3
- Fabric Loader: 0.19.5
- Fabric API: 0.161.0+26.3
- Java: 25
- Mod version: 0.1.0
- Mod ID: `well_fed`

Build with:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

Built jars are written to `build/libs`.
