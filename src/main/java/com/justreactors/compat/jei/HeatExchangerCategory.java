package com.justreactors.compat.jei;

import com.justreactors.JustReactors;
import com.justreactors.exchanger.ExchangerConversion;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;

import java.util.Locale;

/** JEI page for the Heat Exchanger: Hot Coolant + Water -> Coolant + Steam. */
public class HeatExchangerCategory implements IRecipeCategory<ExchangerConversion> {
    public static final IRecipeType<ExchangerConversion> TYPE =
            IRecipeType.create(JustReactors.id("heat_exchanging"), ExchangerConversion.class);

    private static final int WIDTH = 120;
    private static final int HEIGHT = 56;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public HeatExchangerCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(40);
    }

    @Override
    public IRecipeType<ExchangerConversion> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.justreactors.heat_exchanging");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ExchangerConversion r, IFocusGroup focuses) {
        builder.addInputSlot(4, 4).setStandardSlotBackground()
                .setFluidRenderer(r.hotCoolantMb(), false, 16, 16)
                .add(ModFluids.HOT_COOLANT_SOURCE.get(), r.hotCoolantMb());
        builder.addInputSlot(24, 4).setStandardSlotBackground()
                .setFluidRenderer(r.waterMb(), false, 16, 16)
                .add(Fluids.WATER, r.waterMb());
        builder.addOutputSlot(80, 4).setOutputSlotBackground()
                .setFluidRenderer(r.coolantMb(), false, 16, 16)
                .add(ModFluids.COOLANT_SOURCE.get(), r.coolantMb());
        builder.addOutputSlot(100, 4).setOutputSlotBackground()
                .setFluidRenderer(r.steamMb(), false, 16, 16)
                .add(ModFluids.STEAM_SOURCE.get(), r.steamMb());
    }

    @Override
    public void draw(ExchangerConversion r, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        arrow.draw(g, 46, 4);
        var font = Minecraft.getInstance().font;
        g.text(font, Component.translatable("jei.justreactors.heat_exchanging.ratio",
                r.steamMb() / Math.max(1, r.hotCoolantMb())), 4, 28, 0xFF606060, false);
        g.text(font, Component.translatable("jei.justreactors.heat_exchanging.tube",
                String.format(Locale.ROOT, "%,d", r.perTubeMb())), 4, 40, 0xFF606060, false);
    }
}
