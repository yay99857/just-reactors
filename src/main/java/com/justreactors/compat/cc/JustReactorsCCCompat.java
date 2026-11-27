package com.justreactors.compat.cc;

import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Registers optional CC: Tweaked peripherals for Reactor and Turbine Controllers.
 * Only loaded when ComputerCraft is present.
 */
public final class JustReactorsCCCompat {
    private JustReactorsCCCompat() {
    }

    public static void register() {
        ForgeComputerCraftAPI.registerPeripheralProvider((level, pos, side) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReactorControllerBlockEntity reactor) return LazyOptional.of(() -> new ReactorPeripheral(reactor));
            if (be instanceof TurbineControllerBlockEntity turbine) return LazyOptional.of(() -> new TurbinePeripheral(turbine));
            return LazyOptional.empty();
        });
    }
}
