package com.justreactors.compat.rei;

import com.justreactors.JustReactors;
import com.justreactors.JustReactorsConfig;
import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.exchanger.ExchangerConversion;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.registry.ModItems;
import com.justreactors.turbine.TurbineCoil;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Native Roughly Enough Items support: reprocessing, moderators, turbine coils and the heat exchanger.
 * Only class-loaded by REI itself, so the mod runs fine without it.
 */
@REIPluginClient
public class JustReactorsReiPlugin implements REIClientPlugin {
    static final CategoryIdentifier<JrDisplay> REPROCESSING = CategoryIdentifier.of(JustReactors.id("reprocessing"));
    static final CategoryIdentifier<JrDisplay> MODERATORS = CategoryIdentifier.of(JustReactors.id("moderators"));
    static final CategoryIdentifier<JrDisplay> COILS = CategoryIdentifier.of(JustReactors.id("turbine_coils"));
    static final CategoryIdentifier<JrDisplay> EXCHANGER = CategoryIdentifier.of(JustReactors.id("heat_exchanging"));

    private static final int TEXT = 0xFF404040;
    private static final int TEXT_DARK = 0xFFBBBBBB;

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new JrCategory(REPROCESSING, "jei.justreactors.reprocessing", ModItems.CYANITE_REPROCESSOR.get().getDefaultInstance(), 52, (d, b, w) -> {
            w.add(slot(b, 4, 10, d.getInputEntries().get(0)).markInput());
            w.add(slot(b, 26, 10, d.getInputEntries().get(1)).markInput());
            w.add(Widgets.createArrow(new Point(b.x + 52, b.y + 10)));
            w.add(Widgets.createResultSlotBackground(new Point(b.x + 88, b.y + 10)));
            w.add(slot(b, 88, 10, d.getOutputEntries().get(0)).disableBackground().markOutput());
            int[] info = (int[]) d.data();
            w.add(label(b, 4, 30, Component.translatable("jei.justreactors.reprocessing.info",
                    String.format(Locale.ROOT, "%.1f", info[1] / 20.0), String.format(Locale.ROOT, "%,d", info[0]))));
            w.add(label(b, 4, 40, Component.translatable("rei.justreactors.water", String.format(Locale.ROOT, "%,d", info[2]))));
        }));
        registry.add(new JrCategory(MODERATORS, "jei.justreactors.moderators", ModItems.REACTOR_FUEL_ROD.get().getDefaultInstance(), 42, (d, b, w) -> {
            w.add(slot(b, 6, 13, d.getInputEntries().get(0)).markInput());
            ReactorModerator m = (ReactorModerator) d.data();
            w.add(label(b, 28, 4, Component.translatable("jei.justreactors.moderator.absorption",
                    String.format(Locale.ROOT, "%.0f", m.absorption() * 100.0), String.format(Locale.ROOT, "%.0f", m.heatEfficiency() * 100.0))));
            w.add(label(b, 28, 16, Component.translatable("jei.justreactors.moderator.moderation",
                    String.format(Locale.ROOT, "%.2f", m.moderation()))));
            w.add(label(b, 28, 28, Component.translatable("jei.justreactors.moderator.conductivity",
                    String.format(Locale.ROOT, "%.2f", m.heatConductivity()))));
        }));
        registry.add(new JrCategory(COILS, "jei.justreactors.turbine_coils", ModItems.TURBINE_ROTOR_SHAFT.get().getDefaultInstance(), 42, (d, b, w) -> {
            w.add(slot(b, 6, 13, d.getInputEntries().get(0)).markInput());
            TurbineCoil c = (TurbineCoil) d.data();
            w.add(label(b, 28, 4, Component.translatable("jei.justreactors.coil.efficiency", String.format(Locale.ROOT, "%.2f", c.efficiency()))));
            w.add(label(b, 28, 16, Component.translatable("jei.justreactors.coil.bonus", String.format(Locale.ROOT, "%.2f", c.bonus()))));
            w.add(label(b, 28, 28, Component.translatable("jei.justreactors.coil.drag", String.format(Locale.ROOT, "%.2f", c.drag()))));
        }));
        registry.add(new JrCategory(EXCHANGER, "jei.justreactors.heat_exchanging", ModItems.HEAT_EXCHANGER_CONTROLLER.get().getDefaultInstance(), 60, (d, b, w) -> {
            w.add(slot(b, 4, 4, d.getInputEntries().get(0)).markInput());
            w.add(slot(b, 24, 4, d.getInputEntries().get(1)).markInput());
            w.add(Widgets.createArrow(new Point(b.x + 46, b.y + 4)));
            w.add(slot(b, 80, 4, d.getOutputEntries().get(0)).markOutput());
            w.add(slot(b, 100, 4, d.getOutputEntries().get(1)).markOutput());
            ExchangerConversion r = (ExchangerConversion) d.data();
            w.add(label(b, 4, 26, Component.translatable("rei.justreactors.exchange",
                    String.format(Locale.ROOT, "%,d", r.hotCoolantMb()), String.format(Locale.ROOT, "%,d", r.waterMb()))));
            w.add(label(b, 4, 37, Component.translatable("jei.justreactors.heat_exchanging.ratio", r.steamMb() / Math.max(1, r.hotCoolantMb()))));
            w.add(label(b, 4, 48, Component.translatable("jei.justreactors.heat_exchanging.tube", String.format(Locale.ROOT, "%,d", r.perTubeMb()))));
        }));

        registry.addWorkstations(REPROCESSING, EntryStacks.of(ModBlocks.CYANITE_REPROCESSOR.get()));
        registry.addWorkstations(MODERATORS, EntryStacks.of(ModBlocks.REACTOR_CONTROLLER.get()), EntryStacks.of(ModBlocks.REACTOR_FUEL_ROD.get()));
        registry.addWorkstations(COILS, EntryStacks.of(ModBlocks.TURBINE_CONTROLLER.get()), EntryStacks.of(ModBlocks.TURBINE_ROTOR_SHAFT.get()));
        registry.addWorkstations(EXCHANGER, EntryStacks.of(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get()), EntryStacks.of(ModBlocks.HEAT_EXCHANGER_TUBE.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(new JrDisplay(REPROCESSING,
                List.of(EntryIngredient.of(EntryStacks.of(new ItemStack(ModItems.CYANITE_INGOT.get(), CyaniteReprocessorBlockEntity.CYANITE_PER_OP))),
                        fluid(Fluids.WATER, JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_WATER_COST))),
                List.of(EntryIngredient.of(EntryStacks.of(ModItems.BLUTONIUM_INGOT.get()))),
                new int[]{JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_ENERGY_COST), JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_TICKS),
                        JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_WATER_COST)}));

        for (ReactorModerator.DisplayEntry e : ReactorModerator.getDisplayEntries()) {
            EntryIngredient in = e.fluid() != null ? fluid(e.fluid(), 1_000) : items(e.stacks());
            registry.add(new JrDisplay(MODERATORS, List.of(in), List.of(), e.moderator()));
        }
        for (TurbineCoil.DisplayEntry e : TurbineCoil.getDisplayEntries()) {
            registry.add(new JrDisplay(COILS, List.of(items(e.stacks())), List.of(), e.coil()));
        }

        ExchangerConversion r = ExchangerConversion.current();
        registry.add(new JrDisplay(EXCHANGER,
                List.of(fluid(ModFluids.HOT_COOLANT_SOURCE.get(), r.hotCoolantMb()), fluid(Fluids.WATER, r.waterMb())),
                List.of(fluid(ModFluids.COOLANT_SOURCE.get(), r.coolantMb()), fluid(ModFluids.STEAM_SOURCE.get(), r.steamMb())),
                r));
    }

    /**
     * REI on 26.3 draws fluid entries as empty slots, so fluids are shown as their buckets and the
     * amounts are written in the display text.
     */
    private static EntryIngredient fluid(Fluid fluid, long mb) {
        Item bucket = fluid.isSame(Fluids.WATER) ? Items.WATER_BUCKET : fluid.getBucket();
        return EntryIngredient.of(EntryStacks.of(bucket == Items.AIR ? Items.BUCKET : bucket));
    }

    private static EntryIngredient items(List<ItemStack> stacks) {
        List<EntryStack<ItemStack>> list = new ArrayList<>();
        for (ItemStack s : stacks) list.add(EntryStacks.of(s));
        return EntryIngredient.of(list);
    }

    private static me.shedaniel.rei.api.client.gui.widgets.Slot slot(Rectangle b, int x, int y, EntryIngredient entries) {
        return Widgets.createSlot(new Point(b.x + x + 1, b.y + y + 1)).entries(entries);
    }

    private static Widget label(Rectangle b, int x, int y, Component text) {
        return Widgets.createLabel(new Point(b.x + x, b.y + y), text).leftAligned().noShadow().color(TEXT, TEXT_DARK);
    }

    /** Client-only display carrying the numbers its category draws. Not synced, so it has no serializer. */
    static final class JrDisplay extends BasicDisplay {
        private final CategoryIdentifier<JrDisplay> category;
        private final Object data;

        JrDisplay(CategoryIdentifier<JrDisplay> category, List<EntryIngredient> inputs, List<EntryIngredient> outputs, Object data) {
            super(inputs, outputs);
            this.category = category;
            this.data = data;
        }

        Object data() {
            return data;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return category;
        }

        @Override
        public DisplaySerializer<? extends me.shedaniel.rei.api.common.display.Display> getSerializer() {
            return null;
        }
    }

    @FunctionalInterface
    interface Layout {
        void build(JrDisplay display, Rectangle bounds, List<Widget> widgets);
    }

    static final class JrCategory implements DisplayCategory<JrDisplay> {
        private final CategoryIdentifier<JrDisplay> id;
        private final String titleKey;
        private final Renderer icon;
        private final int height;
        private final Layout layout;

        JrCategory(CategoryIdentifier<JrDisplay> id, String titleKey, ItemStack icon, int height, Layout layout) {
            this.id = id;
            this.titleKey = titleKey;
            this.icon = EntryStacks.of(icon);
            this.height = height;
            this.layout = layout;
        }

        @Override
        public CategoryIdentifier<? extends JrDisplay> getCategoryIdentifier() {
            return id;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(titleKey);
        }

        @Override
        public Renderer getIcon() {
            return icon;
        }

        @Override
        public int getDisplayHeight() {
            return height + 8;
        }

        @Override
        public int getDisplayWidth(JrDisplay display) {
            return 214;
        }

        @Override
        public List<Widget> setupDisplay(JrDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            Rectangle inner = new Rectangle(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 8);
            layout.build(display, inner, widgets);
            return widgets;
        }
    }
}
