package com.nightspath.temptingtrough.menu;

import com.nightspath.temptingtrough.TemptingTrough;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final MenuType<FoodBowlMenu> FOOD_BOWL =
            register("food_bowl", FoodBowlMenu::new);

    private ModMenuTypes() {
    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> supplier
    ) {
        return Registry.register(
                BuiltInRegistries.MENU,
                TemptingTrough.id(name),
                new MenuType<>(supplier, FeatureFlagSet.of())
        );
    }

    public static void initialize() {
    }
}
