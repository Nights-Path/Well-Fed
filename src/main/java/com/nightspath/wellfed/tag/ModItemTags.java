package com.nightspath.wellfed.tag;

import com.nightspath.wellfed.WellFed;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> TROUGH_FOODS =
            TagKey.create(Registries.ITEM, WellFed.id("trough_foods"));

    private ModItemTags() {
    }
}
