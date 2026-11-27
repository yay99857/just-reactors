package com.justreactors.compat.cc;

import com.justreactors.exchanger.HeatExchangerControllerBlockEntity;
import com.justreactors.transfer.ReactorFluidTank;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class HeatExchangerPeripheral implements IPeripheral {
    private final HeatExchangerControllerBlockEntity controller;

    public HeatExchangerPeripheral(HeatExchangerControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public String getType() {
        return "justreactors_heat_exchanger";
    }

    @Override
    public Object getTarget() {
        return controller;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof HeatExchangerPeripheral p && p.controller == this.controller;
    }

    @LuaFunction(mainThread = true)
    public final boolean isAssembled() {
        return controller.isAssembled();
    }

    @LuaFunction(mainThread = true)
    public final boolean getActive() {
        return controller.isActive();
    }

    @LuaFunction(mainThread = true)
    public final void setActive(boolean active) {
        controller.setActive(active);
    }

    @LuaFunction(mainThread = true)
    public final int getTubes() {
        return controller.getTubes();
    }

    @LuaFunction(mainThread = true)
    public final int getCoolingRate() {
        return controller.getLastCooledMb();
    }

    @LuaFunction(mainThread = true)
    public final int getMaxCoolingRate() {
        return controller.getMaxCoolingMb();
    }

    @LuaFunction(mainThread = true)
    public final int getSteamRate() {
        return controller.getLastSteamMb();
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getTanks() {
        return Map.of(
                "hotCoolant", tank(controller.getHotTank()),
                "coolant", tank(controller.getCoolantTank()),
                "water", tank(controller.getWaterTank()),
                "steam", tank(controller.getSteamTank()));
    }

    private static Map<String, Object> tank(ReactorFluidTank t) {
        return Map.of("amount", t.getFluidAmount(), "capacity", t.getCapacity());
    }
}
