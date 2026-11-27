package com.justreactors.registry;

import com.justreactors.JustReactors;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, JustReactors.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, JustReactors.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.justreactors.steam")
                    .density(-500)
                    .viscosity(200)
                    .temperature(400)
                    .canSwim(false)
                    .canDrown(false)));

    private static BaseFlowingFluid.Properties steamProperties() {
        return new BaseFlowingFluid.Properties(STEAM_TYPE, STEAM_SOURCE, STEAM_FLOWING)
                .bucket(ModItems.STEAM_BUCKET);
    }

    public static final DeferredHolder<Fluid, FlowingFluid> STEAM_SOURCE = FLUIDS.register("steam",
            () -> new BaseFlowingFluid.Source(steamProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> STEAM_FLOWING = FLUIDS.register("flowing_steam",
            () -> new BaseFlowingFluid.Flowing(steamProperties()));

    // Reusable reactor coolant loop: Coolant -> (reactor) -> Hot Coolant -> (heat exchanger) -> Coolant
    public static final DeferredHolder<FluidType, FluidType> COOLANT_TYPE = FLUID_TYPES.register("coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.justreactors.coolant")
                    .density(1200)
                    .viscosity(1400)
                    .temperature(260)));

    public static final DeferredHolder<FluidType, FluidType> HOT_COOLANT_TYPE = FLUID_TYPES.register("hot_coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.justreactors.hot_coolant")
                    .density(1000)
                    .viscosity(1000)
                    .temperature(900)
                    .lightLevel(6)));

    private static BaseFlowingFluid.Properties coolantProperties() {
        return new BaseFlowingFluid.Properties(COOLANT_TYPE, COOLANT_SOURCE, COOLANT_FLOWING)
                .bucket(ModItems.COOLANT_BUCKET);
    }

    private static BaseFlowingFluid.Properties hotCoolantProperties() {
        return new BaseFlowingFluid.Properties(HOT_COOLANT_TYPE, HOT_COOLANT_SOURCE, HOT_COOLANT_FLOWING)
                .bucket(ModItems.HOT_COOLANT_BUCKET);
    }

    public static final DeferredHolder<Fluid, FlowingFluid> COOLANT_SOURCE = FLUIDS.register("coolant",
            () -> new BaseFlowingFluid.Source(coolantProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> COOLANT_FLOWING = FLUIDS.register("flowing_coolant",
            () -> new BaseFlowingFluid.Flowing(coolantProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_COOLANT_SOURCE = FLUIDS.register("hot_coolant",
            () -> new BaseFlowingFluid.Source(hotCoolantProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> HOT_COOLANT_FLOWING = FLUIDS.register("flowing_hot_coolant",
            () -> new BaseFlowingFluid.Flowing(hotCoolantProperties()));

    public static boolean isCoolant(net.minecraft.world.level.material.Fluid fluid) {
        return fluid.isSame(COOLANT_SOURCE.get());
    }

    public static boolean isHotCoolant(net.minecraft.world.level.material.Fluid fluid) {
        return fluid.isSame(HOT_COOLANT_SOURCE.get());
    }

    private ModFluids() {
    }
}
