package com.justreactors.battery;

import com.justreactors.multiblock.LinkedPartBlockEntity;
import com.justreactors.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class BatteryPowerPortBlockEntity extends LinkedPartBlockEntity<BatteryControllerBlockEntity> {
    public BatteryPowerPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_POWER_PORT.get(), pos, state, BatteryControllerBlockEntity.class);
    }

    public boolean isOutlet() {
        return getBlockState().getValue(BatteryPowerPortBlock.OUTLET);
    }

    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        BatteryControllerBlockEntity c = getController();
        if (c == null) return null;
        return new PortView(c.getEnergyStorage(), !isOutlet(), isOutlet(), c.getMaxTransfer());
    }

    /** One-way view of the battery limited to the per-port transfer rate. */
    private record PortView(IEnergyStorage battery, boolean in, boolean out, int limit) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return in ? battery.receiveEnergy(Math.min(limit, toReceive), simulate) : 0;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return out ? battery.extractEnergy(Math.min(limit, toExtract), simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            return battery.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return battery.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return out;
        }

        @Override
        public boolean canReceive() {
            return in;
        }
    }
}
