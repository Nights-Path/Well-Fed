package com.nightspath.wellfed.block.entity;

import com.nightspath.wellfed.block.FoodBowlBlock;
import com.nightspath.wellfed.menu.FoodBowlMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class FoodBowlBlockEntity extends FilteredContainerBlockEntity implements MenuProvider {
    public static final int CONTAINER_SIZE = 3;
    public static final int INTERACTION_RADIUS = 2;
    private static final int CHECK_INTERVAL_TICKS = 20;

    public FoodBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOOD_BOWL, pos, state, CONTAINER_SIZE);
    }

    @Override
    protected boolean isItemAllowed(ItemStack stack) {
        return stack.is(ItemTags.CAT_FOOD) || stack.is(ItemTags.WOLF_FOOD);
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
        if (state.hasProperty(FoodBowlBlock.FILLED)
                && state.getValue(FoodBowlBlock.FILLED) != filled) {
            level.setBlockAndUpdate(this.getBlockPos(), state.setValue(FoodBowlBlock.FILLED, filled));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.well_fed.food_bowl");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FoodBowlMenu(containerId, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoodBowlBlockEntity bowl) {
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.getGameTime() % CHECK_INTERVAL_TICKS != 0L) {
            return;
        }

        AABB searchBox = new AABB(
                pos.getX() - INTERACTION_RADIUS,
                pos.getY() - INTERACTION_RADIUS,
                pos.getZ() - INTERACTION_RADIUS,
                pos.getX() + INTERACTION_RADIUS + 1,
                pos.getY() + INTERACTION_RADIUS + 1,
                pos.getZ() + INTERACTION_RADIUS + 1
        );

        List<TamableAnimal> pets = serverLevel.getEntitiesOfClass(
                TamableAnimal.class,
                searchBox,
                pet -> pet.isTame()
                        && pet.isAlive()
                        && pet.getHealth() < pet.getMaxHealth()
                        && (pet instanceof Cat || pet instanceof Wolf)
        );

        for (TamableAnimal pet : pets) {
            int slot = bowl.findFoodSlot(pet);
            if (slot < 0) {
                continue;
            }

            ItemStack food = bowl.getItem(slot);
            FoodProperties foodProperties = food.get(DataComponents.FOOD);
            if (foodProperties == null || foodProperties.nutrition() <= 0) {
                continue;
            }

            pet.heal(foodProperties.nutrition());
            bowl.consumeOne(slot);
        }
    }

    private int findFoodSlot(TamableAnimal pet) {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (pet instanceof Cat && stack.is(ItemTags.CAT_FOOD)) {
                return slot;
            }

            if (pet instanceof Wolf && stack.is(ItemTags.WOLF_FOOD)) {
                return slot;
            }
        }

        return -1;
    }
}
