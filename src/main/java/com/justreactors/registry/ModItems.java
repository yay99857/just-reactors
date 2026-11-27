package com.justreactors.registry;

import com.justreactors.JustReactors;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JustReactors.MOD_ID);

    // Materials & fluids
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium");
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot");
    public static final DeferredItem<Item> GRAPHITE_INGOT = ITEMS.registerSimpleItem("graphite_ingot");
    public static final DeferredItem<Item> CYANITE_INGOT = ITEMS.registerSimpleItem("cyanite_ingot");
    public static final DeferredItem<Item> BLUTONIUM_INGOT = ITEMS.registerSimpleItem("blutonium_ingot");

    public static final DeferredItem<BucketItem> STEAM_BUCKET = ITEMS.registerItem("steam_bucket",
            props -> new BucketItem(ModFluids.STEAM_SOURCE.get(), props.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BucketItem> COOLANT_BUCKET = ITEMS.registerItem("coolant_bucket",
            props -> new BucketItem(ModFluids.COOLANT_SOURCE.get(), props.craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> HOT_COOLANT_BUCKET = ITEMS.registerItem("hot_coolant_bucket",
            props -> new BucketItem(ModFluids.HOT_COOLANT_SOURCE.get(), props.craftRemainder(Items.BUCKET).stacksTo(1)));

    // Block items
    public static final DeferredItem<BlockItem> URANIUM_ORE = blockItem(ModBlocks.URANIUM_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_URANIUM_ORE = blockItem(ModBlocks.DEEPSLATE_URANIUM_ORE);
    public static final DeferredItem<BlockItem> URANIUM_BLOCK = blockItem(ModBlocks.URANIUM_BLOCK);
    public static final DeferredItem<BlockItem> GRAPHITE_BLOCK = blockItem(ModBlocks.GRAPHITE_BLOCK);
    public static final DeferredItem<BlockItem> CYANITE_BLOCK = blockItem(ModBlocks.CYANITE_BLOCK);
    public static final DeferredItem<BlockItem> BLUTONIUM_BLOCK = blockItem(ModBlocks.BLUTONIUM_BLOCK);

    public static final DeferredItem<BlockItem> CYANITE_REPROCESSOR = tooltipBlockItem(
            ModBlocks.CYANITE_REPROCESSOR, "tooltip.justreactors.cyanite_reprocessor");

    public static final DeferredItem<BlockItem> REACTOR_CASING = blockItem(ModBlocks.REACTOR_CASING);
    public static final DeferredItem<BlockItem> REACTOR_GLASS = blockItem(ModBlocks.REACTOR_GLASS);
    public static final DeferredItem<BlockItem> REACTOR_CONTROLLER = tooltipBlockItem(
            ModBlocks.REACTOR_CONTROLLER, "tooltip.justreactors.reactor_controller");
    public static final DeferredItem<BlockItem> REACTOR_FUEL_ROD = blockItem(ModBlocks.REACTOR_FUEL_ROD);
    public static final DeferredItem<BlockItem> REACTOR_CONTROL_ROD = blockItem(ModBlocks.REACTOR_CONTROL_ROD);
    public static final DeferredItem<BlockItem> REACTOR_POWER_TAP = blockItem(ModBlocks.REACTOR_POWER_TAP);
    public static final DeferredItem<BlockItem> REACTOR_ACCESS_PORT = blockItem(ModBlocks.REACTOR_ACCESS_PORT);
    public static final DeferredItem<BlockItem> REACTOR_REDSTONE_PORT = blockItem(ModBlocks.REACTOR_REDSTONE_PORT);
    public static final DeferredItem<BlockItem> REACTOR_COOLANT_PORT = tooltipBlockItem(
            ModBlocks.REACTOR_COOLANT_PORT, "tooltip.justreactors.reactor_coolant_port");

    public static final DeferredItem<BlockItem> TURBINE_CASING = blockItem(ModBlocks.TURBINE_CASING);
    public static final DeferredItem<BlockItem> TURBINE_GLASS = blockItem(ModBlocks.TURBINE_GLASS);
    public static final DeferredItem<BlockItem> TURBINE_CONTROLLER = tooltipBlockItem(
            ModBlocks.TURBINE_CONTROLLER, "tooltip.justreactors.turbine_controller");
    public static final DeferredItem<BlockItem> TURBINE_ROTOR_BEARING = blockItem(ModBlocks.TURBINE_ROTOR_BEARING);
    public static final DeferredItem<BlockItem> TURBINE_ROTOR_SHAFT = blockItem(ModBlocks.TURBINE_ROTOR_SHAFT);
    public static final DeferredItem<BlockItem> TURBINE_ROTOR_BLADE = blockItem(ModBlocks.TURBINE_ROTOR_BLADE);
    public static final DeferredItem<BlockItem> TURBINE_FLUID_PORT = tooltipBlockItem(
            ModBlocks.TURBINE_FLUID_PORT, "tooltip.justreactors.turbine_fluid_port");
    public static final DeferredItem<BlockItem> TURBINE_POWER_TAP = blockItem(ModBlocks.TURBINE_POWER_TAP);
    public static final DeferredItem<BlockItem> TURBINE_REDSTONE_PORT = tooltipBlockItem(
            ModBlocks.TURBINE_REDSTONE_PORT, "tooltip.justreactors.turbine_redstone_port");

    public static final DeferredItem<BlockItem> HEAT_EXCHANGER_CASING = blockItem(ModBlocks.HEAT_EXCHANGER_CASING);
    public static final DeferredItem<BlockItem> HEAT_EXCHANGER_GLASS = blockItem(ModBlocks.HEAT_EXCHANGER_GLASS);
    public static final DeferredItem<BlockItem> HEAT_EXCHANGER_CONTROLLER = tooltipBlockItem(
            ModBlocks.HEAT_EXCHANGER_CONTROLLER, "tooltip.justreactors.heat_exchanger_controller");
    public static final DeferredItem<BlockItem> HEAT_EXCHANGER_FLUID_PORT = tooltipBlockItem(
            ModBlocks.HEAT_EXCHANGER_FLUID_PORT, "tooltip.justreactors.heat_exchanger_fluid_port");
    public static final DeferredItem<BlockItem> HEAT_EXCHANGER_TUBE = tooltipBlockItem(
            ModBlocks.HEAT_EXCHANGER_TUBE, "tooltip.justreactors.heat_exchanger_tube");

    public static final DeferredItem<BlockItem> BATTERY_CASING = blockItem(ModBlocks.BATTERY_CASING);
    public static final DeferredItem<BlockItem> BATTERY_GLASS = blockItem(ModBlocks.BATTERY_GLASS);
    public static final DeferredItem<BlockItem> BATTERY_CONTROLLER = tooltipBlockItem(
            ModBlocks.BATTERY_CONTROLLER, "tooltip.justreactors.battery_controller");
    public static final DeferredItem<BlockItem> BATTERY_POWER_PORT = tooltipBlockItem(
            ModBlocks.BATTERY_POWER_PORT, "tooltip.justreactors.battery_power_port");
    public static final DeferredItem<BlockItem> BATTERY_REDSTONE_SENSOR = tooltipBlockItem(
            ModBlocks.BATTERY_REDSTONE_SENSOR, "tooltip.justreactors.battery_redstone_sensor");
    public static final DeferredItem<BlockItem> BATTERY_CELL = tooltipBlockItem(
            ModBlocks.BATTERY_CELL, "tooltip.justreactors.battery_cell");

    private ModItems() {
    }

    private static DeferredItem<BlockItem> blockItem(DeferredBlock<? extends Block> block) {
        return ITEMS.registerItem(block.getId().getPath(),
                props -> new BlockItem(block.get(), props));
    }

    private static DeferredItem<BlockItem> tooltipBlockItem(DeferredBlock<? extends Block> block, String tooltipKey) {
        return ITEMS.registerItem(block.getId().getPath(),
                props -> new TooltipBlockItem(block.get(), props, tooltipKey));
    }

    private static final class TooltipBlockItem extends BlockItem {
        private final String tooltipKey;

        private TooltipBlockItem(Block block, Properties properties, String tooltipKey) {
            super(block, properties);
            this.tooltipKey = tooltipKey;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context,
                                    List<Component> tooltipComponents, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltipComponents, flag);
            tooltipComponents.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
