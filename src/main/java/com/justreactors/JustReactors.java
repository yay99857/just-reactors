package com.justreactors;

import com.justreactors.data.ReactorDataReloadListeners;
import com.justreactors.demo.DemoSetup;
import com.justreactors.gametest.ReactorGameTests;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModCreativeTabs;
import com.justreactors.registry.ModFluids;
import com.justreactors.registry.ModItems;
import com.justreactors.registry.ModMenus;
import com.justreactors.registry.ModSounds;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(JustReactors.MOD_ID)
public class JustReactors {
    public static final String MOD_ID = "justreactors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public JustReactors(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Just Reactors");

        modContainer.registerConfig(ModConfig.Type.SYNCED, JustReactorsConfig.SPEC);

        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(JustReactors::registerCapabilities);
        DemoSetup.register(NeoForge.EVENT_BUS);
        ReactorDataReloadListeners.register(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e) ->
                com.justreactors.multiblock.AssembledMultiblocks.clear());
        if (ModList.get().isLoaded("carryon")) {
            com.justreactors.compat.carryon.CarryOnCompat.register();
        }

        if (GameTestHooks.isGametestEnabled()) {
            ReactorGameTests.register(modEventBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static BlockBehaviour.Properties reactorBlockProperties(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .strength(hardness, resistance)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.REACTOR_POWER_TAP.get(),
                (be, side) -> be.getEnergyHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.REACTOR_ACCESS_PORT.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.REACTOR_COOLANT_PORT.get(),
                (be, side) -> be.getFluidHandler(side));

        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.TURBINE_POWER_TAP.get(),
                (be, side) -> be.getEnergyHandler(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.TURBINE_FLUID_PORT.get(),
                (be, side) -> be.getFluidHandler(side));

        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CYANITE_REPROCESSOR.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CYANITE_REPROCESSOR.get(),
                (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.CYANITE_REPROCESSOR.get(),
                (be, side) -> be.getEnergyHandler(side));

        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.HEAT_EXCHANGER_FLUID_PORT.get(),
                (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ModBlockEntities.BATTERY_POWER_PORT.get(),
                (be, side) -> be.getEnergyHandler(side));

        if (ModList.get().isLoaded("computercraft")) {
            com.justreactors.compat.cc.JustReactorsCCCompat.registerCapabilities(event);
        }
    }
}
