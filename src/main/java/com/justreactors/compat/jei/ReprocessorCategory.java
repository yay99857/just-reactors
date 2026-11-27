package com.justreactors.compat.jei;

import com.justreactors.JustReactors;
import com.justreactors.JustReactorsConfig;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModItems;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import java.util.Locale;

public class ReprocessorCategory implements IRecipeCategory<ReprocessorCategory.ReprocessorDisplay> {
    public static final RecipeType<ReprocessorDisplay> TYPE =
            RecipeType.create(JustReactors.MOD_ID, "reprocessing", ReprocessorDisplay.class);

    private static final int WIDTH = 116;
    private static final int HEIGHT = 46;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public record ReprocessorDisplay(int cyaniteCount, int waterMb, int energyFe, int ticks) {
    }

    public ReprocessorCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.CYANITE_REPROCESSOR.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(
                Math.max(10, JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_TICKS)));
    }

    @Override
    public RecipeType<ReprocessorDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.justreactors.reprocessing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ReprocessorDisplay recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 10)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(ModItems.CYANITE_INGOT.get(), recipe.cyaniteCount()));
        builder.addInputSlot(26, 10)
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.waterMb(), false, 16, 16)
                .addFluidStack(Fluids.WATER, recipe.waterMb());
        builder.addOutputSlot(88, 10)
                .setOutputSlotBackground()
                .addItemStack(new ItemStack(ModItems.BLUTONIUM_INGOT.get(), 1));
    }

    @Override
    public void draw(ReprocessorDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        arrow.draw(g, 52, 10);
        var font = Minecraft.getInstance().font;
        Component info = Component.translatable("jei.justreactors.reprocessing.info",
                String.format(Locale.ROOT, "%.1f", recipe.ticks() / 20.0),
                String.format(Locale.ROOT, "%,d", recipe.energyFe()));
        g.drawString(font, info, 4, HEIGHT - 10, 0xFF606060, false);
    }
}
