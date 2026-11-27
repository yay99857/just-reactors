package com.justreactors.compat.emi;

import com.justreactors.JustReactors;
import com.justreactors.JustReactorsConfig;
import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.exchanger.ExchangerConversion;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.registry.ModItems;
import com.justreactors.turbine.TurbineCoil;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Native EMI support: reprocessing, moderators, turbine coils and the heat exchanger.
 * Only class-loaded by EMI itself, so the mod runs fine without it.
 */
@EmiEntrypoint
public class JustReactorsEmiPlugin implements EmiPlugin {
    private static final int TEXT = 0xFF404040;

    static final EmiRecipeCategory REPROCESSING = new EmiRecipeCategory(JustReactors.id("reprocessing"),
            EmiStack.of(ModItems.CYANITE_REPROCESSOR.get())) {
        @Override
        public Component getName() {
            return Component.translatable("jei.justreactors.reprocessing");
        }
    };
    static final EmiRecipeCategory MODERATORS = new EmiRecipeCategory(JustReactors.id("moderators"),
            EmiStack.of(ModItems.REACTOR_FUEL_ROD.get())) {
        @Override
        public Component getName() {
            return Component.translatable("jei.justreactors.moderators");
        }
    };
    static final EmiRecipeCategory COILS = new EmiRecipeCategory(JustReactors.id("turbine_coils"),
            EmiStack.of(ModItems.TURBINE_ROTOR_SHAFT.get())) {
        @Override
        public Component getName() {
            return Component.translatable("jei.justreactors.turbine_coils");
        }
    };
    static final EmiRecipeCategory EXCHANGER = new EmiRecipeCategory(JustReactors.id("heat_exchanging"),
            EmiStack.of(ModItems.HEAT_EXCHANGER_CONTROLLER.get())) {
        @Override
        public Component getName() {
            return Component.translatable("jei.justreactors.heat_exchanging");
        }
    };

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(REPROCESSING);
        registry.addCategory(MODERATORS);
        registry.addCategory(COILS);
        registry.addCategory(EXCHANGER);
        registry.addWorkstation(REPROCESSING, EmiStack.of(ModBlocks.CYANITE_REPROCESSOR.get()));
        registry.addWorkstation(MODERATORS, EmiStack.of(ModBlocks.REACTOR_CONTROLLER.get()));
        registry.addWorkstation(MODERATORS, EmiStack.of(ModBlocks.REACTOR_FUEL_ROD.get()));
        registry.addWorkstation(COILS, EmiStack.of(ModBlocks.TURBINE_CONTROLLER.get()));
        registry.addWorkstation(COILS, EmiStack.of(ModBlocks.TURBINE_ROTOR_SHAFT.get()));
        registry.addWorkstation(EXCHANGER, EmiStack.of(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get()));
        registry.addWorkstation(EXCHANGER, EmiStack.of(ModBlocks.HEAT_EXCHANGER_TUBE.get()));

