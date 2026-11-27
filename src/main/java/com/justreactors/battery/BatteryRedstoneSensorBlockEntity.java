package com.justreactors.battery;

import com.justreactors.multiblock.LinkedPartBlockEntity;
import com.justreactors.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import com.justreactors.compat.nbt.ValueInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.justreactors.compat.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class BatteryRedstoneSensorBlockEntity extends LinkedPartBlockEntity<BatteryControllerBlockEntity> implements MenuProvider {
    private BatterySensorMode mode = BatterySensorMode.CHARGE_ABOVE;
    private int thresholdPercent = 90;

    public BatteryRedstoneSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_REDSTONE_SENSOR.get(), pos, state, BatteryControllerBlockEntity.class);
    }

    /** Called by the controller every tick while assembled. */
    public void tickOutput(BatteryControllerBlockEntity battery) {
        BatteryEnergyStorage e = battery.getEnergyStorage();
        setPowered(mode.test(e.getEnergyStored(), e.getMaxEnergyStored(), thresholdPercent));
    }

    /** Turns the signal off once the battery is gone (the controller no longer drives this sensor). */
    void serverTick() {
        if (level != null && level.getGameTime() % 20 == 0 && getController() == null) setPowered(false);
    }

    @Override
    public void setControllerPos(@Nullable BlockPos pos) {
        super.setControllerPos(pos);
        if (pos == null) setPowered(false);
    }

    private void setPowered(boolean powered) {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.getValue(BatteryRedstoneSensorBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(BatteryRedstoneSensorBlock.POWERED, powered), 3);
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    public int getSignal() {
        return getBlockState().getValue(BatteryRedstoneSensorBlock.POWERED) ? 15 : 0;
    }

    public BatterySensorMode getMode() {
        return mode;
    }

    public void setMode(BatterySensorMode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            setChanged();
            refresh();
        }
    }

    public int getThresholdPercent() {
        return thresholdPercent;
    }

    public void adjustThreshold(int delta) {
        int next = Math.max(0, Math.min(100, thresholdPercent + delta));
        if (next != thresholdPercent) {
            thresholdPercent = next;
            setChanged();
            refresh();
        }
    }

    private void refresh() {
        BatteryControllerBlockEntity c = getController();
        if (c != null) tickOutput(c);
        else setPowered(false);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.battery_redstone_sensor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BatterySensorMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        output.putInt("Mode", mode.ordinal());
        output.putInt("Threshold", thresholdPercent);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);
        mode = BatterySensorMode.byOrdinal(input.getIntOr("Mode", 0));
        thresholdPercent = Math.max(0, Math.min(100, input.getIntOr("Threshold", 90)));
    }
}
