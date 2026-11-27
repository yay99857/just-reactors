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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

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

    private ModItems() {
    }

    private static DeferredItem<BlockItem> blockItem(DeferredBlock<? extends Block> block) {
        return ITEMS.registerItem(block.getId().getPath(),
                props -> new BlockItem(block.get(), props),
                Item.Properties::useBlockDescriptionPrefix);
    }

    private static DeferredItem<BlockItem> tooltipBlockItem(DeferredBlock<? extends Block> block, String tooltipKey) {
        return ITEMS.registerItem(block.getId().getPath(),
                props -> new TooltipBlockItem(block.get(), props, tooltipKey),
                Item.Properties::useBlockDescriptionPrefix);
    }

    private static final class TooltipBlockItem extends BlockItem {
        private final String tooltipKey;

        private TooltipBlockItem(Block block, Properties properties, String tooltipKey) {
            super(block, properties);
            this.tooltipKey = tooltipKey;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                    Consumer<Component> tooltipComponents, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltipComponents, flag);
            tooltipComponents.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
