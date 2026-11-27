package com.justreactors.registry;

import com.justreactors.JustReactors;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JustReactors.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.justreactors"))
                    .icon(() -> new ItemStack(ModBlocks.REACTOR_CONTROLLER.get()))
                    .displayItems((parameters, output) -> {
                        // Reactor parts
                        output.accept(ModBlocks.REACTOR_CONTROLLER.get());
                        output.accept(ModBlocks.REACTOR_CASING.get());
                        output.accept(ModBlocks.REACTOR_GLASS.get());
                        output.accept(ModBlocks.REACTOR_FUEL_ROD.get());
                        output.accept(ModBlocks.REACTOR_CONTROL_ROD.get());
                        output.accept(ModBlocks.REACTOR_POWER_TAP.get());
                        output.accept(ModBlocks.REACTOR_ACCESS_PORT.get());
                        output.accept(ModBlocks.REACTOR_COOLANT_PORT.get());
                        output.accept(ModBlocks.REACTOR_REDSTONE_PORT.get());

                        // Turbine parts
                        output.accept(ModBlocks.TURBINE_CONTROLLER.get());
                        output.accept(ModBlocks.TURBINE_CASING.get());
                        output.accept(ModBlocks.TURBINE_GLASS.get());
                        output.accept(ModBlocks.TURBINE_ROTOR_BEARING.get());
                        output.accept(ModBlocks.TURBINE_ROTOR_SHAFT.get());
                        output.accept(ModBlocks.TURBINE_ROTOR_BLADE.get());
                        output.accept(ModBlocks.TURBINE_FLUID_PORT.get());
                        output.accept(ModBlocks.TURBINE_POWER_TAP.get());
                        output.accept(ModBlocks.TURBINE_REDSTONE_PORT.get());

                        // Heat Exchanger parts
                        output.accept(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
                        output.accept(ModBlocks.HEAT_EXCHANGER_CASING.get());
                        output.accept(ModBlocks.HEAT_EXCHANGER_GLASS.get());
                        output.accept(ModBlocks.HEAT_EXCHANGER_TUBE.get());
                        output.accept(ModBlocks.HEAT_EXCHANGER_FLUID_PORT.get());

                        // Battery parts
                        output.accept(ModBlocks.BATTERY_CONTROLLER.get());
                        output.accept(ModBlocks.BATTERY_CASING.get());
                        output.accept(ModBlocks.BATTERY_GLASS.get());
                        output.accept(ModBlocks.BATTERY_CELL.get());
                        output.accept(ModBlocks.BATTERY_POWER_PORT.get());
                        output.accept(ModBlocks.BATTERY_REDSTONE_SENSOR.get());

                        // Reprocessing & materials
                        output.accept(ModBlocks.CYANITE_REPROCESSOR.get());
                        output.accept(ModItems.RAW_URANIUM.get());
                        output.accept(ModItems.URANIUM_INGOT.get());
                        output.accept(ModItems.GRAPHITE_INGOT.get());
                        output.accept(ModItems.CYANITE_INGOT.get());
                        output.accept(ModItems.BLUTONIUM_INGOT.get());
                        output.accept(ModItems.STEAM_BUCKET.get());
                        output.accept(ModItems.COOLANT_BUCKET.get());
                        output.accept(ModItems.HOT_COOLANT_BUCKET.get());

                        output.accept(ModBlocks.URANIUM_ORE.get());
                        output.accept(ModBlocks.DEEPSLATE_URANIUM_ORE.get());
                        output.accept(ModBlocks.URANIUM_BLOCK.get());
                        output.accept(ModBlocks.GRAPHITE_BLOCK.get());
                        output.accept(ModBlocks.CYANITE_BLOCK.get());
                        output.accept(ModBlocks.BLUTONIUM_BLOCK.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
