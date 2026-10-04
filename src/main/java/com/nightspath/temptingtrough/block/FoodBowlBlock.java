package com.nightspath.temptingtrough.block;

import com.nightspath.temptingtrough.block.entity.FoodBowlBlockEntity;
import com.nightspath.temptingtrough.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class FoodBowlBlock extends BaseEntityBlock {
    public FoodBowlBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FoodBowlBlockEntity bowl) {
            player.openMenu(bowl);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoodBowlBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FOOD_BOWL, FoodBowlBlockEntity::serverTick);
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
