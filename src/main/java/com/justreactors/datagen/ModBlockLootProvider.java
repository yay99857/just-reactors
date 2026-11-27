package com.justreactors.datagen;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(ModBlocks.URANIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_URANIUM.get()));
        add(ModBlocks.DEEPSLATE_URANIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_URANIUM.get()));

        dropSelf(ModBlocks.URANIUM_BLOCK.get());
        dropSelf(ModBlocks.GRAPHITE_BLOCK.get());
        dropSelf(ModBlocks.CYANITE_BLOCK.get());
        dropSelf(ModBlocks.BLUTONIUM_BLOCK.get());
        dropSelf(ModBlocks.CYANITE_REPROCESSOR.get());

        dropSelf(ModBlocks.REACTOR_CASING.get());
        dropSelf(ModBlocks.REACTOR_GLASS.get());
        dropSelf(ModBlocks.REACTOR_CONTROLLER.get());
        dropSelf(ModBlocks.REACTOR_FUEL_ROD.get());
        dropSelf(ModBlocks.REACTOR_CONTROL_ROD.get());
        dropSelf(ModBlocks.REACTOR_POWER_TAP.get());
        dropSelf(ModBlocks.REACTOR_ACCESS_PORT.get());
        dropSelf(ModBlocks.REACTOR_REDSTONE_PORT.get());
        dropSelf(ModBlocks.REACTOR_COOLANT_PORT.get());

        dropSelf(ModBlocks.TURBINE_CASING.get());
        dropSelf(ModBlocks.TURBINE_GLASS.get());
        dropSelf(ModBlocks.TURBINE_CONTROLLER.get());
        dropSelf(ModBlocks.TURBINE_ROTOR_BEARING.get());
        dropSelf(ModBlocks.TURBINE_ROTOR_SHAFT.get());
        dropSelf(ModBlocks.TURBINE_ROTOR_BLADE.get());
        dropSelf(ModBlocks.TURBINE_FLUID_PORT.get());
        dropSelf(ModBlocks.TURBINE_POWER_TAP.get());
        dropSelf(ModBlocks.TURBINE_REDSTONE_PORT.get());
        dropSelf(ModBlocks.HEAT_EXCHANGER_CASING.get());
        dropSelf(ModBlocks.HEAT_EXCHANGER_GLASS.get());
        dropSelf(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
        dropSelf(ModBlocks.HEAT_EXCHANGER_FLUID_PORT.get());
        dropSelf(ModBlocks.HEAT_EXCHANGER_TUBE.get());
        dropSelf(ModBlocks.BATTERY_CASING.get());
        dropSelf(ModBlocks.BATTERY_GLASS.get());
        dropSelf(ModBlocks.BATTERY_CONTROLLER.get());
        dropSelf(ModBlocks.BATTERY_POWER_PORT.get());
        dropSelf(ModBlocks.BATTERY_REDSTONE_SENSOR.get());
        dropSelf(ModBlocks.BATTERY_CELL.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(JustReactors.MOD_ID))
                .toList();
    }
}
