package com.nightspath.temptingtrough.block.entity;

import com.nightspath.temptingtrough.block.FeedingTroughBlock;
import com.nightspath.temptingtrough.menu.FilteredChestMenu;
import com.nightspath.temptingtrough.tag.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class FeedingTroughBlockEntity extends FilteredContainerBlockEntity implements MenuProvider {
    public static final int CONTAINER_SIZE = 27;
    public static final int INFLUENCE_RADIUS = 4;
    public static final int INTERACTION_RADIUS = 2;

    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FEEDING_TROUGH, pos, state, CONTAINER_SIZE);
    }

    @Override
    protected boolean isItemAllowed(ItemStack stack) {
        return stack.is(ModItemTags.TROUGH_FOODS);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        syncFilledState();
    }

    private void syncFilledState() {
        Level level = this.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState state = this.getBlockState();
        boolean filled = !this.isEmpty();
        if (state.hasProperty(FeedingTroughBlock.FILLED)
                && state.getValue(FeedingTroughBlock.FILLED) != filled) {
            level.setBlockAndUpdate(this.getBlockPos(), state.setValue(FeedingTroughBlock.FILLED, filled));
        }
    }

    public int findFoodFor(Animal animal) {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty() && animal.isFood(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public boolean hasFoodFor(Animal animal) {
        return findFoodFor(animal) >= 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tempting_trough.feeding_trough");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FilteredChestMenu(containerId, inventory, this);
    }
}
