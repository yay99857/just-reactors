package com.justreactors.blockentity;

import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorFluidTank;
import com.justreactors.transfer.RestrictedFluidHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
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
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        ReactorControllerBlockEntity controller = getController();
        if (controller == null) return null;
        // Outlet: extract-only Steam / Hot Coolant. Inlet: insert-only Water / Coolant.
        return isOutlet()
                ? RestrictedFluidHandler.extractOnly(controller.getSteamTank())
                : RestrictedFluidHandler.insertOnly(controller.getCoolantTank(),
                        r -> r.is(Fluids.WATER) || ModFluids.isCoolant(r.getFluid()));
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
            ResourceHandler<FluidResource> neighbor = level.getCapability(Capabilities.Fluid.BLOCK, neighborPos, dir.getOpposite());
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
