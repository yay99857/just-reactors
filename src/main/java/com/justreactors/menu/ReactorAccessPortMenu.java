package com.justreactors.menu;

import com.justreactors.blockentity.ReactorAccessPortBlockEntity;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ReactorAccessPortMenu extends AbstractContainerMenu {
    public static final int BTN_TOGGLE_MODE = 0;

    public final ReactorAccessPortBlockEntity port;
    private final Level level;
    private int syncedOutlet;

    public ReactorAccessPortMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ReactorAccessPortMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.REACTOR_ACCESS_PORT.get(), containerId);
        this.port = (ReactorAccessPortBlockEntity) entity;
        this.level = inv.player.level();

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return port.isOutlet() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedOutlet = value;
            }
        });

        // Slot 0: Fuel Input, Slot 1: Waste Output
        addSlot(new ReactorSlot(port.getInventory(), ReactorAccessPortBlockEntity.SLOT_FUEL, 44, 35, true));
        addSlot(new ReactorSlot(port.getInventory(), ReactorAccessPortBlockEntity.SLOT_WASTE, 116, 35, false));

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BTN_TOGGLE_MODE) {
            port.toggleOutlet();
            return true;
        }
        return false;
    }

    public boolean isOutlet() {
        return syncedOutlet != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, port.getBlockPos()),
                player, ModBlocks.REACTOR_ACCESS_PORT.get());
    }
}
