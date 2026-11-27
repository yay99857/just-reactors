package com.justreactors.datagen;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModFluids;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, JustReactors.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.FLUIDS_STEAM)
                .add(ModFluids.STEAM_SOURCE.getKey())
                .add(ModFluids.STEAM_FLOWING.getKey());
    }
}
