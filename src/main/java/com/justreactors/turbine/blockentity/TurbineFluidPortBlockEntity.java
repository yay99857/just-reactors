package com.justreactors.turbine.blockentity;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import com.justreactors.transfer.Caps;
import com.justreactors.transfer.Handlers;
import net.minecraftforge.fluids.capability.IFluidHandler;
import com.justreactors.registry.ModTags;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorFluidTank;
import com.justreactors.turbine.block.TurbineFluidPortBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class TurbineFluidPortBlockEntity extends TurbinePartBlockEntity {
    public TurbineFluidPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURBINE_FLUID_PORT.get(), pos, state);
    }

    public boolean isOutlet() {
        return getBlockState().getValue(TurbineFluidPortBlock.OUTLET);
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        TurbineControllerBlockEntity controller = getController();
        if (controller == null) return null;

        if (isOutlet()) {
            return Handlers.extractOnly(controller.getWaterTank());
        }
        return Handlers.insertOnly(controller.getSteamTank(), TurbineFluidPortBlockEntity::isSteam);
    }

    private static boolean isSteam(FluidStack stack) {
        return stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get()) || stack.getFluid().is(ModTags.FLUIDS_STEAM);
    }

    public void pushWaterToNeighbors(ReactorFluidTank waterTank, int maxRate) {
        if (level == null || level.isClientSide() || !isOutlet() || waterTank.isEmpty()) return;
        TurbineControllerBlockEntity controller = getController();
        int remaining = Math.min(maxRate, waterTank.getFluidAmount());
        if (remaining <= 0) return;

        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            if (controller != null && controller.getStructure() != null && controller.getStructure().contains(neighborPos)) {
                continue;
            }
            IFluidHandler neighbor = Caps.block(level, neighborPos, ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
            if (neighbor == null) continue;
            FluidStack toPush = ReactorFluidTank.withAmount(waterTank.getFluid(), remaining);
            int pushed = ReactorFluidTank.pushFluid(neighbor, toPush);
            if (pushed > 0) {
                waterTank.drainInternal(pushed);
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
