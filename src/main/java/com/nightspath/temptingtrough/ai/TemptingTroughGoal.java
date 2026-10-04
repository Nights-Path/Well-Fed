package com.nightspath.temptingtrough.ai;

import com.nightspath.temptingtrough.block.entity.FeedingTroughBlockEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public final class TemptingTroughGoal extends Goal {
    private static final double MOVE_SPEED = 1.0;
    private static final int SAFE_RADIUS = 2;
    private static final int SEARCH_INTERVAL_TICKS = 20;

    private final Animal animal;
    private FeedingTroughBlockEntity trough;
    private int searchCooldown;

    public TemptingTroughGoal(Animal animal) {
        this.animal = animal;
        this.searchCooldown = animal.getRandom().nextInt(SEARCH_INTERVAL_TICKS);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.searchCooldown-- > 0) {
            return false;
        }
        this.searchCooldown = SEARCH_INTERVAL_TICKS;

        this.trough = findNearestMatchingTrough();
        if (this.trough == null) {
            return false;
        }

        return animal.canFallInLove() || isNearInfluenceBoundary(this.trough.getBlockPos());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.trough == null
                || this.trough.isRemoved()
                || !isInsideInfluenceCube(this.trough.getBlockPos())
                || !this.trough.hasFoodFor(this.animal)) {
            return false;
        }

        return animal.canFallInLove() || !isInsideSafeCube(this.trough.getBlockPos());
    }

    @Override
    public void stop() {
        this.animal.getNavigation().stop();
        this.trough = null;
    }

    @Override
    public void tick() {
        if (this.trough == null) {
            return;
        }

        BlockPos troughPos = this.trough.getBlockPos();
        this.animal.getLookControl().setLookAt(
                troughPos.getX() + 0.5,
                troughPos.getY() + 0.5,
                troughPos.getZ() + 0.5
        );

        if (animal.canFallInLove() && isInsideInteractionCube(troughPos)) {
            int foodSlot = this.trough.findFoodFor(this.animal);
            if (foodSlot >= 0) {
                this.trough.consumeOne(foodSlot);
                this.animal.setInLove(null);
                this.animal.getNavigation().stop();
            }
            return;
        }

        if (animal.canFallInLove() || !isInsideSafeCube(troughPos)) {
            this.animal.getNavigation().moveTo(
                    troughPos.getX() + 0.5,
                    troughPos.getY() + 0.5,
                    troughPos.getZ() + 0.5,
                    MOVE_SPEED
            );
        } else {
            this.animal.getNavigation().stop();
        }
    }

    private FeedingTroughBlockEntity findNearestMatchingTrough() {
        Level level = this.animal.level();
        BlockPos origin = this.animal.blockPosition();
        FeedingTroughBlockEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        BlockPos min = origin.offset(
                -FeedingTroughBlockEntity.INFLUENCE_RADIUS,
                -FeedingTroughBlockEntity.INFLUENCE_RADIUS,
                -FeedingTroughBlockEntity.INFLUENCE_RADIUS
        );
        BlockPos max = origin.offset(
                FeedingTroughBlockEntity.INFLUENCE_RADIUS,
                FeedingTroughBlockEntity.INFLUENCE_RADIUS,
                FeedingTroughBlockEntity.INFLUENCE_RADIUS
        );

        for (BlockPos candidatePos : BlockPos.betweenClosed(min, max)) {
            if (!level.isLoaded(candidatePos)) {
                continue;
            }

            if (level.getBlockEntity(candidatePos) instanceof FeedingTroughBlockEntity candidate
                    && candidate.hasFoodFor(this.animal)) {
                double distance = candidatePos.distSqr(origin);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }
        }

        return nearest;
    }

    private boolean isNearInfluenceBoundary(BlockPos troughPos) {
        BlockPos animalPos = animal.blockPosition();
        return Math.abs(animalPos.getX() - troughPos.getX()) >= FeedingTroughBlockEntity.INFLUENCE_RADIUS - 1
                || Math.abs(animalPos.getY() - troughPos.getY()) >= FeedingTroughBlockEntity.INFLUENCE_RADIUS - 1
                || Math.abs(animalPos.getZ() - troughPos.getZ()) >= FeedingTroughBlockEntity.INFLUENCE_RADIUS - 1;
    }

    private boolean isInsideInfluenceCube(BlockPos troughPos) {
        return isInsideCube(troughPos, FeedingTroughBlockEntity.INFLUENCE_RADIUS);
    }

    private boolean isInsideInteractionCube(BlockPos troughPos) {
        return isInsideCube(troughPos, FeedingTroughBlockEntity.INTERACTION_RADIUS);
    }

    private boolean isInsideSafeCube(BlockPos troughPos) {
        return isInsideCube(troughPos, SAFE_RADIUS);
    }

    private boolean isInsideCube(BlockPos center, int radius) {
        BlockPos animalPos = animal.blockPosition();
        return Math.abs(animalPos.getX() - center.getX()) <= radius
                && Math.abs(animalPos.getY() - center.getY()) <= radius
                && Math.abs(animalPos.getZ() - center.getZ()) <= radius;
    }
}
