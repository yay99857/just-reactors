package com.justreactors.turbine.blockentity;

import com.justreactors.datagen.ModTags;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorFluidTank;
import com.justreactors.transfer.RestrictedFluidHandler;
import com.justreactors.turbine.block.TurbineFluidPortBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

public class TurbineFluidPortBlockEntity extends TurbinePartBlockEntity {
    public TurbineFluidPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURBINE_FLUID_PORT.get(), pos, state);
    }

    public boolean isOutlet() {
        return getBlockState().getValue(TurbineFluidPortBlock.OUTLET);
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        TurbineControllerBlockEntity controller = getController();
        if (controller == null) return null;
        return isOutlet()
                ? RestrictedFluidHandler.extractOnly(controller.getWaterTank())
                : RestrictedFluidHandler.insertOnly(controller.getSteamTank(), TurbineFluidPortBlockEntity::isSteam);
    }

    @SuppressWarnings("deprecation")
    private static boolean isSteam(FluidResource resource) {
        return resource.is(ModFluids.STEAM_SOURCE.get())
                || resource.getFluid().builtInRegistryHolder().is(ModTags.FLUIDS_STEAM);
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
            ResourceHandler<FluidResource> neighbor = level.getCapability(Capabilities.Fluid.BLOCK, neighborPos, dir.getOpposite());
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
