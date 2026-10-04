package com.nightspath.wellfed.world;

import com.nightspath.wellfed.WellFed;
import com.nightspath.wellfed.block.FeedingTroughBlock;
import com.nightspath.wellfed.block.ModBlocks;
import com.nightspath.wellfed.block.entity.FoodBowlBlockEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
    private static final float FEEDING_TROUGH_VILLAGE_CHANCE = 0.25F;

    private static final int DECORATION_DELAY_TICKS = 2;
    private static final boolean WORLDGEN_DIAGNOSTICS = true;

    private static final long FOOD_BOWL_CHANCE_SALT = 0x4A6F7920426F776CL;
    private static final long FOOD_BOWL_POSITION_SALT = 0x426F776C506F734CL;
    private static final long FOOD_BOWL_LOOT_SALT = 0x426F776C4C6F6F74L;
    private static final long TROUGH_CHANCE_SALT = 0x54726F7567684368L;
    private static final long TROUGH_POSITION_SALT = 0x54726F756768506FL;

    private static final Map<ServerLevel, Map<ChunkPos, Integer>> PENDING_CHUNKS =
            new WeakHashMap<>();

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
    private static final int FOOD_TOTAL_WEIGHT =
            FOOD.stream().mapToInt(WeightedFood::weight).sum();

    private VillageDecorationManager() {
    }

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) ->
                queueChunk(level, chunk.getPos()));

        ServerTickEvents.END_SERVER_TICK.register(server -> processPendingChunks(server));
    }

    private static void queueChunk(ServerLevel level, ChunkPos chunkPos) {
        PENDING_CHUNKS
                .computeIfAbsent(level, ignored -> new LinkedHashMap<>())
                .putIfAbsent(chunkPos, DECORATION_DELAY_TICKS);
    }

    private static void processPendingChunks(net.minecraft.server.MinecraftServer server) {
        // Chunk inspection can indirectly cause additional chunk-load events.
        // Never run decoration while iterating PENDING_CHUNKS, because those
        // events can enqueue into the same maps and invalidate the iterators.
        List<PendingDecoration> ready = new ArrayList<>();

        var levelIterator = PENDING_CHUNKS.entrySet().iterator();

        while (levelIterator.hasNext()) {
            var levelEntry = levelIterator.next();
            ServerLevel level = levelEntry.getKey();

            if (level == null) {
                levelIterator.remove();
                continue;
            }

            if (level.getServer() != server) {
                continue;
            }

            Map<ChunkPos, Integer> chunks = levelEntry.getValue();
            var chunkIterator = chunks.entrySet().iterator();

            while (chunkIterator.hasNext()) {
                var chunkEntry = chunkIterator.next();
                int ticksLeft = chunkEntry.getValue();

                if (ticksLeft > 1) {
                    chunkEntry.setValue(ticksLeft - 1);
                    continue;
                }

                ready.add(new PendingDecoration(level, chunkEntry.getKey()));
                chunkIterator.remove();
            }

            if (chunks.isEmpty()) {
                levelIterator.remove();
            }
        }

        // All map iterators are closed before decoration begins. If decoration
        // causes more chunks to load, queueChunk() may safely add them for a
        // future tick.
        for (PendingDecoration pending : ready) {
            decorateLoadedChunk(pending.level(), pending.chunkPos());
        }
    }

    private static void decorateLoadedChunk(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk triggerChunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
        if (triggerChunk == null) {
            return;
        }

        Registry<Structure> structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Predicate<Structure> isVillage = structure ->
                structures.wrapAsHolder(structure).is(StructureTags.VILLAGE);

        List<StructureStart> villages = level.structureManager()
                .startsForStructure(chunkPos.x(), chunkPos.z(), isVillage);

        if (villages.isEmpty()) {
            return;
        }

        VillageDecorationData data = VillageDecorationData.get(level);

        for (StructureStart village : villages) {
            if (!village.isValid()) {
                continue;
            }

            long villageKey = village.getChunkPos().pack();
            boolean bowlResolved = data.isFoodBowlResolved(villageKey);
            boolean troughResolved = data.isFeedingTroughResolved(villageKey);

            if (bowlResolved && troughResolved) {
                continue;
            }

            List<LevelChunk> loadedVillageChunks = getLoadedVillageChunks(level, village);

            diagnostics(
                    "Village {} detected from chunk {} in {}. Loaded village chunks: {}. Bowl resolved: {}. Trough resolved: {}.",
                    village.getChunkPos(),
                    chunkPos,
                    level.dimension(),
                    loadedVillageChunks.size(),
                    bowlResolved,
                    troughResolved
            );

            processFoodBowl(level, loadedVillageChunks, village, villageKey, data);
            processFeedingTrough(level, loadedVillageChunks, village, villageKey, data);
        }
    }

    private static List<LevelChunk> getLoadedVillageChunks(
            ServerLevel level,
            StructureStart village
    ) {
        List<LevelChunk> chunks = new ArrayList<>();

        village.getBoundingBox().intersectingChunks().forEach(chunkPos -> {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
            if (chunk != null) {
                chunks.add(chunk);
            }
        });

        return chunks;
    }

    private static void processFoodBowl(
            ServerLevel level,
            List<LevelChunk> loadedChunks,
            StructureStart village,
            long villageKey,
            VillageDecorationData data
    ) {
        if (data.isFoodBowlResolved(villageKey)) {
            return;
        }

        if (!rollChance(level, villageKey, FOOD_BOWL_CHANCE_SALT, FOOD_BOWL_VILLAGE_CHANCE)) {
            data.resolveFoodBowl(villageKey);
            diagnostics(
                    "Village {} did not roll a Food Bowl (40% chance).",
                    village.getChunkPos()
            );
            return;
        }

        List<VillageAnchor> beds = findVillageAnchors(
                level,
                loadedChunks,
                village,
                state -> state.is(BlockTags.BEDS)
        );

        diagnostics(
                "Village {} rolled a Food Bowl. Found {} loaded bed anchor(s).",
                village.getChunkPos(),
                beds.size()
        );

        if (beds.isEmpty()) {
            diagnostics(
                    "Village {} has no loaded bed anchors yet; Food Bowl remains unresolved and will retry on a later chunk load.",
                    village.getChunkPos()
            );
            return;
        }

        RandomSource random = randomFor(level, villageKey, FOOD_BOWL_POSITION_SALT);
        Util.shuffle(beds, random);

        for (VillageAnchor bed : beds) {
            BlockPos placement = findPlacementNear(
                    level,
                    bed.chunk(),
                    bed.pos(),
                    3,
                    false,
                    random
            );
            if (placement == null) {
                continue;
            }

            if (placeFoodBowl(level, placement, villageKey)) {
                data.resolveFoodBowl(villageKey);
                diagnostics(
                        "Placed Food Bowl at {} for village {}.",
                        placement,
                        village.getChunkPos()
                );
                return;
            }
        }

        diagnostics(
                "Village {} rolled a Food Bowl but no safe placement was found; it will retry on a later chunk load.",
                village.getChunkPos()
        );
    }

    private static void processFeedingTrough(
            ServerLevel level,
            List<LevelChunk> loadedChunks,
            StructureStart village,
            long villageKey,
            VillageDecorationData data
    ) {
        if (data.isFeedingTroughResolved(villageKey)) {
            return;
        }

        if (!rollChance(
                level,
                villageKey,
                TROUGH_CHANCE_SALT,
                FEEDING_TROUGH_VILLAGE_CHANCE
        )) {
            data.resolveFeedingTrough(villageKey);
            diagnostics(
                    "Village {} did not roll a Feeding Trough (25% chance).",
                    village.getChunkPos()
            );
            return;
        }

        RandomSource random = randomFor(level, villageKey, TROUGH_POSITION_SALT);

        List<VillageAnchor> smokers = findVillageAnchors(
                level,
                loadedChunks,
                village,
                state -> state.is(Blocks.SMOKER)
        );

        if (!smokers.isEmpty()) {
            diagnostics(
                    "Village {} rolled a Feeding Trough and has {} loaded smoker anchor(s); trying butcher placement first.",
                    village.getChunkPos(),
                    smokers.size()
            );

            Util.shuffle(smokers, random);
            for (VillageAnchor smoker : smokers) {
                BlockPos placement = findPlacementNear(
                        level,
                        smoker.chunk(),
                        smoker.pos(),
                        5,
                        true,
                        random
                );
                if (placement == null) {
                    continue;
                }

                if (placeFeedingTrough(level, placement, smoker.pos())) {
                    data.resolveFeedingTrough(villageKey);
                    diagnostics(
                            "Placed Feeding Trough at {} near butcher smoker {} for village {}.",
                            placement,
                            smoker.pos(),
                            village.getChunkPos()
                    );
                    return;
                }
            }

            diagnostics(
                    "Village {} had a smoker but no safe butcher-area placement; falling back to general village placement.",
                    village.getChunkPos()
            );
        }

        List<VillageAnchor> beds = findVillageAnchors(
                level,
                loadedChunks,
                village,
                state -> state.is(BlockTags.BEDS)
        );

        diagnostics(
                "Village {} general Feeding Trough fallback found {} loaded bed anchor(s).",
                village.getChunkPos(),
                beds.size()
        );

        if (beds.isEmpty()) {
            diagnostics(
                    "Village {} rolled a Feeding Trough but has no loaded fallback anchors yet; it remains unresolved and will retry.",
                    village.getChunkPos()
            );
            return;
        }

        Util.shuffle(beds, random);
        for (VillageAnchor bed : beds) {
            BlockPos placement = findPlacementNear(
                    level,
                    bed.chunk(),
                    bed.pos(),
                    4,
                    true,
                    random
            );
            if (placement == null) {
                continue;
            }

            if (placeFeedingTrough(level, placement, bed.pos())) {
                data.resolveFeedingTrough(villageKey);
                diagnostics(
                        "Placed general-village Feeding Trough at {} near bed {} for village {}.",
                        placement,
                        bed.pos(),
                        village.getChunkPos()
                );
                return;
            }
        }

        diagnostics(
                "Village {} rolled a Feeding Trough but no safe general placement was found; it will retry on a later chunk load.",
                village.getChunkPos()
        );
    }

    private static List<VillageAnchor> findVillageAnchors(
            ServerLevel level,
            List<LevelChunk> chunks,
            StructureStart village,
            Predicate<BlockState> predicate
    ) {
        List<VillageAnchor> exactPieceAnchors = new ArrayList<>();
        List<VillageAnchor> boundingBoxFallbackAnchors = new ArrayList<>();

        for (LevelChunk chunk : chunks) {
            chunk.findBlocks(predicate, (pos, state) -> {
                BlockPos immutable = pos.immutable();

                if (!village.getBoundingBox().isInside(immutable)) {
                    return;
                }

                VillageAnchor anchor = new VillageAnchor(chunk, immutable);
                boundingBoxFallbackAnchors.add(anchor);

                if (level.structureManager().structureHasPieceAt(immutable, village)) {
                    exactPieceAnchors.add(anchor);
                }
            });
        }

        if (!exactPieceAnchors.isEmpty()) {
            return exactPieceAnchors;
        }

        if (!boundingBoxFallbackAnchors.isEmpty()) {
            diagnostics(
                    "Village {} anchor scan found matching blocks inside its overall bounds but not inside a structure piece; using bounding-box fallback.",
                    village.getChunkPos()
            );
        }

        return boundingBoxFallbackAnchors;
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

    private static boolean placeFeedingTrough(
            ServerLevel level,
            BlockPos placement,
            BlockPos anchor
    ) {
        Direction facing = troughFacingFor(placement, anchor);
        BlockState troughState = ModBlocks.FEEDING_TROUGH
                .defaultBlockState()
                .setValue(FeedingTroughBlock.FACING, facing)
                .setValue(FeedingTroughBlock.FILLED, false);

        return level.setBlockAndUpdate(placement, troughState);
    }

    private static boolean placeFoodBowl(
            ServerLevel level,
            BlockPos pos,
            long villageKey
    ) {
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

    private static Direction troughFacingFor(BlockPos trough, BlockPos anchor) {
        int dx = anchor.getX() - trough.getX();
        int dz = anchor.getZ() - trough.getZ();

        // The model's open ends run perpendicular to FACING. Orient the open
        // ends toward the nearby village anchor whenever possible.
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

    private static void diagnostics(String message, Object... args) {
        if (WORLDGEN_DIAGNOSTICS) {
            WellFed.LOGGER.info("[VillageGen] " + message, args);
        }
    }

    private record PendingDecoration(ServerLevel level, ChunkPos chunkPos) {
    }

    private record Candidate(BlockPos pos, int distance) {
    }

    private record VillageAnchor(LevelChunk chunk, BlockPos pos) {
    }

    private record WeightedFood(Item item, int weight, int minCount, int maxCount) {
    }
}
