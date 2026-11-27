package com.justreactors.compat.cc;

import com.justreactors.blockentity.ReactorControlRodBlockEntity;
import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.reactor.ReactorMode;
import com.justreactors.reactor.ReactorStructure;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactorPeripheral implements IPeripheral {
    private final ReactorControllerBlockEntity controller;

    public ReactorPeripheral(ReactorControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public String getType() {
        return "justreactors_reactor";
    }

    @Override
    public Object getTarget() {
        return controller;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ReactorPeripheral p && p.controller == this.controller;
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
    public final boolean isActivelyCooled() {
        return controller.getSimulation().getMode() == ReactorMode.ACTIVE;
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
    public final double getFuelAmount() {
        return controller.getSimulation().getFuelMb();
    }

    @LuaFunction(mainThread = true)
    public final double getWasteAmount() {
        return controller.getSimulation().getWasteMb();
    }

    @LuaFunction(mainThread = true)
    public final int getFuelAmountMax() {
        return controller.getTotalFuelCapacityMb();
    }

    @LuaFunction(mainThread = true)
    public final double getFuelTemperature() {
        return controller.getSimulation().getFuelHeat();
    }

    @LuaFunction(mainThread = true)
    public final double getCasingTemperature() {
        return controller.getSimulation().getCasingHeat();
    }

    @LuaFunction(mainThread = true)
    public final double getFuelConsumedLastTick() {
        return controller.getSimulation().getLastFuelBurnedMb();
    }

    @LuaFunction(mainThread = true)
    public final int getCoolantAmount() {
        return controller.getCoolantTank().getFluidAmount();
    }

    @LuaFunction(mainThread = true)
    public final int getCoolantAmountMax() {
        return controller.getCoolantTank().getCapacity();
    }

    @LuaFunction(mainThread = true)
    public final int getSteamAmount() {
        return controller.getSteamTank().getFluidAmount();
    }

    @LuaFunction(mainThread = true)
    public final int getSteamAmountMax() {
        return controller.getSteamTank().getCapacity();
    }

    @LuaFunction(mainThread = true)
    public final int getSteamProducedLastTick() {
        return controller.getSimulation().getLastSteamProducedMb();
    }

    @LuaFunction(mainThread = true)
    public final int getNumberOfControlRods() {
        ReactorStructure structure = controller.getStructure();
        return structure == null ? 0 : structure.controlRods().size();
    }

    @LuaFunction(mainThread = true)
    public final int getControlRodLevel(int index) throws LuaException {
        ReactorControlRodBlockEntity rod = getControlRod(index);
        return rod.getInsertion();
    }

    @LuaFunction(mainThread = true)
    public final Map<Integer, Integer> getControlRodLevels() throws LuaException {
        ReactorStructure structure = requireAssembled();
        Level level = controller.getLevel();
        if (level == null) throw new LuaException("Reactor level is unavailable");

        Map<Integer, Integer> result = new HashMap<>();
        List<BlockPos> rods = structure.controlRods();
        for (int i = 0; i < rods.size(); i++) {
            if (level.getBlockEntity(rods.get(i)) instanceof ReactorControlRodBlockEntity rod) {
                result.put(i + 1, rod.getInsertion());
            }
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public final void setControlRodLevel(int index, int level) throws LuaException {
        ReactorControlRodBlockEntity rod = getControlRod(index);
        rod.setInsertion(level);
    }

    @LuaFunction(mainThread = true)
    public final void setAllControlRodLevels(int level) throws LuaException {
        requireAssembled();
        controller.setAllControlRods(level);
    }

    @LuaFunction(mainThread = true)
    public final int doEjectWaste() {
        return controller.ejectWasteAndCount();
    }

    private ReactorStructure requireAssembled() throws LuaException {
        ReactorStructure structure = controller.getStructure();
        if (structure == null) {
            throw new LuaException("Reactor is not assembled");
        }
        return structure;
    }

    private ReactorControlRodBlockEntity getControlRod(int index) throws LuaException {
        ReactorStructure structure = requireAssembled();
        List<BlockPos> rods = structure.controlRods();
        if (index < 0 || index >= rods.size()) {
            throw new LuaException("Control rod index out of bounds: " + index + " (count: " + rods.size() + ")");
        }
        Level level = controller.getLevel();
        if (level != null && level.getBlockEntity(rods.get(index)) instanceof ReactorControlRodBlockEntity rod) {
            return rod;
        }
        throw new LuaException("Control rod block entity missing at index " + index);
    }
}
