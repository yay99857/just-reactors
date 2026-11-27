package com.justreactors.registry;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import com.justreactors.JustReactors;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, JustReactors.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, JustReactors.MOD_ID);

    public static final RegistryObject<FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.justreactors.steam")
                    .density(-500)
                    .viscosity(200)
                    .temperature(400)
                    .canSwim(false)
                    .canDrown(false)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL = new ResourceLocation("block/water_still");
                        private static final ResourceLocation FLOWING = new ResourceLocation("block/water_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFE8F1F8;
                        }
                    });
                }
            });

    private static ForgeFlowingFluid.Properties steamProperties() {
        return new ForgeFlowingFluid.Properties(STEAM_TYPE, STEAM_SOURCE, STEAM_FLOWING)
                .bucket(ModItems.STEAM_BUCKET);
    }

    public static final RegistryObject<FlowingFluid> STEAM_SOURCE = FLUIDS.register("steam",
            () -> new ForgeFlowingFluid.Source(steamProperties()));

    public static final RegistryObject<FlowingFluid> STEAM_FLOWING = FLUIDS.register("flowing_steam",
            () -> new ForgeFlowingFluid.Flowing(steamProperties()));

    // Reusable reactor coolant loop: Coolant -> (reactor) -> Hot Coolant -> (heat exchanger) -> Coolant
    public static final RegistryObject<FluidType> COOLANT_TYPE = FLUID_TYPES.register("coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.justreactors.coolant")
                    .density(1200)
                    .viscosity(1400)
                    .temperature(260)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL = new ResourceLocation("block/water_still");
                        private static final ResourceLocation FLOWING = new ResourceLocation("block/water_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFF4FD6E0;
                        }
                    });
                }
            });

    public static final RegistryObject<FluidType> HOT_COOLANT_TYPE = FLUID_TYPES.register("hot_coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.justreactors.hot_coolant")
                    .density(1000)
                    .viscosity(1000)
                    .temperature(900)
                    .lightLevel(6)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL = new ResourceLocation("block/water_still");
                        private static final ResourceLocation FLOWING = new ResourceLocation("block/water_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOWING;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFEF7A35;
                        }
                    });
                }
            });

    private static ForgeFlowingFluid.Properties coolantProperties() {
        return new ForgeFlowingFluid.Properties(COOLANT_TYPE, COOLANT_SOURCE, COOLANT_FLOWING)
                .bucket(ModItems.COOLANT_BUCKET);
    }

    private static ForgeFlowingFluid.Properties hotCoolantProperties() {
        return new ForgeFlowingFluid.Properties(HOT_COOLANT_TYPE, HOT_COOLANT_SOURCE, HOT_COOLANT_FLOWING)
                .bucket(ModItems.HOT_COOLANT_BUCKET);
    }

    public static final RegistryObject<FlowingFluid> COOLANT_SOURCE = FLUIDS.register("coolant",
            () -> new ForgeFlowingFluid.Source(coolantProperties()));
    public static final RegistryObject<FlowingFluid> COOLANT_FLOWING = FLUIDS.register("flowing_coolant",
            () -> new ForgeFlowingFluid.Flowing(coolantProperties()));
    public static final RegistryObject<FlowingFluid> HOT_COOLANT_SOURCE = FLUIDS.register("hot_coolant",
            () -> new ForgeFlowingFluid.Source(hotCoolantProperties()));
    public static final RegistryObject<FlowingFluid> HOT_COOLANT_FLOWING = FLUIDS.register("flowing_hot_coolant",
            () -> new ForgeFlowingFluid.Flowing(hotCoolantProperties()));

    public static boolean isCoolant(net.minecraft.world.level.material.Fluid fluid) {
        return fluid.isSame(COOLANT_SOURCE.get());
    }

    public static boolean isHotCoolant(net.minecraft.world.level.material.Fluid fluid) {
        return fluid.isSame(HOT_COOLANT_SOURCE.get());
    }

    private ModFluids() {
    }
}
