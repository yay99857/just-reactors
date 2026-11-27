package com.justreactors.exchanger;

import com.justreactors.multiblock.LinkedPartBlockEntity;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.Handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class HeatExchangerFluidPortBlockEntity extends LinkedPartBlockEntity<HeatExchangerControllerBlockEntity> {
    public HeatExchangerFluidPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_EXCHANGER_FLUID_PORT.get(), pos, state, HeatExchangerControllerBlockEntity.class);
    }

    public ExchangerPortMode getMode() {
        return getBlockState().getValue(HeatExchangerFluidPortBlock.MODE);
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        HeatExchangerControllerBlockEntity c = getController();
        if (c == null) return null;
        return switch (getMode()) {
            case INPUT -> new InputHandler(
                    Handlers.insertOnly(c.getHotTank(), s -> ModFluids.isHotCoolant(s.getFluid())),
                    Handlers.insertOnly(c.getWaterTank(), s -> s.is(Fluids.WATER)));
            case OUTPUT_COOLANT -> Handlers.extractOnly(c.getCoolantTank());
            case OUTPUT_STEAM -> Handlers.extractOnly(c.getSteamTank());
        };
    }

    /** Input port: Hot Coolant goes to the primary tank, Water to the secondary one. */
    private record InputHandler(IFluidHandler hot, IFluidHandler water) implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        private IFluidHandler tank(int i) {
            return i == 0 ? hot : water;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank(tank).getFluidInTank(0);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank(tank).getTankCapacity(0);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank(tank).isFluidValid(0, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = hot.fill(resource, action);
            return filled > 0 ? filled : water.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
