package com.justreactors.battery;

import com.justreactors.multiblock.LinkedPartBlockEntity;
import com.justreactors.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.LimitingEnergyHandler;
import org.jetbrains.annotations.Nullable;

public class BatteryPowerPortBlockEntity extends LinkedPartBlockEntity<BatteryControllerBlockEntity> {
    public BatteryPowerPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_POWER_PORT.get(), pos, state, BatteryControllerBlockEntity.class);
    }

    public boolean isOutlet() {
        return getBlockState().getValue(BatteryPowerPortBlock.OUTLET);
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        BatteryControllerBlockEntity c = getController();
        if (c == null) return null;
        int io = c.getMaxTransfer();
        return isOutlet()
                ? new LimitingEnergyHandler(c.getEnergyStorage(), 0, io)
                : new LimitingEnergyHandler(c.getEnergyStorage(), io, 0);
    }
}
