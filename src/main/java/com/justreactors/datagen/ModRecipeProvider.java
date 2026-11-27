package com.justreactors.datagen;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModItems;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        super(recipes, advancements);
    }

    @Override
    protected void buildRecipes() {
        RecipeOutput out = this.output;

        // Smelting & Blasting
        cook(out, ModItems.RAW_URANIUM.get(), ModItems.URANIUM_INGOT.get(), 0.7F, "uranium_ingot_from_raw");
        cook(out, ModTags.ORES_URANIUM, ModItems.URANIUM_INGOT.get(), 1.0F, "uranium_ingot_from_ore");
        cook(out, Items.COAL, ModItems.GRAPHITE_INGOT.get(), 0.35F, "graphite_ingot_from_coal");
        cook(out, Items.CHARCOAL, ModItems.GRAPHITE_INGOT.get(), 0.35F, "graphite_ingot_from_charcoal");

        // Storage blocks <-> Ingots
        storageBlock(out, ModTags.INGOTS_URANIUM, ModItems.URANIUM_INGOT.get(), ModBlocks.URANIUM_BLOCK.get(), "uranium");
        storageBlock(out, ModTags.INGOTS_GRAPHITE, ModItems.GRAPHITE_INGOT.get(), ModBlocks.GRAPHITE_BLOCK.get(), "graphite");
        storageBlock(out, ModTags.INGOTS_CYANITE, ModItems.CYANITE_INGOT.get(), ModBlocks.CYANITE_BLOCK.get(), "cyanite");
        storageBlock(out, ModTags.INGOTS_BLUTONIUM, ModItems.BLUTONIUM_INGOT.get(), ModBlocks.BLUTONIUM_BLOCK.get(), "blutonium");

        // Reactor Multiblock Parts
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REACTOR_CASING.get(), 4)
                .pattern("IGI")
                .pattern("G G")
                .pattern("IGI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', ModTags.INGOTS_GRAPHITE)
                .unlockedBy("has_graphite", has(ModTags.INGOTS_GRAPHITE))
                .save(out);

        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REACTOR_GLASS.get(), 2)
                .pattern("GCG")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('G', Tags.BlockItems.GLASS_BLOCKS.item())
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_CONTROLLER.get())
                .pattern("CUC")
                .pattern("RDR")
                .pattern("CUC")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('U', ModTags.INGOTS_URANIUM)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        shaped(RecipeCategory.MISC, ModBlocks.REACTOR_FUEL_ROD.get())
                .pattern("IGI")
                .pattern("GUG")
                .pattern("IGI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', ModTags.INGOTS_GRAPHITE)
                .define('U', ModTags.INGOTS_URANIUM)
                .unlockedBy("has_uranium", has(ModTags.INGOTS_URANIUM))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_CONTROL_ROD.get())
                .pattern("CGC")
                .pattern("GRG")
                .pattern("CGC")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('G', ModTags.INGOTS_GRAPHITE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_POWER_TAP.get())
                .pattern("CRC")
                .pattern("RKR")
                .pattern("CRC")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('K', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_ACCESS_PORT.get())
                .pattern("CHC")
                .pattern(" P ")
                .pattern("C C")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('H', Items.HOPPER)
                .define('P', Items.PISTON)
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_REDSTONE_PORT.get())
                .pattern("CRC")
                .pattern("RGR")
                .pattern("CRC")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.REACTOR_COOLANT_PORT.get())
                .pattern("CBC")
                .pattern("IPI")
                .pattern("CBC")
                .define('C', ModBlocks.REACTOR_CASING.get())
                .define('B', Items.BUCKET)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', Items.PISTON)
                .unlockedBy("has_casing", has(ModBlocks.REACTOR_CASING.get()))
                .save(out);

        // Cyanite Reprocessor
        shaped(RecipeCategory.REDSTONE, ModBlocks.CYANITE_REPROCESSOR.get())
                .pattern("ICI")
                .pattern("BFB")
                .pattern("IRI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('C', ModTags.INGOTS_CYANITE)
                .define('B', Items.BUCKET)
                .define('F', Items.FURNACE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_cyanite", has(ModTags.INGOTS_CYANITE))
                .save(out);

        // Steam Turbine Multiblock Parts (Cyanite + Graphite + Iron progression)
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TURBINE_CASING.get(), 4)
                .pattern("IGI")
                .pattern("C C")
                .pattern("IGI")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', ModTags.INGOTS_GRAPHITE)
                .define('C', ModTags.INGOTS_CYANITE)
                .unlockedBy("has_cyanite", has(ModTags.INGOTS_CYANITE))
                .save(out);

        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TURBINE_GLASS.get(), 2)
                .pattern("GCG")
                .define('C', ModBlocks.TURBINE_CASING.get())
                .define('G', Tags.BlockItems.GLASS_BLOCKS.item())
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.TURBINE_CONTROLLER.get())
                .pattern("TYT")
                .pattern("RDR")
                .pattern("TYT")
                .define('T', ModBlocks.TURBINE_CASING.get())
                .define('Y', ModTags.INGOTS_CYANITE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(out);

        shaped(RecipeCategory.MISC, ModBlocks.TURBINE_ROTOR_BEARING.get())
                .pattern("TGT")
                .pattern("IDI")
                .pattern("TGT")
                .define('T', ModBlocks.TURBINE_CASING.get())
                .define('G', ModTags.INGOTS_CYANITE)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(out);

        shaped(RecipeCategory.MISC, ModBlocks.TURBINE_ROTOR_SHAFT.get(), 2)
                .pattern(" I ")
                .pattern("IGI")
                .pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', ModTags.INGOTS_CYANITE)
                .unlockedBy("has_cyanite", has(ModTags.INGOTS_CYANITE))
                .save(out);

        shaped(RecipeCategory.MISC, ModBlocks.TURBINE_ROTOR_BLADE.get(), 4)
                .pattern("IGI")
                .pattern("GCG")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', ModTags.INGOTS_GRAPHITE)
                .define('C', ModTags.INGOTS_CYANITE)
                .unlockedBy("has_cyanite", has(ModTags.INGOTS_CYANITE))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.TURBINE_FLUID_PORT.get())
                .pattern("TBT")
                .pattern("IPI")
                .pattern("TBT")
                .define('T', ModBlocks.TURBINE_CASING.get())
                .define('B', Items.BUCKET)
                .define('I', Tags.Items.INGOTS_IRON)
                .define('P', Items.PISTON)
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.TURBINE_POWER_TAP.get())
                .pattern("TRT")
                .pattern("RKR")
                .pattern("TRT")
                .define('T', ModBlocks.TURBINE_CASING.get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('K', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(out);

        shaped(RecipeCategory.REDSTONE, ModBlocks.TURBINE_REDSTONE_PORT.get())
                .pattern("TRT")
                .pattern("RGR")
                .pattern("TRT")
                .define('T', ModBlocks.TURBINE_CASING.get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has_turbine_casing", has(ModBlocks.TURBINE_CASING.get()))
                .save(out);
    }

    private void storageBlock(RecipeOutput out, TagKey<Item> ingotTag, Item ingot, ItemLike block, String name) {
        shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("III").pattern("III").pattern("III")
                .define('I', ingotTag)
                .unlockedBy("has_" + name, has(ingotTag))
                .save(out, key(name + "_block_from_ingots"));
        shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(block)
                .unlockedBy("has_" + name + "_block", has(block))
                .save(out, key(name + "_ingots_from_block"));
    }

    private void cook(RecipeOutput out, ItemLike input, ItemLike result, float xp, String name) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, CookingBookCategory.MISC, result, xp, 200)
                .unlockedBy("has_input", has(input))
                .save(out, key(name + "_smelting"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, CookingBookCategory.MISC, result, xp, 100)
                .unlockedBy("has_input", has(input))
                .save(out, key(name + "_blasting"));
    }

    private void cook(RecipeOutput out, TagKey<Item> input, ItemLike result, float xp, String name) {
        SimpleCookingRecipeBuilder.smelting(tag(input), RecipeCategory.MISC, CookingBookCategory.MISC, result, xp, 200)
                .unlockedBy("has_input", has(input))
                .save(out, key(name + "_smelting"));
        SimpleCookingRecipeBuilder.blasting(tag(input), RecipeCategory.MISC, CookingBookCategory.MISC, result, xp, 100)
                .unlockedBy("has_input", has(input))
                .save(out, key(name + "_blasting"));
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, JustReactors.id(path));
    }
}
