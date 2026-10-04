package com.nightspath.temptingtrough.client;

import com.nightspath.temptingtrough.client.screen.FoodBowlScreen;
import com.nightspath.temptingtrough.menu.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class TemptingTroughClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.FOOD_BOWL, FoodBowlScreen::new);
    }
}
