package com.justreactors.client;

import com.justreactors.JustReactors;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.registry.ModMenus;
import com.justreactors.turbine.client.TurbineControllerScreen;
import com.justreactors.turbine.client.TurbineRedstonePortScreen;
import com.justreactors.turbine.client.TurbineRotorRenderer;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

@EventBusSubscriber(modid = JustReactors.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.REACTOR_CONTROLLER.get(), ReactorControllerScreen::new);
        event.register(ModMenus.REACTOR_CONTROL_ROD.get(), ReactorControlRodScreen::new);
        event.register(ModMenus.REACTOR_ACCESS_PORT.get(), ReactorAccessPortScreen::new);
        event.register(ModMenus.REACTOR_REDSTONE_PORT.get(), ReactorRedstonePortScreen::new);
        event.register(ModMenus.TURBINE_CONTROLLER.get(), TurbineControllerScreen::new);
        event.register(ModMenus.TURBINE_REDSTONE_PORT.get(), TurbineRedstonePortScreen::new);
        event.register(ModMenus.CYANITE_REPROCESSOR.get(), CyaniteReprocessorScreen::new);
        event.register(ModMenus.HEAT_EXCHANGER.get(), com.justreactors.exchanger.client.HeatExchangerScreen::new);
        event.register(ModMenus.BATTERY.get(), com.justreactors.battery.client.BatteryScreen::new);
        event.register(ModMenus.BATTERY_SENSOR.get(), com.justreactors.battery.client.BatterySensorScreen::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ResourceLocation still = ResourceLocation.withDefaultNamespace("block/water_still");
        ResourceLocation flowing = ResourceLocation.withDefaultNamespace("block/water_flow");
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowing;
            }

            @Override
            public int getTintColor() {
                return 0xFFE8F1F8;
            }
        }, ModFluids.STEAM_TYPE.get());
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowing;
            }

            @Override
            public int getTintColor() {
                return 0xFF4FD6E0;
            }
        }, ModFluids.COOLANT_TYPE.get());
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowing;
            }

            @Override
            public int getTintColor() {
                return 0xFFEF7A35;
            }
        }, ModFluids.HOT_COOLANT_TYPE.get());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.REACTOR_CONTROLLER.get(), ReactorFuelRodRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURBINE_CONTROLLER.get(), TurbineRotorRenderer::new);
    }
}
