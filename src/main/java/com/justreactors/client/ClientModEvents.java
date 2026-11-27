package com.justreactors.client;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModMenus;
import com.justreactors.turbine.client.TurbineControllerScreen;
import com.justreactors.turbine.client.TurbineRedstonePortScreen;
import com.justreactors.turbine.client.TurbineRotorRenderer;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// The steam fluid's client textures are set in ModFluids (FluidType#initializeClient on 1.20.1).
@EventBusSubscriber(modid = JustReactors.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.REACTOR_CONTROLLER.get(), ReactorControllerScreen::new);
            MenuScreens.register(ModMenus.REACTOR_CONTROL_ROD.get(), ReactorControlRodScreen::new);
            MenuScreens.register(ModMenus.REACTOR_ACCESS_PORT.get(), ReactorAccessPortScreen::new);
            MenuScreens.register(ModMenus.REACTOR_REDSTONE_PORT.get(), ReactorRedstonePortScreen::new);
            MenuScreens.register(ModMenus.TURBINE_CONTROLLER.get(), TurbineControllerScreen::new);
            MenuScreens.register(ModMenus.TURBINE_REDSTONE_PORT.get(), TurbineRedstonePortScreen::new);
            MenuScreens.register(ModMenus.CYANITE_REPROCESSOR.get(), CyaniteReprocessorScreen::new);
            MenuScreens.register(ModMenus.HEAT_EXCHANGER.get(), com.justreactors.exchanger.client.HeatExchangerScreen::new);
            MenuScreens.register(ModMenus.BATTERY.get(), com.justreactors.battery.client.BatteryScreen::new);
            MenuScreens.register(ModMenus.BATTERY_SENSOR.get(), com.justreactors.battery.client.BatterySensorScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.REACTOR_CONTROLLER.get(), ReactorFuelRodRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURBINE_CONTROLLER.get(), TurbineRotorRenderer::new);
    }
}
