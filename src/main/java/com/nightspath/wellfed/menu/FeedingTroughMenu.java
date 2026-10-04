package com.nightspath.wellfed.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FeedingTroughMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SLOTS = 5;
    private static final int INVENTORY_START = CONTAINER_SLOTS;
    private static final int INVENTORY_END = INVENTORY_START + Inventory.INVENTORY_SIZE;

    private final Container container;

    public FeedingTroughMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(CONTAINER_SLOTS));
    }

    public FeedingTroughMenu(int containerId, Inventory inventory, Container container) {
        super(ModMenuTypes.FEEDING_TROUGH, containerId);
        checkContainerSize(container, CONTAINER_SLOTS);
        this.container = container;
        container.startOpen(inventory.player);

        for (int x = 0; x < CONTAINER_SLOTS; x++) {
            final int slotIndex = x;
            this.addSlot(new Slot(container, slotIndex, 44 + x * SLOT_SIZE, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(slotIndex, stack);
                }
            });
        }

        this.addStandardInventorySlots(inventory, 8, 51);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack clicked = stack.copy();

        if (slotIndex < CONTAINER_SLOTS) {
            if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
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

        return clicked;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
