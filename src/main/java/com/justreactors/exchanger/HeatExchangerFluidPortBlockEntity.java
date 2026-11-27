package com.justreactors.exchanger;

import com.justreactors.multiblock.LinkedPartBlockEntity;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.RestrictedFluidHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

public class HeatExchangerFluidPortBlockEntity extends LinkedPartBlockEntity<HeatExchangerControllerBlockEntity> {
    public HeatExchangerFluidPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_EXCHANGER_FLUID_PORT.get(), pos, state, HeatExchangerControllerBlockEntity.class);
    }

    public ExchangerPortMode getMode() {
        return getBlockState().getValue(HeatExchangerFluidPortBlock.MODE);
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        HeatExchangerControllerBlockEntity c = getController();
        if (c == null) return null;
        return switch (getMode()) {
            case INPUT -> new CombinedResourceHandler<>(
                    RestrictedFluidHandler.insertOnly(c.getHotTank(), r -> ModFluids.isHotCoolant(r.getFluid())),
                    RestrictedFluidHandler.insertOnly(c.getWaterTank(), r -> r.is(Fluids.WATER)));
            case OUTPUT_COOLANT -> RestrictedFluidHandler.extractOnly(c.getCoolantTank());
            case OUTPUT_STEAM -> RestrictedFluidHandler.extractOnly(c.getSteamTank());
        };
    }
}
