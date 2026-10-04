package com.nightspath.temptingtrough.block;

import com.nightspath.temptingtrough.block.entity.FeedingTroughBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class FeedingTroughBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    // Visible/selection shape: full 16x16 footprint.
    private static final VoxelShape SHAPE_EAST_WEST = Shapes.or(
            Block.box(1.0, 0.0, 0.0, 15.0, 2.0, 16.0),
            Block.box(1.0, 2.0, 0.0, 15.0, 8.0, 2.0),
            Block.box(1.0, 2.0, 14.0, 15.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 2.0, 11.0, 2.0),
            Block.box(0.0, 0.0, 14.0, 2.0, 11.0, 16.0),
            Block.box(14.0, 0.0, 0.0, 16.0, 11.0, 2.0),
            Block.box(14.0, 0.0, 14.0, 16.0, 11.0, 16.0)
    );

    private static final VoxelShape SHAPE_NORTH_SOUTH = Shapes.or(
            Block.box(0.0, 0.0, 1.0, 16.0, 2.0, 15.0),
            Block.box(0.0, 2.0, 1.0, 2.0, 8.0, 15.0),
            Block.box(14.0, 2.0, 1.0, 16.0, 8.0, 15.0),
            Block.box(0.0, 0.0, 0.0, 2.0, 11.0, 2.0),
            Block.box(14.0, 0.0, 0.0, 16.0, 11.0, 2.0),
            Block.box(0.0, 0.0, 14.0, 2.0, 11.0, 16.0),
            Block.box(14.0, 0.0, 14.0, 16.0, 11.0, 16.0)
    );

    // Fence/wall-like collision: the rim extends to 1.5 blocks high so mobs
    // cannot jump onto or over the trough walls even though the model is lower.
    private static final VoxelShape COLLISION_EAST_WEST = Shapes.or(
            Block.box(1.0, 0.0, 0.0, 15.0, 2.0, 16.0),
            Block.box(1.0, 2.0, 0.0, 15.0, 24.0, 2.0),
            Block.box(1.0, 2.0, 14.0, 15.0, 24.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 2.0, 24.0, 2.0),
            Block.box(0.0, 0.0, 14.0, 2.0, 24.0, 16.0),
            Block.box(14.0, 0.0, 0.0, 16.0, 24.0, 2.0),
            Block.box(14.0, 0.0, 14.0, 16.0, 24.0, 16.0)
    );

    private static final VoxelShape COLLISION_NORTH_SOUTH = Shapes.or(
            Block.box(0.0, 0.0, 1.0, 16.0, 2.0, 15.0),
            Block.box(0.0, 2.0, 1.0, 2.0, 24.0, 15.0),
            Block.box(14.0, 2.0, 1.0, 16.0, 24.0, 15.0),
            Block.box(0.0, 0.0, 0.0, 2.0, 24.0, 2.0),
            Block.box(14.0, 0.0, 0.0, 16.0, 24.0, 2.0),
            Block.box(0.0, 0.0, 14.0, 2.0, 24.0, 16.0),
            Block.box(14.0, 0.0, 14.0, 16.0, 24.0, 16.0)
    );

    public FeedingTroughBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(FILLED, false)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(FILLED, false);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.Z ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.Z ? COLLISION_EAST_WEST : COLLISION_NORTH_SOUTH;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FILLED);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            player.openMenu(trough);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeedingTroughBlockEntity(pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            boolean movedByPiston
    ) {
        if (level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
        Containers.updateNeighboursAfterDestroy(state, level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
