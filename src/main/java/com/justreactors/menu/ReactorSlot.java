package com.justreactors.menu;

import com.justreactors.transfer.ReactorItemHandler;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu slot backed directly by a {@link ReactorItemHandler}.
 */
public class ReactorSlot extends Slot {
    private static final Container EMPTY_CONTAINER = new SimpleContainer(0);
    private final ReactorItemHandler handler;
    private final int index;
    private final boolean allowPlace;

    public ReactorSlot(ReactorItemHandler handler, int index, int x, int y, boolean allowPlace) {
        super(EMPTY_CONTAINER, index, x, y);
        this.handler = handler;
        this.index = index;
        this.allowPlace = allowPlace;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return allowPlace && handler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem() {
        return handler.getStackInSlot(index);
    }

    @Override
    public void set(ItemStack stack) {
        handler.setStackInSlot(index, stack);
        setChanged();
    }

    @Override
    public void setByPlayer(ItemStack newStack) {
        handler.setStackInSlot(index, newStack);
        setChanged();
    }

    @Override
    public void setChanged() {
        handler.markChanged(index);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(64, stack.getMaxStackSize());
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack current = handler.getStackInSlot(index);
        if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        int taken = Math.min(amount, current.getCount());
        ItemStack result = current.copyWithCount(taken);
        if (taken == current.getCount()) {
            handler.setStackInSlot(index, ItemStack.EMPTY);
        } else {
            handler.setStackInSlot(index, current.copyWithCount(current.getCount() - taken));
        }
        return result;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !handler.getStackInSlot(index).isEmpty();
    }
}
