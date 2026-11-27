package com.justreactors.datagen;

import com.justreactors.JustReactors;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class ModTags {
    // Mod-specific fuel tag (includes c:ingots/uranium, c:ingots/blutonium, and optional c:ingots/yellorium)
    public static final TagKey<Item> REACTOR_FUEL = itemTag(JustReactors.MOD_ID, "reactor_fuel");

    // Common tags (c:) for cross-mod interoperability
    public static final TagKey<Item> INGOTS_URANIUM = itemTag("c", "ingots/uranium");
    public static final TagKey<Item> INGOTS_YELLORIUM = itemTag("c", "ingots/yellorium");
    public static final TagKey<Item> INGOTS_GRAPHITE = itemTag("c", "ingots/graphite");
    public static final TagKey<Item> INGOTS_CYANITE = itemTag("c", "ingots/cyanite");
    public static final TagKey<Item> INGOTS_BLUTONIUM = itemTag("c", "ingots/blutonium");
    public static final TagKey<Item> RAW_MATERIALS_URANIUM = itemTag("c", "raw_materials/uranium");
    public static final TagKey<Item> ORES_URANIUM = itemTag("c", "ores/uranium");
    public static final TagKey<Item> STORAGE_BLOCKS_URANIUM = itemTag("c", "storage_blocks/uranium");
    public static final TagKey<Item> STORAGE_BLOCKS_GRAPHITE = itemTag("c", "storage_blocks/graphite");
    public static final TagKey<Item> STORAGE_BLOCKS_CYANITE = itemTag("c", "storage_blocks/cyanite");
    public static final TagKey<Item> STORAGE_BLOCKS_BLUTONIUM = itemTag("c", "storage_blocks/blutonium");

    public static final TagKey<Block> BLOCK_ORES_URANIUM = blockTag("c", "ores/uranium");
    public static final TagKey<Block> BLOCK_STORAGE_URANIUM = blockTag("c", "storage_blocks/uranium");
    public static final TagKey<Block> BLOCK_STORAGE_GRAPHITE = blockTag("c", "storage_blocks/graphite");
    public static final TagKey<Block> BLOCK_STORAGE_CYANITE = blockTag("c", "storage_blocks/cyanite");
    public static final TagKey<Block> BLOCK_STORAGE_BLUTONIUM = blockTag("c", "storage_blocks/blutonium");

    public static final TagKey<Fluid> FLUIDS_STEAM = fluidTag("c", "steam");

    private ModTags() {
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<Fluid> fluidTag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
