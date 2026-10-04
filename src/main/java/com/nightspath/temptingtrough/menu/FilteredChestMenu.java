package com.nightspath.temptingtrough.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FilteredChestMenu extends AbstractContainerMenu {
    private static final int ROWS = 3;
    private static final int COLUMNS = 9;
    private static final int CONTAINER_SLOTS = ROWS * COLUMNS;

    private final Container container;

    public FilteredChestMenu(int containerId, Inventory inventory, Container container) {
        super(MenuType.GENERIC_9x3, containerId);
        checkContainerSize(container, CONTAINER_SLOTS);
        this.container = container;
        container.startOpen(inventory.player);

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                int containerSlot = x + y * COLUMNS;
                this.addSlot(new Slot(
                        container,
                        containerSlot,
                        8 + x * SLOT_SIZE,
                        18 + y * SLOT_SIZE
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return container.canPlaceItem(containerSlot, stack);
                    }
                });
            }
        }

        int inventoryTop = 18 + ROWS * SLOT_SIZE + 13;
        this.addStandardInventorySlots(inventory, 8, inventoryTop);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();

            if (slotIndex < CONTAINER_SLOTS) {
                if (!this.moveItemStackTo(stack, CONTAINER_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, CONTAINER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return clicked;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
