package com.nightspath.wellfed.client;

import com.nightspath.wellfed.client.screen.FeedingTroughScreen;
import com.nightspath.wellfed.client.screen.FoodBowlScreen;
import com.nightspath.wellfed.menu.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class WellFedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.FEEDING_TROUGH, FeedingTroughScreen::new);
        MenuScreens.register(ModMenuTypes.FOOD_BOWL, FoodBowlScreen::new);
    }
}
