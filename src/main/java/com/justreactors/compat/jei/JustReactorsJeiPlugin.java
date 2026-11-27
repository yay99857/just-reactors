package com.justreactors.compat.jei;

import com.justreactors.JustReactors;
import com.justreactors.JustReactorsConfig;
import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.registry.ModBlocks;
import com.justreactors.turbine.TurbineCoil;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

@JeiPlugin
public class JustReactorsJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return JustReactors.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ReprocessorCategory(guiHelper),
                new ModeratorCategory(guiHelper),
                new TurbineCoilCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.CYANITE_REPROCESSOR.get(), ReprocessorCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.REACTOR_CONTROLLER.get(), ModeratorCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.REACTOR_FUEL_ROD.get(), ModeratorCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.TURBINE_CONTROLLER.get(), TurbineCoilCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.TURBINE_ROTOR_SHAFT.get(), TurbineCoilCategory.TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ReprocessorCategory.TYPE, List.of(
                new ReprocessorCategory.ReprocessorDisplay(
                        CyaniteReprocessorBlockEntity.CYANITE_PER_OP,
                        JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_WATER_COST),
                        JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_ENERGY_COST),
                        JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_TICKS)
                )
        ));
        registration.addRecipes(ModeratorCategory.TYPE, ReactorModerator.getDisplayEntries());
        registration.addRecipes(TurbineCoilCategory.TYPE, TurbineCoil.getDisplayEntries());

        registration.addIngredientInfo(ModBlocks.REACTOR_CONTROLLER.get(),
                Component.translatable("jei.justreactors.info.reactor_controller"));
        registration.addIngredientInfo(ModBlocks.REACTOR_COOLANT_PORT.get(),
                Component.translatable("jei.justreactors.info.reactor_coolant_port"));
        registration.addIngredientInfo(ModBlocks.TURBINE_CONTROLLER.get(),
                Component.translatable("jei.justreactors.info.turbine_controller"));
        registration.addIngredientInfo(ModBlocks.TURBINE_REDSTONE_PORT.get(),
                Component.translatable("jei.justreactors.info.turbine_redstone_port"));
        registration.addIngredientInfo(ModBlocks.CYANITE_REPROCESSOR.get(),
                Component.translatable("jei.justreactors.info.cyanite_reprocessor"));
    }
}
