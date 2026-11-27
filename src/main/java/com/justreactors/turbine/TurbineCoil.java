package com.justreactors.turbine;

import com.justreactors.registry.ModBlocks;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
 * Electromagnetic induction coil materials placed around the turbine rotor shaft.
 * Lookup order: Java API overrides -> Datapack JSON entries (block, then tag) -> Built-in defaults.
 */
public record TurbineCoil(double efficiency, double bonus, double drag) {
    public record TagEntry(TagKey<Block> tag, TurbineCoil coil) {
    }

    public record DisplayEntry(
            List<ItemStack> stacks,
            @Nullable TagKey<Block> tag,
            TurbineCoil coil
    ) {
    }

    public static final TurbineCoil IRON = new TurbineCoil(1.0, 1.0, 1.0);
    public static final TurbineCoil COPPER = new TurbineCoil(1.1, 1.05, 1.05);
    public static final TurbineCoil GOLD = new TurbineCoil(1.6, 1.20, 1.40);
    public static final TurbineCoil DIAMOND = new TurbineCoil(2.2, 1.40, 1.60);
    public static final TurbineCoil EMERALD = new TurbineCoil(2.5, 1.50, 1.80);
    public static final TurbineCoil CYANITE = new TurbineCoil(2.0, 1.35, 1.40);
    public static final TurbineCoil BLUTONIUM = new TurbineCoil(3.0, 1.80, 2.00);
    public static final TurbineCoil NETHERITE = new TurbineCoil(3.5, 2.00, 2.20);

    private static final Map<Block, TurbineCoil> CUSTOM = new HashMap<>();
    private static volatile Map<Block, TurbineCoil> datapackBlocks = Map.of();
    private static volatile List<TagEntry> datapackTags = List.of();
    private static volatile int datapackCount = 0;

    public static void register(Block block, TurbineCoil coil) {
        CUSTOM.put(block, coil);
    }

    public static void setDatapackEntries(Map<Block, TurbineCoil> blocks, List<TagEntry> tags, int loadedCount) {
        datapackBlocks = Map.copyOf(blocks);
        datapackTags = List.copyOf(tags);
        datapackCount = loadedCount;
    }

    public static int getDatapackCount() {
        return datapackCount;
    }

    @Nullable
    public static TurbineCoil of(BlockState state) {
        Block block = state.getBlock();

        TurbineCoil custom = CUSTOM.get(block);
        if (custom != null) return custom;

        TurbineCoil dpBlock = datapackBlocks.get(block);
        if (dpBlock != null) return dpBlock;

        for (TagEntry entry : datapackTags) {
            if (state.is(entry.tag())) return entry.coil();
        }

        if (block == Blocks.IRON_BLOCK) return IRON;
        if (state.is(Tags.Blocks.STORAGE_BLOCKS_COPPER)) return COPPER;
        if (block == Blocks.GOLD_BLOCK) return GOLD;
        if (block == Blocks.DIAMOND_BLOCK) return DIAMOND;
        if (block == Blocks.EMERALD_BLOCK) return EMERALD;
        if (block == ModBlocks.CYANITE_BLOCK.get()) return CYANITE;
        if (block == ModBlocks.BLUTONIUM_BLOCK.get()) return BLUTONIUM;
        if (block == Blocks.NETHERITE_BLOCK) return NETHERITE;
        return null;
    }

    /**
     * Returns deduplicated display entries for JEI/UI, preferring datapack-loaded definitions
     * overlaid with Java API overrides, and falling back to built-in defaults before world load.
     */
    public static List<DisplayEntry> getDisplayEntries() {
        List<DisplayEntry> entries = new ArrayList<>();
        Set<Block> seenBlocks = new HashSet<>();

        if (!datapackBlocks.isEmpty() || !datapackTags.isEmpty()) {
            Map<Block, TurbineCoil> effectiveBlocks = new LinkedHashMap<>(datapackBlocks);
            effectiveBlocks.putAll(CUSTOM);

            for (Map.Entry<Block, TurbineCoil> e : effectiveBlocks.entrySet()) {
                Block block = e.getKey();
                if (!seenBlocks.add(block)) continue;
                Item item = block.asItem();
                if (item != Items.AIR) {
                    entries.add(new DisplayEntry(List.of(new ItemStack(item)), null, e.getValue()));
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
                    entries.add(new DisplayEntry(List.copyOf(stacks), tagEntry.tag(), tagEntry.coil()));
                }
            }
        } else {
            addFallbackBlock(entries, seenBlocks, Blocks.NETHERITE_BLOCK, NETHERITE);
            addFallbackBlock(entries, seenBlocks, ModBlocks.BLUTONIUM_BLOCK.get(), BLUTONIUM);
            addFallbackBlock(entries, seenBlocks, Blocks.EMERALD_BLOCK, EMERALD);
            addFallbackBlock(entries, seenBlocks, Blocks.DIAMOND_BLOCK, DIAMOND);
            addFallbackBlock(entries, seenBlocks, ModBlocks.CYANITE_BLOCK.get(), CYANITE);
            addFallbackBlock(entries, seenBlocks, Blocks.GOLD_BLOCK, GOLD);
            addFallbackTag(entries, seenBlocks, Tags.Blocks.STORAGE_BLOCKS_COPPER, Blocks.COPPER_BLOCK.weathering().unaffected(), COPPER);
            addFallbackBlock(entries, seenBlocks, Blocks.IRON_BLOCK, IRON);

            for (Map.Entry<Block, TurbineCoil> e : CUSTOM.entrySet()) {
                Block block = e.getKey();
                if (!seenBlocks.add(block)) continue;
                Item item = block.asItem();
                if (item != Items.AIR) {
                    entries.add(new DisplayEntry(List.of(new ItemStack(item)), null, e.getValue()));
                }
            }
        }

        entries.sort(Comparator.comparingDouble(
                (DisplayEntry e) -> e.coil().efficiency() * e.coil().bonus()).reversed());
        return List.copyOf(entries);
    }

    private static void addFallbackBlock(List<DisplayEntry> entries, Set<Block> seen, Block block, TurbineCoil coil) {
        if (!seen.add(block)) return;
        TurbineCoil effective = CUSTOM.getOrDefault(block, coil);
        Item item = block.asItem();
        if (item != Items.AIR) {
            entries.add(new DisplayEntry(List.of(new ItemStack(item)), null, effective));
        }
    }

    private static void addFallbackTag(List<DisplayEntry> entries, Set<Block> seen,
                                       TagKey<Block> tag, Block fallbackBlock, TurbineCoil coil) {
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
            entries.add(new DisplayEntry(List.copyOf(stacks), tag, coil));
        }
    }
}
