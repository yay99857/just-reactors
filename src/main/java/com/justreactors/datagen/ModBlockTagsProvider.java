package com.justreactors.datagen;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, JustReactors.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.URANIUM_ORE.getKey(),
                ModBlocks.DEEPSLATE_URANIUM_ORE.getKey(),
                ModBlocks.URANIUM_BLOCK.getKey(),
                ModBlocks.GRAPHITE_BLOCK.getKey(),
                ModBlocks.CYANITE_BLOCK.getKey(),
                ModBlocks.BLUTONIUM_BLOCK.getKey(),
                ModBlocks.CYANITE_REPROCESSOR.getKey(),
                ModBlocks.REACTOR_CASING.getKey(),
                ModBlocks.REACTOR_GLASS.getKey(),
                ModBlocks.REACTOR_CONTROLLER.getKey(),
                ModBlocks.REACTOR_FUEL_ROD.getKey(),
                ModBlocks.REACTOR_CONTROL_ROD.getKey(),
                ModBlocks.REACTOR_POWER_TAP.getKey(),
                ModBlocks.REACTOR_ACCESS_PORT.getKey(),
                ModBlocks.REACTOR_REDSTONE_PORT.getKey(),
                ModBlocks.REACTOR_COOLANT_PORT.getKey(),
                ModBlocks.TURBINE_CASING.getKey(),
                ModBlocks.TURBINE_GLASS.getKey(),
                ModBlocks.TURBINE_CONTROLLER.getKey(),
                ModBlocks.TURBINE_ROTOR_BEARING.getKey(),
                ModBlocks.TURBINE_ROTOR_SHAFT.getKey(),
                ModBlocks.TURBINE_ROTOR_BLADE.getKey(),
                ModBlocks.TURBINE_FLUID_PORT.getKey(),
                ModBlocks.TURBINE_POWER_TAP.getKey(),
                ModBlocks.TURBINE_REDSTONE_PORT.getKey()
        );

        tag(BlockTags.NEEDS_IRON_TOOL).add(
                ModBlocks.URANIUM_ORE.getKey(),
                ModBlocks.DEEPSLATE_URANIUM_ORE.getKey(),
                ModBlocks.URANIUM_BLOCK.getKey(),
                ModBlocks.BLUTONIUM_BLOCK.getKey()
        );

        tag(ModTags.BLOCK_ORES_URANIUM).add(
                ModBlocks.URANIUM_ORE.getKey(),
                ModBlocks.DEEPSLATE_URANIUM_ORE.getKey()
        );
        tag(ModTags.BLOCK_STORAGE_URANIUM).add(ModBlocks.URANIUM_BLOCK.getKey());
        tag(ModTags.BLOCK_STORAGE_GRAPHITE).add(ModBlocks.GRAPHITE_BLOCK.getKey());
        tag(ModTags.BLOCK_STORAGE_CYANITE).add(ModBlocks.CYANITE_BLOCK.getKey());
        tag(ModTags.BLOCK_STORAGE_BLUTONIUM).add(ModBlocks.BLUTONIUM_BLOCK.getKey());
    }
}
