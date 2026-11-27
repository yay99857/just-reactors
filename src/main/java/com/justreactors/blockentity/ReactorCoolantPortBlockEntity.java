package com.justreactors.blockentity;

import com.justreactors.transfer.Handlers;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.transfer.ReactorFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes the assembled reactor's coolant (Water) inlet or Steam outlet to pipes and Fluid Tanks.
 */
public class ReactorCoolantPortBlockEntity extends ReactorPartBlockEntity {
    public ReactorCoolantPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_COOLANT_PORT.get(), pos, state);
    }

    public boolean isOutlet() {
        return getBlockState().getValue(ReactorCoolantPortBlock.OUTLET);
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        ReactorControllerBlockEntity controller = getController();
        if (controller == null) return null;

        if (isOutlet()) {
            // Outlet: extract-only Steam
            return Handlers.extractOnly(controller.getSteamTank());
        }
        // Inlet: insert-only Water
        return Handlers.insertOnly(controller.getCoolantTank(), stack -> stack.is(Fluids.WATER));
    }

    public void pushSteamToNeighbors(ReactorFluidTank steamTank, int maxRate) {
        if (level == null || level.isClientSide() || !isOutlet() || steamTank.isEmpty()) return;
        ReactorControllerBlockEntity controller = getController();
        int remaining = Math.min(maxRate, steamTank.getFluidAmount());
        if (remaining <= 0) return;

        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            if (controller != null && controller.getStructure() != null && controller.getStructure().contains(neighborPos)) {
                continue;
            }
            IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) continue;
            FluidStack toPush = steamTank.getFluid().copyWithAmount(remaining);
            int pushed = ReactorFluidTank.pushFluid(neighbor, toPush);
            if (pushed > 0) {
                steamTank.drainInternal(pushed);
                remaining -= pushed;
            }
        }
    }
}
