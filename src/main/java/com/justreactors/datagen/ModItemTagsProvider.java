package com.justreactors.datagen;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                               CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, JustReactors.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.INGOTS_URANIUM).add(ModItems.URANIUM_INGOT.getKey());
        tag(ModTags.INGOTS_GRAPHITE).add(ModItems.GRAPHITE_INGOT.getKey());
        tag(ModTags.INGOTS_CYANITE).add(ModItems.CYANITE_INGOT.getKey());
        tag(ModTags.INGOTS_BLUTONIUM).add(ModItems.BLUTONIUM_INGOT.getKey());
        tag(ModTags.RAW_MATERIALS_URANIUM).add(ModItems.RAW_URANIUM.getKey());
        tag(ModTags.ORES_URANIUM).add(ModItems.URANIUM_ORE.getKey(), ModItems.DEEPSLATE_URANIUM_ORE.getKey());
        tag(ModTags.STORAGE_BLOCKS_URANIUM).add(ModItems.URANIUM_BLOCK.getKey());
        tag(ModTags.STORAGE_BLOCKS_GRAPHITE).add(ModItems.GRAPHITE_BLOCK.getKey());
        tag(ModTags.STORAGE_BLOCKS_CYANITE).add(ModItems.CYANITE_BLOCK.getKey());
        tag(ModTags.STORAGE_BLOCKS_BLUTONIUM).add(ModItems.BLUTONIUM_BLOCK.getKey());

        tag(Tags.Items.INGOTS)
                .addTag(ModTags.INGOTS_URANIUM)
                .addTag(ModTags.INGOTS_GRAPHITE)
                .addTag(ModTags.INGOTS_CYANITE)
                .addTag(ModTags.INGOTS_BLUTONIUM);
        tag(Tags.Items.RAW_MATERIALS).addTag(ModTags.RAW_MATERIALS_URANIUM);
        tag(Tags.Items.ORES).addTag(ModTags.ORES_URANIUM);
        tag(Tags.Items.STORAGE_BLOCKS)
                .addTag(ModTags.STORAGE_BLOCKS_URANIUM)
                .addTag(ModTags.STORAGE_BLOCKS_GRAPHITE)
                .addTag(ModTags.STORAGE_BLOCKS_CYANITE)
                .addTag(ModTags.STORAGE_BLOCKS_BLUTONIUM);

        // Reactor fuel accepts uranium, blutonium, or optional yellorium ingots
        tag(ModTags.REACTOR_FUEL)
                .addTag(ModTags.INGOTS_URANIUM)
                .addTag(ModTags.INGOTS_BLUTONIUM)
                .addOptionalTag(ModTags.INGOTS_YELLORIUM);
    }
}
