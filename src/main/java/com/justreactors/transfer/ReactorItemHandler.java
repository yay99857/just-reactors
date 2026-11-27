package com.justreactors.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * Simple slot-based item handler on NeoForge 26.3's transactional transfer API.
 */
public class ReactorItemHandler extends ItemStacksResourceHandler {
    public ReactorItemHandler(int size) {
        super(size);
    }

    public int getSlots() {
        return size();
    }

    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        set(slot, stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack),
                stack.isEmpty() ? 0 : stack.getCount());
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return isItemValid(index, resource.toStack());
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        onSlotChanged(index);
    }

    protected void onSlotChanged(int slot) {
    }

    public void markChanged(int slot) {
        onSlotChanged(slot);
    }
}
