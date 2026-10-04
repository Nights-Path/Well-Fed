package com.nightspath.temptingtrough;

import com.nightspath.temptingtrough.ai.TroughAnimalBehavior;
import com.nightspath.temptingtrough.block.ModBlocks;
import com.nightspath.temptingtrough.block.entity.ModBlockEntities;
import com.nightspath.temptingtrough.menu.ModMenuTypes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TemptingTrough implements ModInitializer {
    public static final String MOD_ID = "tempting_trough";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModMenuTypes.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        TroughAnimalBehavior.initialize();

        LOGGER.info("Tempting Trough initialized.");
    }
}
