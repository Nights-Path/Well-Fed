# Tempting Trough

A Fabric mod for Minecraft 26.3 that adds a Feeding Trough for livestock-style animals and a Food Bowl for tamed cats and wolves.

## Feeding Trough

- 27 inventory slots (3 rows of 9).
- Accepts only items in the `tempting_trough:trough_foods` item tag.
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

The default `trough_foods` tag is intentionally crop/seed focused. Datapacks and mods can extend the tag.

## Food Bowl

- 27 inventory slots (3 rows of 9), matching the Feeding Trough.
- One block serves both tamed cats and tamed wolves.
- Does not tempt pets and never enables breeding.
- Pets only eat when already inside a 5×5×5 interaction cube and are injured.
- Cats consume only `minecraft:cat_food`; wolves consume only `minecraft:wolf_food`.
- Healing uses the consumed item's vanilla food nutrition value.
- Sitting pets remain sitting because the bowl never adds a movement goal.

## Development

- Minecraft: 26.3
- Fabric Loader: 0.19.5
- Fabric API: 0.161.0+26.3
- Java: 25

Build with:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

Built jars are written to `build/libs`.

## Assets and recipes

The block models currently reuse vanilla textures as placeholders. Final art can replace the model/item JSON later without changing block logic.

Crafting recipes are intentionally not defined yet because their ingredients and layouts have not been specified.
