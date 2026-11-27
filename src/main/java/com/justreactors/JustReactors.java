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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(JustReactors.MOD_ID)
public class JustReactors {
    public static final String MOD_ID = "justreactors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public JustReactors() {
        LOGGER.info("Initializing Just Reactors");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, JustReactorsConfig.SPEC);

        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(JustReactors::commonSetup);
        DemoSetup.register(MinecraftForge.EVENT_BUS);
        ReactorDataReloadListeners.register(MinecraftForge.EVENT_BUS);

        if (ForgeGameTestHooks.isGametestEnabled()) {
            ReactorGameTests.register(modEventBus);
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static BlockBehaviour.Properties reactorBlockProperties(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .strength(hardness, resistance)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    // Item, fluid and energy handlers are exposed by each block entity's getCapability (see transfer/Caps).
    private static void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("computercraft")) {
            com.justreactors.compat.cc.JustReactorsCCCompat.register();
        }
    }
}
