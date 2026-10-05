package com.nightspath.wellfed.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightspath.wellfed.WellFed;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class VillageDecorationData extends SavedData {
    public static final Codec<VillageDecorationData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.listOf()
                            .optionalFieldOf("food_bowl_resolved", List.of())
                            .forGetter(data -> List.copyOf(data.foodBowlResolved)),
                    Codec.LONG.listOf()
                            .optionalFieldOf("feeding_trough_resolved", List.of())
                            .forGetter(data -> List.copyOf(data.feedingTroughResolved))
            ).apply(instance, VillageDecorationData::new)
    );

    public static final SavedDataType<VillageDecorationData> TYPE =
            new SavedDataType<>(
                    WellFed.id("village_features"),
                    VillageDecorationData::new,
                    CODEC,
                    null
            );

    private final Set<Long> foodBowlResolved = new HashSet<>();
    private final Set<Long> feedingTroughResolved = new HashSet<>();

    public VillageDecorationData() {
    }

    private VillageDecorationData(List<Long> foodBowls, List<Long> feedingTroughs) {
        this.foodBowlResolved.addAll(foodBowls);
        this.feedingTroughResolved.addAll(feedingTroughs);
    }

    public static VillageDecorationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isFoodBowlResolved(long villageKey) {
        return this.foodBowlResolved.contains(villageKey);
    }

    public boolean isFeedingTroughResolved(long villageKey) {
        return this.feedingTroughResolved.contains(villageKey);
    }

    public void resolveFoodBowl(long villageKey) {
        if (this.foodBowlResolved.add(villageKey)) {
            this.setDirty();
        }
    }

    public void resolveFeedingTrough(long villageKey) {
        if (this.feedingTroughResolved.add(villageKey)) {
            this.setDirty();
        }
    }
}
