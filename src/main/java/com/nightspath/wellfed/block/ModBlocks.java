package com.nightspath.wellfed.block;

import com.nightspath.wellfed.WellFed;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static final Block FEEDING_TROUGH = register(
            "feeding_trough",
            FeedingTroughBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD)
    );

    public static final Block FOOD_BOWL = register(
            "food_bowl",
            FoodBowlBlock::new,
            BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.WOOD)
    );

    private ModBlocks() {
    }

    private static Block register(
            String path,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties properties
    ) {
        Identifier id = WellFed.id(path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Block block = factory.apply(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        BlockItem blockItem = new BlockItem(
                block,
                new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)
        );
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(FEEDING_TROUGH);
            output.accept(FOOD_BOWL);
        });
    }
}
