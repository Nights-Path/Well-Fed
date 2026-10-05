package com.nightspath.wellfed.ai;

import com.nightspath.wellfed.block.entity.FeedingTroughBlockEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class FeedingTroughGoal extends Goal {
    private static final double MOVE_SPEED = 1.0;
    private static final int SAFE_RADIUS = 2;
    private static final int SEARCH_INTERVAL_TICKS = 20;
    private static final int POPULATION_RECHECK_TICKS = 20;
    private static final int MAX_NEARBY_BABIES = 5;
    private static final int MAX_NEARBY_ADULTS = 10;

    private final Animal animal;
    private FeedingTroughBlockEntity trough;
    private int searchCooldown;
    private int populationRetryCooldown;

    public FeedingTroughGoal(Animal animal) {
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

        return isReadyForTroughBreeding() || isNearInfluenceBoundary(this.trough.getBlockPos());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.trough == null
                || this.trough.isRemoved()
                || !isInsideInfluenceCube(this.trough.getBlockPos())
                || !this.trough.hasFoodFor(this.animal)) {
            return false;
        }

        return isReadyForTroughBreeding() || !isInsideSafeCube(this.trough.getBlockPos());
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

        if (isReadyForTroughBreeding() && isInsideInteractionCube(troughPos)) {
            if (this.populationRetryCooldown > 0) {
                this.populationRetryCooldown--;
                this.animal.getNavigation().stop();
                return;
            }

            PopulationCounts population = countNearbyPopulation(troughPos);
            if (population.babies() >= MAX_NEARBY_BABIES
                    || population.adults() >= MAX_NEARBY_ADULTS) {
                // Keep the animal near the trough, but do not consume food or
                // start love mode while the local population is at either cap.
                this.populationRetryCooldown = POPULATION_RECHECK_TICKS;
                this.animal.getNavigation().stop();
                return;
            }

            int foodSlot = this.trough.findFoodFor(this.animal);
            if (foodSlot >= 0) {
                this.trough.consumeOne(foodSlot);
                this.animal.setInLove(null);
                this.animal.getNavigation().stop();
            }
            return;
        }

        if (isReadyForTroughBreeding() || !isInsideSafeCube(troughPos)) {
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

    private boolean isReadyForTroughBreeding() {
        // Vanilla parents receive a positive age after breeding (the breeding
        // cooldown). Requiring age == 0 means the trough cannot immediately
        // feed them again during that cooldown. Checking isInLove separately
        // also guarantees one item is consumed per love-mode activation.
        return this.animal.getAge() == 0 && !this.animal.isInLove();
    }

    private PopulationCounts countNearbyPopulation(BlockPos troughPos) {
        int radius = FeedingTroughBlockEntity.INFLUENCE_RADIUS;
        AABB area = new AABB(troughPos).inflate(radius);

        int babies = 0;
        int adults = 0;

        for (Animal nearby : this.animal.level().getEntitiesOfClass(Animal.class, area)) {
            // Tamed animals are handled by the Food Bowl and should not affect
            // livestock population limits for the Feeding Trough.
            if (nearby instanceof TamableAnimal) {
                continue;
            }

            if (nearby.isBaby()) {
                babies++;
                if (babies >= MAX_NEARBY_BABIES) {
                    break;
                }
            } else {
                adults++;
                if (adults >= MAX_NEARBY_ADULTS) {
                    break;
                }
            }
        }

        return new PopulationCounts(babies, adults);
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
    private record PopulationCounts(int babies, int adults) {
    }

}
