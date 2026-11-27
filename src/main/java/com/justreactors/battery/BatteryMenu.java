package com.justreactors.battery;

import com.justreactors.menu.ReactorSlot;
import com.justreactors.menu.SyncedInt;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.IntSupplier;

public class BatteryMenu extends AbstractContainerMenu {
    public static final int SLOT_X = 149;
    public static final int CHARGE_Y = 31;
    public static final int DISCHARGE_Y = 79;
    public static final int INV_Y = 106;

    public final BatteryControllerBlockEntity battery;
    private final Level level;
    private final SyncedInt energy;
    private final SyncedInt capacity;
    private final SyncedInt input;
    private final SyncedInt output;
    private final SyncedInt cells;
    private final SyncedInt maxTransfer;

    public BatteryMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BatteryMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.BATTERY.get(), containerId);
        this.battery = (BatteryControllerBlockEntity) entity;
        this.level = inv.player.level();
        this.energy = sync(() -> battery.getEnergyStorage().getEnergyStored());
        this.capacity = sync(() -> battery.getEnergyStorage().getMaxEnergyStored());
        this.input = sync(battery::getLastInput);
        this.output = sync(battery::getLastOutput);
        this.cells = sync(battery::getCells);
        this.maxTransfer = sync(battery::getMaxTransfer);

        addSlot(new ReactorSlot(battery.getInventory(), BatteryControllerBlockEntity.SLOT_CHARGE, SLOT_X, CHARGE_Y, true));
        addSlot(new ReactorSlot(battery.getInventory(), BatteryControllerBlockEntity.SLOT_DISCHARGE, SLOT_X, DISCHARGE_Y, true));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, INV_Y + 58));
        }
    }

    private SyncedInt sync(IntSupplier supplier) {
        SyncedInt synced = new SyncedInt(supplier);
        addDataSlot(synced.low());
        addDataSlot(synced.high());
        return synced;
    }

    public int getEnergy() {
        return energy.get();
    }

    public int getCapacity() {
        return Math.max(1, capacity.get());
    }

    public int getInput() {
        return input.get();
    }

    public int getOutput() {
        return output.get();
    }

    public int getCells() {
        return cells.get();
    }

    public int getMaxTransfer() {
        return maxTransfer.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, battery.getBlockPos()), player, ModBlocks.BATTERY_CONTROLLER.get());
    }
}
