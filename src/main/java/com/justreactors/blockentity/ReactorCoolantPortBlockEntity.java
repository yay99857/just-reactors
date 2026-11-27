package com.justreactors.blockentity;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import com.justreactors.transfer.Caps;
import com.justreactors.transfer.Handlers;
import net.minecraftforge.fluids.capability.IFluidHandler;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.transfer.ReactorFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
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
        return Handlers.insertOnly(controller.getCoolantTank(), stack -> stack.getFluid().isSame(Fluids.WATER));
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
            IFluidHandler neighbor = Caps.block(level, neighborPos, ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
            if (neighbor == null) continue;
            FluidStack toPush = ReactorFluidTank.withAmount(steamTank.getFluid(), remaining);
            int pushed = ReactorFluidTank.pushFluid(neighbor, toPush);
            if (pushed > 0) {
                steamTank.drainInternal(pushed);
                remaining -= pushed;
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return Caps.of(getFluidHandler(side)).cast();
        return super.getCapability(cap, side);
    }
}
