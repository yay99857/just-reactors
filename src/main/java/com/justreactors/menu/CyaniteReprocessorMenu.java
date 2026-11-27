package com.justreactors.menu;

import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.IntSupplier;

public class CyaniteReprocessorMenu extends AbstractContainerMenu {
    public final CyaniteReprocessorBlockEntity reprocessor;
    private final Level level;

    private final SyncedInt energy;
    private final SyncedInt maxEnergy;
    private final SyncedInt water;
    private final SyncedInt maxWater;
    private final SyncedInt progress;
    private final SyncedInt maxProgress;

    public CyaniteReprocessorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public CyaniteReprocessorMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.CYANITE_REPROCESSOR.get(), containerId);
        this.reprocessor = (CyaniteReprocessorBlockEntity) entity;
        this.level = inv.player.level();

        this.energy = syncInt(() -> reprocessor.getEnergyStorage().getEnergyStored());
        this.maxEnergy = syncInt(() -> reprocessor.getEnergyStorage().getMaxEnergyStored());
        this.water = syncInt(() -> reprocessor.getWaterTank().getFluidAmount());
        this.maxWater = syncInt(() -> reprocessor.getWaterTank().getCapacity());
        this.progress = syncInt(reprocessor::getProgress);
        this.maxProgress = syncInt(reprocessor::getMaxProgress);

        // Slot 0: Cyanite input, Slot 1: Blutonium output
        addSlot(new ReactorSlot(reprocessor.getInventory(), CyaniteReprocessorBlockEntity.SLOT_INPUT, 56, 35, true));
        addSlot(new ReactorSlot(reprocessor.getInventory(), CyaniteReprocessorBlockEntity.SLOT_OUTPUT, 116, 35, false));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    private SyncedInt syncInt(IntSupplier supplier) {
        SyncedInt synced = new SyncedInt(supplier);
        addDataSlot(synced.low());
        addDataSlot(synced.high());
        return synced;
    }

    public int getEnergy() {
        return energy.get();
    }

    public int getMaxEnergy() {
        return Math.max(1, maxEnergy.get());
    }

    public int getWater() {
        return water.get();
    }

    public int getMaxWater() {
        return Math.max(1, maxWater.get());
    }

    public int getProgress() {
        return progress.get();
    }

    public int getMaxProgress() {
        return Math.max(1, maxProgress.get());
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
        return stillValid(ContainerLevelAccess.create(level, reprocessor.getBlockPos()),
                player, ModBlocks.CYANITE_REPROCESSOR.get());
    }
}
