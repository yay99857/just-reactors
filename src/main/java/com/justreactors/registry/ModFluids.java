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

    private ModFluids() {
    }
}
