package com.justreactors.compat.jei;

import com.justreactors.JustReactors;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.registry.ModBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * JEI category listing valid Reactor Moderator blocks/fluids and their thermal/neutron stats.
 */
public class ModeratorCategory implements IRecipeCategory<ReactorModerator.DisplayEntry> {
    public static final IRecipeType<ReactorModerator.DisplayEntry> TYPE =
            IRecipeType.create(JustReactors.id("reactor_moderators"), ReactorModerator.DisplayEntry.class);

    private static final int WIDTH = 164;
    private static final int HEIGHT = 42;

    private final IDrawable icon;

    public ModeratorCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModBlocks.REACTOR_FUEL_ROD.get());
    }

    @Override
    public IRecipeType<ReactorModerator.DisplayEntry> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.justreactors.moderators");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ReactorModerator.DisplayEntry recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder slot = builder.addInputSlot(6, 13).setStandardSlotBackground();
        if (recipe.fluid() != null) {
            slot.setFluidRenderer(1_000, false, 16, 16).add(recipe.fluid(), 1_000);
        } else {
            slot.addItemStacks(recipe.stacks());
        }
    }

    @Override
    public void draw(ReactorModerator.DisplayEntry recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        ReactorModerator mod = recipe.moderator();
        String abs = String.format(Locale.ROOT, "%.0f", mod.absorption() * 100.0);
        String heatEff = String.format(Locale.ROOT, "%.0f", mod.heatEfficiency() * 100.0);
        String moderation = String.format(Locale.ROOT, "%.2f", mod.moderation());
        String conductivity = String.format(Locale.ROOT, "%.2f", mod.heatConductivity());

        g.text(font, Component.translatable("jei.justreactors.moderator.absorption", abs, heatEff),
                28, 4, 0xFF404040, false);
        g.text(font, Component.translatable("jei.justreactors.moderator.moderation", moderation),
                28, 16, 0xFF606060, false);
        g.text(font, Component.translatable("jei.justreactors.moderator.conductivity", conductivity),
                28, 28, 0xFF606060, false);
    }
}
