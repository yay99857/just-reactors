package com.justreactors.compat.cc;

import com.justreactors.battery.BatteryControllerBlockEntity;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

public class BatteryPeripheral implements IPeripheral {
    private final BatteryControllerBlockEntity battery;

    public BatteryPeripheral(BatteryControllerBlockEntity battery) {
        this.battery = battery;
    }

    @Override
    public String getType() {
        return "justreactors_battery";
    }

    @Override
    public Object getTarget() {
        return battery;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof BatteryPeripheral p && p.battery == this.battery;
    }

    @LuaFunction(mainThread = true)
    public final boolean isAssembled() {
        return battery.isAssembled();
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyStored() {
        return battery.getEnergyStorage().getEnergyStored();
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyCapacity() {
        return battery.getEnergyStorage().getMaxEnergyStored();
    }

    @LuaFunction(mainThread = true)
    public final double getChargePercent() {
        int cap = battery.getEnergyStorage().getMaxEnergyStored();
        return cap <= 0 ? 0.0 : battery.getEnergyStorage().getEnergyStored() * 100.0 / cap;
    }

    @LuaFunction(mainThread = true)
    public final int getInputRate() {
        return battery.getLastInput();
    }

    @LuaFunction(mainThread = true)
    public final int getOutputRate() {
        return battery.getLastOutput();
    }

    @LuaFunction(mainThread = true)
    public final int getCells() {
        return battery.getCells();
    }

    @LuaFunction(mainThread = true)
    public final int getMaxTransfer() {
        return battery.getMaxTransfer();
    }
}
