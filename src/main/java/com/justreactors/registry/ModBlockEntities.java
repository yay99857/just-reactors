package com.justreactors.registry;

import com.justreactors.JustReactors;
import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.blockentity.ReactorAccessPortBlockEntity;
import com.justreactors.blockentity.ReactorControlRodBlockEntity;
import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.blockentity.ReactorCoolantPortBlockEntity;
import com.justreactors.blockentity.ReactorPowerTapBlockEntity;
import com.justreactors.blockentity.ReactorRedstonePortBlockEntity;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;
import com.justreactors.turbine.blockentity.TurbineFluidPortBlockEntity;
import com.justreactors.turbine.blockentity.TurbinePowerTapBlockEntity;
import com.justreactors.turbine.blockentity.TurbineRedstonePortBlockEntity;

import com.justreactors.battery.BatteryControllerBlockEntity;
import com.justreactors.battery.BatteryPowerPortBlockEntity;
import com.justreactors.battery.BatteryRedstoneSensorBlockEntity;
import com.justreactors.exchanger.HeatExchangerControllerBlockEntity;
import com.justreactors.exchanger.HeatExchangerFluidPortBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JustReactors.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorControllerBlockEntity>> REACTOR_CONTROLLER =
            BLOCK_ENTITIES.register("reactor_controller", () ->
                    BlockEntityType.Builder.of(ReactorControllerBlockEntity::new, ModBlocks.REACTOR_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorControlRodBlockEntity>> REACTOR_CONTROL_ROD =
            BLOCK_ENTITIES.register("reactor_control_rod", () ->
                    BlockEntityType.Builder.of(ReactorControlRodBlockEntity::new, ModBlocks.REACTOR_CONTROL_ROD.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorPowerTapBlockEntity>> REACTOR_POWER_TAP =
            BLOCK_ENTITIES.register("reactor_power_tap", () ->
                    BlockEntityType.Builder.of(ReactorPowerTapBlockEntity::new, ModBlocks.REACTOR_POWER_TAP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorAccessPortBlockEntity>> REACTOR_ACCESS_PORT =
            BLOCK_ENTITIES.register("reactor_access_port", () ->
                    BlockEntityType.Builder.of(ReactorAccessPortBlockEntity::new, ModBlocks.REACTOR_ACCESS_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorRedstonePortBlockEntity>> REACTOR_REDSTONE_PORT =
            BLOCK_ENTITIES.register("reactor_redstone_port", () ->
                    BlockEntityType.Builder.of(ReactorRedstonePortBlockEntity::new, ModBlocks.REACTOR_REDSTONE_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorCoolantPortBlockEntity>> REACTOR_COOLANT_PORT =
            BLOCK_ENTITIES.register("reactor_coolant_port", () ->
                    BlockEntityType.Builder.of(ReactorCoolantPortBlockEntity::new, ModBlocks.REACTOR_COOLANT_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CyaniteReprocessorBlockEntity>> CYANITE_REPROCESSOR =
            BLOCK_ENTITIES.register("cyanite_reprocessor", () ->
                    BlockEntityType.Builder.of(CyaniteReprocessorBlockEntity::new, ModBlocks.CYANITE_REPROCESSOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbineControllerBlockEntity>> TURBINE_CONTROLLER =
            BLOCK_ENTITIES.register("turbine_controller", () ->
                    BlockEntityType.Builder.of(TurbineControllerBlockEntity::new, ModBlocks.TURBINE_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbineFluidPortBlockEntity>> TURBINE_FLUID_PORT =
            BLOCK_ENTITIES.register("turbine_fluid_port", () ->
                    BlockEntityType.Builder.of(TurbineFluidPortBlockEntity::new, ModBlocks.TURBINE_FLUID_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbinePowerTapBlockEntity>> TURBINE_POWER_TAP =
            BLOCK_ENTITIES.register("turbine_power_tap", () ->
                    BlockEntityType.Builder.of(TurbinePowerTapBlockEntity::new, ModBlocks.TURBINE_POWER_TAP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbineRedstonePortBlockEntity>> TURBINE_REDSTONE_PORT =
            BLOCK_ENTITIES.register("turbine_redstone_port", () ->
                    BlockEntityType.Builder.of(TurbineRedstonePortBlockEntity::new, ModBlocks.TURBINE_REDSTONE_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatExchangerControllerBlockEntity>> HEAT_EXCHANGER_CONTROLLER =
            BLOCK_ENTITIES.register("heat_exchanger_controller", () ->
                    BlockEntityType.Builder.of(HeatExchangerControllerBlockEntity::new, ModBlocks.HEAT_EXCHANGER_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatExchangerFluidPortBlockEntity>> HEAT_EXCHANGER_FLUID_PORT =
            BLOCK_ENTITIES.register("heat_exchanger_fluid_port", () ->
                    BlockEntityType.Builder.of(HeatExchangerFluidPortBlockEntity::new, ModBlocks.HEAT_EXCHANGER_FLUID_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryControllerBlockEntity>> BATTERY_CONTROLLER =
            BLOCK_ENTITIES.register("battery_controller", () ->
                    BlockEntityType.Builder.of(BatteryControllerBlockEntity::new, ModBlocks.BATTERY_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryPowerPortBlockEntity>> BATTERY_POWER_PORT =
            BLOCK_ENTITIES.register("battery_power_port", () ->
                    BlockEntityType.Builder.of(BatteryPowerPortBlockEntity::new, ModBlocks.BATTERY_POWER_PORT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryRedstoneSensorBlockEntity>> BATTERY_REDSTONE_SENSOR =
            BLOCK_ENTITIES.register("battery_redstone_sensor", () ->
                    BlockEntityType.Builder.of(BatteryRedstoneSensorBlockEntity::new, ModBlocks.BATTERY_REDSTONE_SENSOR.get()).build(null));

    private ModBlockEntities() {
    }
}
