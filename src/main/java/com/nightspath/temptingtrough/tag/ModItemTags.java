package com.nightspath.temptingtrough.tag;

import com.nightspath.temptingtrough.TemptingTrough;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> TROUGH_FOODS =
            TagKey.create(Registries.ITEM, TemptingTrough.id("trough_foods"));

    private ModItemTags() {
    }
}
