package com.justreactors.compat.cc;

import com.justreactors.JustReactorsConfig;
import com.justreactors.turbine.TurbineStructure;
import com.justreactors.turbine.TurbineVentMode;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

public class TurbinePeripheral implements IPeripheral {
    private final TurbineControllerBlockEntity controller;

    public TurbinePeripheral(TurbineControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public String getType() {
        return "justreactors_turbine";
    }

    @Override
    public Object getTarget() {
        return controller;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof TurbinePeripheral p && p.controller == this.controller;
    }

    @LuaFunction(mainThread = true)
    public final boolean isConnected() {
        return controller.isAssembled();
    }

    @LuaFunction(mainThread = true)
    public final boolean isAssembled() {
        return controller.isAssembled();
    }

    @LuaFunction(mainThread = true)
    public final boolean getActive() {
        return controller.getSimulation().isActive();
    }

    @LuaFunction(mainThread = true)
    public final void setActive(boolean active) {
        controller.setActive(active);
    }

    @LuaFunction(mainThread = true)
    public final boolean getInductorEngaged() {
        return controller.getSimulation().isInductorEngaged();
    }

    @LuaFunction(mainThread = true)
    public final void setInductorEngaged(boolean engaged) {
        controller.setInductorEngaged(engaged);
    }

    @LuaFunction(mainThread = true)
    public final double getRotorSpeed() {
        return controller.getSimulation().getRpm();
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyStored() {
        return controller.getEnergyStorage().getEnergyStored();
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyCapacity() {
        return controller.getEnergyStorage().getMaxEnergyStored();
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyProducedLastTick() {
        return controller.getSimulation().getLastGeneratedFe();
    }

    @LuaFunction(mainThread = true)
    public final int getFluidFlowRate() {
        return controller.getSimulation().getLastSteamConsumedMb();
    }

    @LuaFunction(mainThread = true)
    public final int getFluidFlowRateMax() {
        return controller.getSimulation().getMaxSteamFlowMb();
    }

    @LuaFunction(mainThread = true)
    public final int getFluidFlowRateMaxMax() {
        return JustReactorsConfig.get(JustReactorsConfig.TURBINE_MAX_STEAM_FLOW);
    }

    @LuaFunction(mainThread = true)
    public final void setFluidFlowRateMax(int rate) {
        controller.setMaxSteamFlowMb(rate);
    }

    @LuaFunction(mainThread = true)
    public final int getSteamAmount() {
        return controller.getSteamTank().getFluidAmount();
    }

    @LuaFunction(mainThread = true)
    public final int getWaterAmount() {
        return controller.getWaterTank().getFluidAmount();
    }

    @LuaFunction(mainThread = true)
    public final int getFluidAmountMax() {
        return controller.getSteamTank().getCapacity();
    }

    @LuaFunction(mainThread = true)
    public final String getVentStatus() {
        return controller.getVentMode().id();
    }

    @LuaFunction(mainThread = true)
    public final void setVentOverflow() {
        controller.setVentMode(TurbineVentMode.OVERFLOW);
    }

    @LuaFunction(mainThread = true)
    public final void setVentAll() {
        controller.setVentMode(TurbineVentMode.ALL);
    }

    @LuaFunction(mainThread = true)
    public final void setVentNone() {
        controller.setVentMode(TurbineVentMode.NONE);
    }

    @LuaFunction(mainThread = true)
    public final int getBladeCount() {
        TurbineStructure structure = controller.getStructure();
        return structure == null ? 0 : structure.bladeCount();
    }

    @LuaFunction(mainThread = true)
    public final int getCoilCount() {
        TurbineStructure structure = controller.getStructure();
        return structure == null ? 0 : structure.coilCount();
    }
}
