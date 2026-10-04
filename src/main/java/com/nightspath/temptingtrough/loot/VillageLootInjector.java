package com.nightspath.temptingtrough.loot;

import com.nightspath.temptingtrough.block.ModBlocks;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public final class VillageLootInjector {
    private static final float VILLAGE_BLOCK_LOOT_CHANCE = 0.01F;

    private static final Set<ResourceKey<LootTable>> VILLAGE_CHEST_TABLES = Set.of(
            BuiltInLootTables.VILLAGE_WEAPONSMITH,
            BuiltInLootTables.VILLAGE_TOOLSMITH,
            BuiltInLootTables.VILLAGE_ARMORER,
            BuiltInLootTables.VILLAGE_CARTOGRAPHER,
            BuiltInLootTables.VILLAGE_MASON,
            BuiltInLootTables.VILLAGE_SHEPHERD,
            BuiltInLootTables.VILLAGE_BUTCHER,
            BuiltInLootTables.VILLAGE_FLETCHER,
            BuiltInLootTables.VILLAGE_FISHER,
            BuiltInLootTables.VILLAGE_TANNERY,
            BuiltInLootTables.VILLAGE_TEMPLE,
            BuiltInLootTables.VILLAGE_DESERT_HOUSE,
            BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
            BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
            BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
            BuiltInLootTables.VILLAGE_SAVANNA_HOUSE
    );

    private VillageLootInjector() {
    }

    public static void initialize() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin() || !VILLAGE_CHEST_TABLES.contains(key)) {
                return;
            }

            LootPool.Builder rareBlockPool = LootPool.lootPool()
                    .when(LootItemRandomChanceCondition.randomChance(VILLAGE_BLOCK_LOOT_CHANCE))
                    .add(LootItem.lootTableItem(ModBlocks.FEEDING_TROUGH).setWeight(1))
                    .add(LootItem.lootTableItem(ModBlocks.FOOD_BOWL).setWeight(1));

            tableBuilder.withPool(rareBlockPool);
        });
    }
}
