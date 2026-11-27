package com.justreactors.battery;

import com.justreactors.JustReactorsConfig;
import com.justreactors.multiblock.BoxControllerBlockEntity;
import com.justreactors.multiblock.BoxValidator;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModBlocks;
import com.justreactors.transfer.ReactorItemHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.justreactors.compat.nbt.ValueInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.justreactors.compat.nbt.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Battery multiblock: stores FE in its interior cells. Power Ports move energy in or out, the
 * terminal charges (top slot) and drains (bottom slot) FE items, and Redstone Sensors report the charge.
 */
public class BatteryControllerBlockEntity extends BoxControllerBlockEntity {
    public static final BoxValidator.Spec SPEC = new BoxValidator.Spec("battery",
            ModBlocks.BATTERY_CASING, ModBlocks.BATTERY_CONTROLLER,
            List.of(ModBlocks.BATTERY_CASING, ModBlocks.BATTERY_GLASS, ModBlocks.BATTERY_CONTROLLER,
                    ModBlocks.BATTERY_POWER_PORT, ModBlocks.BATTERY_REDSTONE_SENSOR),
            ModBlocks.BATTERY_CELL, 3, () -> JustReactorsConfig.get(JustReactorsConfig.MAX_BATTERY_SIZE));

    public static final int SLOT_CHARGE = 0;
    public static final int SLOT_DISCHARGE = 1;
    private static final int CELL_VISUAL_INTERVAL = 20;

    private final BatteryEnergyStorage energy = new BatteryEnergyStorage(0, this::setChanged);
    private final ReactorItemHandler inventory = new ReactorItemHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
        }

        @Override
        protected void onSlotChanged(int slot) {
            setChanged();
        }
    };

    private int cells = 0;
    private int lastInput = 0;
    private int lastOutput = 0;
    private int energyAfterLastTick = 0;
    private int lastCellCharge = -1;

    public BatteryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_CONTROLLER.get(), pos, state);
    }

    @Override
    protected BoxValidator.Spec spec() {
        return SPEC;
    }

    @Override
    protected void onAssembled(BoxValidator.Scan scan, boolean firstTime) {
        int count = scan.core().size();
        if (count != cells || firstTime) {
            cells = count;
            energy.setCapacity(capacityFor(cells));
            lastCellCharge = -1;
            setChanged();
        }
        if (firstTime) energyAfterLastTick = energy.getEnergyStored();
    }

    @Override
    protected void onDisassembled() {
        lastInput = 0;
        lastOutput = 0;
    }

    public static int capacityFor(int cells) {
        return (int) Math.min(Integer.MAX_VALUE, (long) cells * JustReactorsConfig.get(JustReactorsConfig.BATTERY_CELL_CAPACITY));
    }

    /** FE/t allowed per Power Port, and through each item slot. */
    public int getMaxTransfer() {
        return (int) Math.min(Integer.MAX_VALUE, (long) cells * JustReactorsConfig.get(JustReactorsConfig.BATTERY_CELL_TRANSFER));
    }

    @Override
    protected boolean tickAssembled(Level level, BoxValidator.Scan scan) {
        // Net change from pipes pushing into input ports / pulling from output ports since the last tick.
        int delta = energy.getEnergyStored() - energyAfterLastTick;
        int received = Math.max(0, delta);
        int out = Math.max(0, -delta);
        int max = getMaxTransfer();

        // Drain the bottom-slot item into the battery.
        IEnergyStorage drainItem = itemEnergy(SLOT_DISCHARGE);
        if (drainItem != null && energy.getSpace() > 0) {
            received += moveEnergy(drainItem, energy, Math.min(max, energy.getSpace()));
        }

        // Output ports push to their neighbours.
        for (BlockPos p : scan.parts(ModBlocks.BATTERY_POWER_PORT.get())) {
            if (energy.getEnergyStored() <= 0) break;
            if (level.getBlockEntity(p) instanceof BatteryPowerPortBlockEntity port && port.isOutlet()) {
                out += port.pushEnergyToNeighbors(energy, max);
            }
        }

        // Charge the top-slot item.
        IEnergyStorage chargeItem = itemEnergy(SLOT_CHARGE);
        if (chargeItem != null && energy.getEnergyStored() > 0) {
            out += moveEnergy(energy, chargeItem, Math.min(max, energy.getEnergyStored()));
        }

        for (BlockPos p : scan.parts(ModBlocks.BATTERY_REDSTONE_SENSOR.get())) {
            if (level.getBlockEntity(p) instanceof BatteryRedstoneSensorBlockEntity sensor) sensor.tickOutput(this);
        }

        if (tickCounter % CELL_VISUAL_INTERVAL == 0) updateCellVisuals(level, scan);

        lastInput = received;
        lastOutput = out;
        energyAfterLastTick = energy.getEnergyStored();
        return received > 0 || out > 0;
    }

    @Nullable
    private IEnergyStorage itemEnergy(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        return stack.isEmpty() ? null : stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    /** Moves up to {@code max} FE from one handler to another, never losing energy on a partial insert. */
    private static int moveEnergy(IEnergyStorage from, IEnergyStorage to, int max) {
        if (max <= 0) return 0;
        int available = from.extractEnergy(max, true);
        if (available <= 0) return 0;
        int accepted = to.receiveEnergy(available, true);
        if (accepted <= 0) return 0;
        int extracted = from.extractEnergy(accepted, false);
        return to.receiveEnergy(extracted, false);
    }

    private void updateCellVisuals(Level level, BoxValidator.Scan scan) {
        int cap = energy.getMaxEnergyStored();
        int charge = cap <= 0 || energy.getEnergyStored() <= 0 ? 0
                : Math.max(1, (int) Math.round(4.0 * energy.getEnergyStored() / cap));
        if (charge == lastCellCharge) return;
        lastCellCharge = charge;
        for (BlockPos p : scan.core()) {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof BatteryCellBlock && st.getValue(BatteryCellBlock.CHARGE) != charge) {
                level.setBlock(p, st.setValue(BatteryCellBlock.CHARGE, charge), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public void dropContents() {
        if (level != null && !level.isClientSide()) {
            net.minecraft.world.SimpleContainer drops = new net.minecraft.world.SimpleContainer(inventory.getSlots());
            for (int i = 0; i < inventory.getSlots(); i++) drops.setItem(i, inventory.getStackInSlot(i));
            net.minecraft.world.Containers.dropContents(level, worldPosition, drops);
        }
    }

    @Override
    public int getComparatorSignal() {
        int cap = energy.getMaxEnergyStored();
        if (!isAssembled() || cap <= 0 || energy.getEnergyStored() <= 0) return 0;
        return Math.max(1, (int) Math.floor(15.0 * energy.getEnergyStored() / cap));
    }

    public BatteryEnergyStorage getEnergyStorage() {
        return energy;
    }

    public ReactorItemHandler getInventory() {
        return inventory;
    }

    public int getCells() {
        return cells;
    }

    public int getLastInput() {
        return lastInput;
    }

    public int getLastOutput() {
        return lastOutput;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.battery_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BatteryMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = new ValueOutput(tag, registries);
        output.putInt("Cells", cells);
        output.putInt("Energy", energy.getEnergyStored());
        inventory.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        cells = Math.max(0, input.getIntOr("Cells", 0));
        energy.setCapacity(capacityFor(cells));
        energy.setEnergyUnchecked(Math.min(energy.getMaxEnergyStored(), input.getIntOr("Energy", 0)));
        energyAfterLastTick = energy.getEnergyStored();
        input.child("Inventory").ifPresent(inventory::deserialize);
    }
}
