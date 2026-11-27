package com.justreactors.compat.cc;

import com.justreactors.registry.ModBlockEntities;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers optional CC: Tweaked peripherals for Reactor and Turbine Controllers.
 * Only loaded when ComputerCraft is present.
 */
public final class JustReactorsCCCompat {
    private JustReactorsCCCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(PeripheralCapability.get(), ModBlockEntities.REACTOR_CONTROLLER.get(),
                (be, side) -> new ReactorPeripheral(be));
        event.registerBlockEntity(PeripheralCapability.get(), ModBlockEntities.TURBINE_CONTROLLER.get(),
                (be, side) -> new TurbinePeripheral(be));
    }
}
