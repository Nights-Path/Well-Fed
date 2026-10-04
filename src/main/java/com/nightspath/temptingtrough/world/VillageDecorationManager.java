package com.nightspath.temptingtrough.world;

import com.nightspath.temptingtrough.TemptingTrough;
import com.nightspath.temptingtrough.block.FeedingTroughBlock;
import com.nightspath.temptingtrough.block.ModBlocks;
import com.nightspath.temptingtrough.block.entity.FoodBowlBlockEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class VillageDecorationManager {
    private static final float FOOD_BOWL_VILLAGE_CHANCE = 0.40F;
    private static final float TROUGH_CHANCE_WITH_BUTCHER = 0.75F;

    private static final long FOOD_BOWL_CHANCE_SALT = 0x4A6F7920426F776CL;
    private static final long FOOD_BOWL_POSITION_SALT = 0x426F776C506F734CL;
    private static final long FOOD_BOWL_LOOT_SALT = 0x426F776C4C6F6F74L;
    private static final long TROUGH_CHANCE_SALT = 0x54726F7567684368L;
    private static final long TROUGH_POSITION_SALT = 0x54726F756768506FL;

    private static final List<WeightedFood> FOOD = List.of(
            new WeightedFood(Items.COD, 5, 1, 3),
            new WeightedFood(Items.SALMON, 5, 1, 3),
            new WeightedFood(Items.BEEF, 4, 1, 3),
            new WeightedFood(Items.PORKCHOP, 4, 1, 3),
            new WeightedFood(Items.MUTTON, 4, 1, 3),
            new WeightedFood(Items.CHICKEN, 3, 1, 3),
            new WeightedFood(Items.RABBIT, 2, 1, 2),
            new WeightedFood(Items.ROTTEN_FLESH, 1, 1, 2)
    );
    private static final int FOOD_TOTAL_WEIGHT = FOOD.stream().mapToInt(WeightedFood::weight).sum();

    private VillageDecorationManager() {
    }

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (!generated) {
                return;
            }

            ChunkPos chunkPos = chunk.getPos();
            level.getServer().execute(() -> decorateGeneratedChunk(level, chunkPos));
        });
    }

    private static void decorateGeneratedChunk(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
        if (chunk == null) {
            return;
        }

        Registry<Structure> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Predicate<Structure> isVillage = structure ->
                structures.wrapAsHolder(structure).is(StructureTags.VILLAGE);

        List<StructureStart> villages = level.structureManager()
                .startsForStructure(chunkPos, isVillage);

        if (villages.isEmpty()) {
            return;
        }

        VillageDecorationData data = VillageDecorationData.get(level);

        for (StructureStart village : villages) {
            if (!village.isValid()) {
                continue;
            }

            long villageKey = village.getChunkPos().toLong();

            processFoodBowl(level, chunk, village, villageKey, data);
            processFeedingTrough(level, chunk, village, villageKey, data);
        }
    }

    private static void processFoodBowl(
            ServerLevel level,
            LevelChunk chunk,
            StructureStart village,
            long villageKey,
            VillageDecorationData data
    ) {
        if (data.isFoodBowlResolved(villageKey)) {
            return;
        }

        if (!rollChance(level, villageKey, FOOD_BOWL_CHANCE_SALT, FOOD_BOWL_VILLAGE_CHANCE)) {
            data.resolveFoodBowl(villageKey);
            return;
        }

        List<BlockPos> beds = findVillageAnchors(
                level,
                chunk,
                village,
                state -> state.is(BlockTags.BEDS)
        );

        if (beds.isEmpty()) {
            return;
        }

        RandomSource random = randomFor(
                level,
                villageKey ^ chunk.getPos().toLong(),
                FOOD_BOWL_POSITION_SALT
        );
        Util.shuffle(beds, random);

        for (BlockPos bed : beds) {
            BlockPos placement = findPlacementNear(level, chunk, bed, 3, false, random);
            if (placement == null) {
                continue;
            }

            if (placeFoodBowl(level, placement, villageKey)) {
                data.resolveFoodBowl(villageKey);
                TemptingTrough.LOGGER.debug(
                        "Generated village food bowl at {} for village start {}",
                        placement,
                        village.getChunkPos()
                );
                return;
            }
        }
    }

    private static void processFeedingTrough(
            ServerLevel level,
            LevelChunk chunk,
            StructureStart village,
            long villageKey,
            VillageDecorationData data
    ) {
        if (data.isFeedingTroughResolved(villageKey)) {
            return;
        }

        List<BlockPos> smokers = findVillageAnchors(
                level,
                chunk,
                village,
                state -> state.is(Blocks.SMOKER)
        );

        if (smokers.isEmpty()) {
            return;
        }

        if (!rollChance(level, villageKey, TROUGH_CHANCE_SALT, TROUGH_CHANCE_WITH_BUTCHER)) {
            data.resolveFeedingTrough(villageKey);
            return;
        }

        RandomSource random = randomFor(
                level,
                villageKey ^ chunk.getPos().toLong(),
                TROUGH_POSITION_SALT
        );
        Util.shuffle(smokers, random);

        for (BlockPos smoker : smokers) {
            BlockPos placement = findPlacementNear(level, chunk, smoker, 5, true, random);
            if (placement == null) {
                continue;
            }

            Direction facing = troughFacingFor(placement, smoker);
            BlockState troughState = ModBlocks.FEEDING_TROUGH
                    .defaultBlockState()
                    .setValue(FeedingTroughBlock.FACING, facing)
                    .setValue(FeedingTroughBlock.FILLED, false);

            if (level.setBlockAndUpdate(placement, troughState)) {
                data.resolveFeedingTrough(villageKey);
                TemptingTrough.LOGGER.debug(
                        "Generated feeding trough at {} near butcher smoker {} for village start {}",
                        placement,
                        smoker,
                        village.getChunkPos()
                );
                return;
            }
        }
    }

    private static List<BlockPos> findVillageAnchors(
            ServerLevel level,
            LevelChunk chunk,
            StructureStart village,
            Predicate<BlockState> predicate
    ) {
        List<BlockPos> anchors = new ArrayList<>();

        chunk.findBlocks(predicate, (pos, state) -> {
            BlockPos immutable = pos.immutable();
            if (level.structureManager().structureHasPieceAt(immutable, village)) {
                anchors.add(immutable);
            }
        });

        return anchors;
    }

    private static BlockPos findPlacementNear(
            ServerLevel level,
            LevelChunk chunk,
            BlockPos anchor,
            int radius,
            boolean needsClearBlockAbove,
            RandomSource random
    ) {
        List<Candidate> candidates = new ArrayList<>();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int horizontalDistance = Math.max(Math.abs(dx), Math.abs(dz));
                    if (horizontalDistance < 1 || horizontalDistance > radius) {
                        continue;
                    }

                    BlockPos candidate = anchor.offset(dx, dy, dz);
                    if (!chunk.getPos().contains(candidate)) {
                        continue;
                    }

                    if (isSafePlacement(level, candidate, needsClearBlockAbove)) {
                        candidates.add(new Candidate(candidate.immutable(), horizontalDistance));
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        Util.shuffle(candidates, random);
        candidates.sort(Comparator.comparingInt(Candidate::distance));
        return candidates.getFirst().pos();
    }

    private static boolean isSafePlacement(
            ServerLevel level,
            BlockPos pos,
            boolean needsClearBlockAbove
    ) {
        BlockState target = level.getBlockState(pos);
        if (!target.canBeReplaced() || !target.getFluidState().isEmpty()) {
            return false;
        }

        if (needsClearBlockAbove) {
            BlockState above = level.getBlockState(pos.above());
            if (!above.canBeReplaced() || !above.getFluidState().isEmpty()) {
                return false;
            }
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.is(Blocks.FARMLAND) || below.is(Blocks.DIRT_PATH)) {
            return false;
        }

        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static boolean placeFoodBowl(ServerLevel level, BlockPos pos, long villageKey) {
        if (!level.setBlockAndUpdate(pos, ModBlocks.FOOD_BOWL.defaultBlockState())) {
            return false;
        }

        if (!(level.getBlockEntity(pos) instanceof FoodBowlBlockEntity bowl)) {
            return false;
        }

        fillFoodBowl(
                bowl,
                randomFor(level, villageKey ^ pos.asLong(), FOOD_BOWL_LOOT_SALT)
        );
        return true;
    }

    private static void fillFoodBowl(FoodBowlBlockEntity bowl, RandomSource random) {
        List<Integer> slots = new ArrayList<>(List.of(0, 1, 2));
        Util.shuffle(slots, random);

        int stackCount = random.nextIntBetweenInclusive(1, 3);
        for (int index = 0; index < stackCount; index++) {
            WeightedFood food = chooseFood(random);
            int count = random.nextIntBetweenInclusive(food.minCount(), food.maxCount());
            bowl.setItem(slots.get(index), new ItemStack(food.item(), count));
        }
    }

    private static WeightedFood chooseFood(RandomSource random) {
        int roll = random.nextInt(FOOD_TOTAL_WEIGHT);

        for (WeightedFood food : FOOD) {
            roll -= food.weight();
            if (roll < 0) {
                return food;
            }
        }

        return FOOD.getLast();
    }

    private static Direction troughFacingFor(BlockPos trough, BlockPos smoker) {
        int dx = smoker.getX() - trough.getX();
        int dz = smoker.getZ() - trough.getZ();

        // The model's open ends run perpendicular to FACING. Orient the open
        // ends toward the butcher area whenever possible.
        return Math.abs(dx) >= Math.abs(dz) ? Direction.NORTH : Direction.EAST;
    }

    private static boolean rollChance(
            ServerLevel level,
            long villageKey,
            long salt,
            float chance
    ) {
        return randomFor(level, villageKey, salt).nextFloat() < chance;
    }

    private static RandomSource randomFor(ServerLevel level, long key, long salt) {
        return RandomSource.create(mix64(level.getSeed() ^ key ^ salt));
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private record Candidate(BlockPos pos, int distance) {
    }

    private record WeightedFood(Item item, int weight, int minCount, int maxCount) {
    }
}
