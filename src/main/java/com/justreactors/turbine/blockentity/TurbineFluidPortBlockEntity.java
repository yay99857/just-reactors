package com.justreactors.turbine.blockentity;

import com.justreactors.transfer.Handlers;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import com.justreactors.datagen.ModTags;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorFluidTank;
import com.justreactors.turbine.block.TurbineFluidPortBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
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
        return stack.is(ModFluids.STEAM_SOURCE.get()) || stack.is(ModTags.FLUIDS_STEAM);
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
            IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) continue;
            FluidStack toPush = waterTank.getFluid().copyWithAmount(remaining);
            int pushed = ReactorFluidTank.pushFluid(neighbor, toPush);
            if (pushed > 0) {
                waterTank.drainInternal(pushed);
                remaining -= pushed;
            }
        }
    }
}
