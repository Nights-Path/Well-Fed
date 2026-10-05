package com.nightspath.wellfed;

import com.nightspath.wellfed.ai.TroughAnimalBehavior;
import com.nightspath.wellfed.block.ModBlocks;
import com.nightspath.wellfed.block.entity.ModBlockEntities;
import com.nightspath.wellfed.config.WellFedConfig;
import com.nightspath.wellfed.loot.VillageLootInjector;
import com.nightspath.wellfed.menu.ModMenuTypes;
import com.nightspath.wellfed.world.VillageDecorationManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WellFed implements ModInitializer {
    public static final String MOD_ID = "well_fed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        WellFedConfig.load();
        ModMenuTypes.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        TroughAnimalBehavior.initialize();
        VillageDecorationManager.initialize();
        VillageLootInjector.initialize();

        LOGGER.info("Well Fed initialized.");
    }
}
