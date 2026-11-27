package com.justreactors.blockentity;

import com.justreactors.registry.ModBlockEntities;
import com.justreactors.transfer.ReactorEnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes the assembled reactor's passive FE buffer and actively pushes FE to adjacent cables/machines.
 */
public class ReactorPowerTapBlockEntity extends ReactorPartBlockEntity {
    public ReactorPowerTapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_POWER_TAP.get(), pos, state);
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        ReactorControllerBlockEntity controller = getController();
        return controller != null ? controller.getEnergyStorage() : null;
    }

    public void pushEnergyToNeighbors(ReactorEnergyStorage buffer, int maxPerTap) {
        if (level == null || level.isClientSide()) return;
        int remaining = Math.min(maxPerTap, buffer.getEnergyStored());
        if (remaining <= 0) return;

        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            // Don't push back into our own reactor multiblock
            ReactorControllerBlockEntity controller = getController();
            if (controller != null && controller.getStructure() != null && controller.getStructure().contains(neighborPos)) {
                continue;
            }
            EnergyHandler neighbor = level.getCapability(Capabilities.Energy.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) continue;
            int pushed = ReactorEnergyStorage.pushEnergy(neighbor, remaining);
            if (pushed > 0) {
                buffer.extractInternal(pushed);
                remaining -= pushed;
            }
        }
    }
}
