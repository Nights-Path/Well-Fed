package com.nightspath.wellfed.block.entity;

import com.nightspath.wellfed.WellFed;
import com.nightspath.wellfed.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final BlockEntityType<FeedingTroughBlockEntity> FEEDING_TROUGH =
            register("feeding_trough", FeedingTroughBlockEntity::new, ModBlocks.FEEDING_TROUGH);

    public static final BlockEntityType<FoodBowlBlockEntity> FOOD_BOWL =
            register("food_bowl", FoodBowlBlockEntity::new, ModBlocks.FOOD_BOWL);

    private ModBlockEntities() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
            Block... blocks
    ) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                WellFed.id(name),
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build()
        );
    }

    public static void initialize() {
    }
}
