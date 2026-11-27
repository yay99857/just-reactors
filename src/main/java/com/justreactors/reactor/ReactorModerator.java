package com.justreactors.reactor;

import com.justreactors.registry.ModBlocks;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thermal and neutron-moderation properties of blocks placed inside the reactor interior.
 * Lookup order: Java API overrides -> Datapack JSON entries (block, then tag) -> Built-in defaults.
 */
public record ReactorModerator(
        double absorption,
        double heatEfficiency,
        double moderation,
        double heatConductivity
) {
    public record TagEntry(TagKey<Block> tag, ReactorModerator moderator) {
    }

    public record DisplayEntry(
            List<ItemStack> stacks,
            @Nullable Fluid fluid,
            @Nullable TagKey<Block> tag,
            ReactorModerator moderator
    ) {
    }

    public static final ReactorModerator AIR = new ReactorModerator(0.10, 0.25, 1.10, 0.05);
    public static final ReactorModerator WATER = new ReactorModerator(0.33, 0.50, 1.33, 0.10);
    public static final ReactorModerator IRON = new ReactorModerator(0.50, 0.75, 1.40, 0.60);
    public static final ReactorModerator COPPER = new ReactorModerator(0.48, 0.72, 1.38, 0.75);
    public static final ReactorModerator GOLD = new ReactorModerator(0.52, 0.80, 1.45, 2.00);
    public static final ReactorModerator GRAPHITE = new ReactorModerator(0.10, 0.50, 2.00, 2.00);
    public static final ReactorModerator REDSTONE = new ReactorModerator(0.75, 0.55, 1.60, 0.90);
    public static final ReactorModerator DIAMOND = new ReactorModerator(0.55, 0.85, 1.50, 3.00);
    public static final ReactorModerator EMERALD = new ReactorModerator(0.55, 0.80, 1.50, 2.50);
    public static final ReactorModerator PACKED_ICE = new ReactorModerator(0.45, 0.65, 1.35, 1.50);
    public static final ReactorModerator BLUE_ICE = new ReactorModerator(0.66, 0.95, 2.20, 3.50);
    public static final ReactorModerator NETHERITE = new ReactorModerator(0.85, 0.95, 2.50, 4.50);

    private static final Map<Block, ReactorModerator> CUSTOM = new HashMap<>();
    private static volatile Map<Block, ReactorModerator> datapackBlocks = Map.of();
    private static volatile List<TagEntry> datapackTags = List.of();
    private static volatile int datapackCount = 0;

    public static void register(Block block, ReactorModerator moderator) {
        CUSTOM.put(block, moderator);
    }

    public static void setDatapackEntries(Map<Block, ReactorModerator> blocks, List<TagEntry> tags, int loadedCount) {
        datapackBlocks = Map.copyOf(blocks);
        datapackTags = List.copyOf(tags);
        datapackCount = loadedCount;
    }

    public static int getDatapackCount() {
        return datapackCount;
    }

    public static ReactorModerator of(BlockState state) {
        if (state.isAir()) return AIR;
        Block block = state.getBlock();

        ReactorModerator custom = CUSTOM.get(block);
        if (custom != null) return custom;

        ReactorModerator dpBlock = datapackBlocks.get(block);
        if (dpBlock != null) return dpBlock;

        for (TagEntry entry : datapackTags) {
            if (state.is(entry.tag())) return entry.moderator();
        }

        if (block == Blocks.WATER) return WATER;
        if (block == Blocks.IRON_BLOCK) return IRON;
        if (state.is(Tags.Blocks.STORAGE_BLOCKS_COPPER)) return COPPER;
        if (block == Blocks.GOLD_BLOCK) return GOLD;
        if (block == ModBlocks.GRAPHITE_BLOCK.get()) return GRAPHITE;
        if (block == Blocks.REDSTONE_BLOCK) return REDSTONE;
        if (block == Blocks.DIAMOND_BLOCK) return DIAMOND;
        if (block == Blocks.EMERALD_BLOCK) return EMERALD;
        if (block == Blocks.PACKED_ICE) return PACKED_ICE;
        if (block == Blocks.BLUE_ICE) return BLUE_ICE;
        if (block == Blocks.NETHERITE_BLOCK) return NETHERITE;
        return AIR;
    }

    /**
     * Returns deduplicated display entries for JEI/UI, preferring datapack-loaded definitions
     * overlaid with Java API overrides, and falling back to built-in defaults before world load.
     */
    public static List<DisplayEntry> getDisplayEntries() {
        List<DisplayEntry> entries = new ArrayList<>();
        Set<Block> seenBlocks = new HashSet<>();

        if (!datapackBlocks.isEmpty() || !datapackTags.isEmpty()) {
            Map<Block, ReactorModerator> effectiveBlocks = new LinkedHashMap<>(datapackBlocks);
            effectiveBlocks.putAll(CUSTOM);

            for (Map.Entry<Block, ReactorModerator> e : effectiveBlocks.entrySet()) {
                Block block = e.getKey();
                if (!seenBlocks.add(block)) continue;
                DisplayEntry entry = blockToDisplayEntry(block, e.getValue());
                if (entry != null) {
                    entries.add(entry);
                }
            }

            for (TagEntry tagEntry : datapackTags) {
                List<ItemStack> stacks = new ArrayList<>();
                for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tagEntry.tag())) {
                    Block block = holder.value();
                    if (seenBlocks.contains(block)) continue;
                    Item item = block.asItem();
                    if (item == Items.AIR) continue;
                    seenBlocks.add(block);
                    stacks.add(new ItemStack(item));
                }
                if (!stacks.isEmpty()) {
                    entries.add(new DisplayEntry(List.copyOf(stacks), null, tagEntry.tag(), tagEntry.moderator()));
                }
            }
        } else {
            addFallbackBlock(entries, seenBlocks, Blocks.WATER, WATER);
            addFallbackBlock(entries, seenBlocks, Blocks.IRON_BLOCK, IRON);
            addFallbackTag(entries, seenBlocks, Tags.Blocks.STORAGE_BLOCKS_COPPER, Blocks.COPPER_BLOCK, COPPER);
            addFallbackBlock(entries, seenBlocks, Blocks.GOLD_BLOCK, GOLD);
            addFallbackBlock(entries, seenBlocks, ModBlocks.GRAPHITE_BLOCK.get(), GRAPHITE);
            addFallbackBlock(entries, seenBlocks, Blocks.REDSTONE_BLOCK, REDSTONE);
            addFallbackBlock(entries, seenBlocks, Blocks.DIAMOND_BLOCK, DIAMOND);
            addFallbackBlock(entries, seenBlocks, Blocks.EMERALD_BLOCK, EMERALD);
            addFallbackBlock(entries, seenBlocks, Blocks.PACKED_ICE, PACKED_ICE);
            addFallbackBlock(entries, seenBlocks, Blocks.BLUE_ICE, BLUE_ICE);
            addFallbackBlock(entries, seenBlocks, Blocks.NETHERITE_BLOCK, NETHERITE);

            for (Map.Entry<Block, ReactorModerator> e : CUSTOM.entrySet()) {
                Block block = e.getKey();
                if (!seenBlocks.add(block)) continue;
                DisplayEntry entry = blockToDisplayEntry(block, e.getValue());
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        entries.sort(Comparator.comparingDouble(
                (DisplayEntry e) -> e.moderator().moderation() * e.moderator().heatConductivity()).reversed());
        return List.copyOf(entries);
    }

    private static void addFallbackBlock(List<DisplayEntry> entries, Set<Block> seen, Block block, ReactorModerator mod) {
        if (!seen.add(block)) return;
        ReactorModerator effective = CUSTOM.getOrDefault(block, mod);
        DisplayEntry entry = blockToDisplayEntry(block, effective);
        if (entry != null) {
            entries.add(entry);
        }
    }

    private static void addFallbackTag(List<DisplayEntry> entries, Set<Block> seen,
                                       TagKey<Block> tag, Block fallbackBlock, ReactorModerator mod) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            Block b = holder.value();
            if (seen.contains(b)) continue;
            Item item = b.asItem();
            if (item == Items.AIR) continue;
            seen.add(b);
            stacks.add(new ItemStack(item));
        }
        if (stacks.isEmpty() && seen.add(fallbackBlock)) {
            Item item = fallbackBlock.asItem();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        if (!stacks.isEmpty()) {
            entries.add(new DisplayEntry(List.copyOf(stacks), null, tag, mod));
        }
    }

    @Nullable
    private static DisplayEntry blockToDisplayEntry(Block block, ReactorModerator mod) {
        if (block instanceof LiquidBlock liquid) {
            return new DisplayEntry(List.of(), liquid.fluid, null, mod);
        }
        if (block == Blocks.WATER) {
            return new DisplayEntry(List.of(), Fluids.WATER, null, mod);
        }
        Item item = block.asItem();
        if (item == Items.AIR) return null;
        return new DisplayEntry(List.of(new ItemStack(item)), null, null, mod);
    }
}
