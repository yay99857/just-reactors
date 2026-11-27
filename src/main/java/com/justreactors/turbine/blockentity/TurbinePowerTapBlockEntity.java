package com.justreactors.turbine.blockentity;

import net.neoforged.neoforge.energy.IEnergyStorage;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.transfer.ReactorEnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class TurbinePowerTapBlockEntity extends TurbinePartBlockEntity {
    public TurbinePowerTapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURBINE_POWER_TAP.get(), pos, state);
    }

    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        TurbineControllerBlockEntity controller = getController();
        return controller != null ? controller.getEnergyStorage() : null;
    }

    public void pushEnergyToNeighbors(ReactorEnergyStorage buffer, int maxPerTap) {
        if (level == null || level.isClientSide()) return;
        int remaining = Math.min(maxPerTap, buffer.getEnergyStored());
        if (remaining <= 0) return;

        TurbineControllerBlockEntity controller = getController();
        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            if (controller != null && controller.getStructure() != null && controller.getStructure().contains(neighborPos)) {
                continue;
            }
            IEnergyStorage neighbor = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) continue;
            int pushed = ReactorEnergyStorage.pushEnergy(neighbor, remaining);
            if (pushed > 0) {
                buffer.extractInternal(pushed);
                remaining -= pushed;
            }
        }
    }
}
