package com.justreactors.registry;

import com.justreactors.JustReactors;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class ModTags {
    // Mod-specific fuel tag (includes forge:ingots/uranium, forge:ingots/blutonium, and optional forge:ingots/yellorium)
    public static final TagKey<Item> REACTOR_FUEL = itemTag(JustReactors.MOD_ID, "reactor_fuel");

    // Common tags (forge:) for cross-mod interoperability
    public static final TagKey<Item> INGOTS_URANIUM = itemTag("forge", "ingots/uranium");
    public static final TagKey<Item> INGOTS_YELLORIUM = itemTag("forge", "ingots/yellorium");
    public static final TagKey<Item> INGOTS_GRAPHITE = itemTag("forge", "ingots/graphite");
    public static final TagKey<Item> INGOTS_CYANITE = itemTag("forge", "ingots/cyanite");
    public static final TagKey<Item> INGOTS_BLUTONIUM = itemTag("forge", "ingots/blutonium");
    public static final TagKey<Item> RAW_MATERIALS_URANIUM = itemTag("forge", "raw_materials/uranium");
    public static final TagKey<Item> ORES_URANIUM = itemTag("forge", "ores/uranium");
    public static final TagKey<Item> STORAGE_BLOCKS_URANIUM = itemTag("forge", "storage_blocks/uranium");
    public static final TagKey<Item> STORAGE_BLOCKS_GRAPHITE = itemTag("forge", "storage_blocks/graphite");
    public static final TagKey<Item> STORAGE_BLOCKS_CYANITE = itemTag("forge", "storage_blocks/cyanite");
    public static final TagKey<Item> STORAGE_BLOCKS_BLUTONIUM = itemTag("forge", "storage_blocks/blutonium");

    public static final TagKey<Block> BLOCK_ORES_URANIUM = blockTag("forge", "ores/uranium");
    public static final TagKey<Block> BLOCK_STORAGE_URANIUM = blockTag("forge", "storage_blocks/uranium");
    public static final TagKey<Block> BLOCK_STORAGE_GRAPHITE = blockTag("forge", "storage_blocks/graphite");
    public static final TagKey<Block> BLOCK_STORAGE_CYANITE = blockTag("forge", "storage_blocks/cyanite");
    public static final TagKey<Block> BLOCK_STORAGE_BLUTONIUM = blockTag("forge", "storage_blocks/blutonium");

    public static final TagKey<Fluid> FLUIDS_STEAM = fluidTag("forge", "steam");

    private ModTags() {
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(namespace, path));
    }

    private static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(namespace, path));
    }

    private static TagKey<Fluid> fluidTag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, new ResourceLocation(namespace, path));
    }
}
