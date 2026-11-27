package com.justreactors.compat.jei;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModBlocks;
import com.justreactors.turbine.TurbineCoil;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * JEI category listing valid Steam Turbine induction coil blocks and their stats.
 */
public class TurbineCoilCategory implements IRecipeCategory<TurbineCoil.DisplayEntry> {
    public static final RecipeType<TurbineCoil.DisplayEntry> TYPE =
            RecipeType.create(JustReactors.MOD_ID, "turbine_coils", TurbineCoil.DisplayEntry.class);

    private static final int WIDTH = 164;
    private static final int HEIGHT = 42;

    private final IDrawable icon;

    public TurbineCoilCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.TURBINE_ROTOR_SHAFT.get());
    }

    @Override
    public RecipeType<TurbineCoil.DisplayEntry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.justreactors.turbine_coils");
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
    public void setRecipe(IRecipeLayoutBuilder builder, TurbineCoil.DisplayEntry recipe, IFocusGroup focuses) {
        builder.addInputSlot(6, 13)
                .setStandardSlotBackground()
                .addItemStacks(recipe.stacks());
    }

    @Override
    public void draw(TurbineCoil.DisplayEntry recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        TurbineCoil coil = recipe.coil();
        String eff = String.format(Locale.ROOT, "%.2f", coil.efficiency());
        String bonus = String.format(Locale.ROOT, "%.2f", coil.bonus());
        String drag = String.format(Locale.ROOT, "%.2f", coil.drag());

        g.drawString(font, Component.translatable("jei.justreactors.coil.efficiency", eff),
                28, 4, 0xFF404040, false);
        g.drawString(font, Component.translatable("jei.justreactors.coil.bonus", bonus),
                28, 16, 0xFF606060, false);
        g.drawString(font, Component.translatable("jei.justreactors.coil.drag", drag),
                28, 28, 0xFF606060, false);
    }
}
