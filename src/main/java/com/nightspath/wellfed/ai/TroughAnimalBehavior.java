package com.nightspath.wellfed.ai;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;

public final class TroughAnimalBehavior {
    private static final int TROUGH_GOAL_PRIORITY = 4;

    private TroughAnimalBehavior() {
    }

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Animal animal && !(animal instanceof TamableAnimal)) {
                animal.getGoalSelector().addGoal(TROUGH_GOAL_PRIORITY, new FeedingTroughGoal(animal));
            }
        });
    }
}