        int water = JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_WATER_COST);
        int energy = JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_ENERGY_COST);
        int ticks = JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_TICKS);
        registry.addRecipe(new Simple(REPROCESSING, "reprocessing/blutonium",
                List.of(EmiStack.of(new ItemStack(ModItems.CYANITE_INGOT.get(), CyaniteReprocessorBlockEntity.CYANITE_PER_OP)),
                        EmiStack.of(Fluids.WATER, water)),
                List.of(EmiStack.of(ModItems.BLUTONIUM_INGOT.get())), 120, 40, w -> {
            w.addSlot(EmiStack.of(new ItemStack(ModItems.CYANITE_INGOT.get(), CyaniteReprocessorBlockEntity.CYANITE_PER_OP)), 4, 4);
            w.addSlot(EmiStack.of(Fluids.WATER, water), 24, 4);
            w.addFillingArrow(50, 5, Math.max(1, ticks) * 50);
            w.addSlot(EmiStack.of(ModItems.BLUTONIUM_INGOT.get()), 88, 4);
            w.addText(Component.translatable("jei.justreactors.reprocessing.info",
                    String.format(Locale.ROOT, "%.1f", ticks / 20.0), String.format(Locale.ROOT, "%,d", energy)), 4, 28, TEXT, false);
        }));

        int i = 0;
        for (ReactorModerator.DisplayEntry e : ReactorModerator.getDisplayEntries()) {
            EmiIngredient in = e.fluid() != null ? EmiStack.of(e.fluid(), 1_000) : items(e.stacks());
            ReactorModerator m = e.moderator();
            registry.addRecipe(new Simple(MODERATORS, "moderators/" + i++, List.of(in), List.of(), 200, 42, w -> {
                w.addSlot(in, 4, 12);
                w.addText(Component.translatable("jei.justreactors.moderator.absorption",
                        String.format(Locale.ROOT, "%.0f", m.absorption() * 100.0), String.format(Locale.ROOT, "%.0f", m.heatEfficiency() * 100.0)),
                        28, 4, TEXT, false);
                w.addText(Component.translatable("jei.justreactors.moderator.moderation", String.format(Locale.ROOT, "%.2f", m.moderation())),
                        28, 16, TEXT, false);
                w.addText(Component.translatable("jei.justreactors.moderator.conductivity", String.format(Locale.ROOT, "%.2f", m.heatConductivity())),
                        28, 28, TEXT, false);
            }));
        }
        i = 0;
        for (TurbineCoil.DisplayEntry e : TurbineCoil.getDisplayEntries()) {
            EmiIngredient in = items(e.stacks());
            TurbineCoil c = e.coil();
            registry.addRecipe(new Simple(COILS, "turbine_coils/" + i++, List.of(in), List.of(), 160, 42, w -> {
                w.addSlot(in, 4, 12);
                w.addText(Component.translatable("jei.justreactors.coil.efficiency", String.format(Locale.ROOT, "%.2f", c.efficiency())), 28, 4, TEXT, false);
                w.addText(Component.translatable("jei.justreactors.coil.bonus", String.format(Locale.ROOT, "%.2f", c.bonus())), 28, 16, TEXT, false);
                w.addText(Component.translatable("jei.justreactors.coil.drag", String.format(Locale.ROOT, "%.2f", c.drag())), 28, 28, TEXT, false);
            }));
        }

        ExchangerConversion r = ExchangerConversion.current();
        EmiStack hot = EmiStack.of(ModFluids.HOT_COOLANT_SOURCE.get(), r.hotCoolantMb());
        EmiStack waterIn = EmiStack.of(Fluids.WATER, r.waterMb());
        EmiStack coolant = EmiStack.of(ModFluids.COOLANT_SOURCE.get(), r.coolantMb());
        EmiStack steam = EmiStack.of(ModFluids.STEAM_SOURCE.get(), r.steamMb());
        registry.addRecipe(new Simple(EXCHANGER, "heat_exchanging/coolant", List.of(hot, waterIn), List.of(coolant, steam), 196, 50, w -> {
            w.addSlot(hot, 4, 4);
            w.addSlot(waterIn, 24, 4);
            w.addTexture(EmiTexture.EMPTY_ARROW, 48, 5);
            w.addSlot(coolant, 82, 4);
            w.addSlot(steam, 102, 4);
            w.addText(Component.translatable("jei.justreactors.heat_exchanging.ratio", r.steamMb() / Math.max(1, r.hotCoolantMb())),
                    4, 28, TEXT, false);
            w.addText(Component.translatable("jei.justreactors.heat_exchanging.tube", String.format(Locale.ROOT, "%,d", r.perTubeMb())),
                    4, 40, TEXT, false);
        }));
    }

    private static EmiIngredient items(List<ItemStack> stacks) {
        List<EmiIngredient> list = new ArrayList<>();
        for (ItemStack s : stacks) list.add(EmiStack.of(s));
        return EmiIngredient.of(list);
    }

    /** A recipe page whose widgets are drawn by a lambda. Output slots get recipe context in addWidgets. */
    private static final class Simple implements EmiRecipe {
        private final EmiRecipeCategory category;
        private final ResourceLocation id;
        private final List<EmiIngredient> inputs;
        private final List<EmiStack> outputs;
        private final int width;
        private final int height;
        private final Consumer<WidgetHolder> layout;

        Simple(EmiRecipeCategory category, String path, List<EmiIngredient> inputs, List<EmiStack> outputs,
               int width, int height, Consumer<WidgetHolder> layout) {
            this.category = category;
            this.id = JustReactors.id("/" + path);
            this.inputs = inputs;
            this.outputs = outputs;
            this.width = width;
            this.height = height;
            this.layout = layout;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return category;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return id;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return inputs;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return outputs;
        }

        @Override
        public int getDisplayWidth() {
            return width;
        }

        @Override
        public int getDisplayHeight() {
            return height;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            layout.accept(widgets);
        }

        @Override
        public boolean supportsRecipeTree() {
            return false;
        }
    }
}
