package com.justreactors.registry;

import net.minecraft.world.level.block.GlassBlock;
import com.justreactors.JustReactors;
import com.justreactors.block.CyaniteReprocessorBlock;
import com.justreactors.block.ReactorAccessPortBlock;
import com.justreactors.block.ReactorCasingBlock;
import com.justreactors.block.ReactorControlRodBlock;
import com.justreactors.block.ReactorControllerBlock;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.block.ReactorFuelRodBlock;
import com.justreactors.block.ReactorGlassBlock;
import com.justreactors.block.ReactorPartBlock;
import com.justreactors.block.ReactorPowerTapBlock;
import com.justreactors.block.ReactorRedstonePortBlock;
import com.justreactors.turbine.block.TurbineCasingBlock;
import com.justreactors.turbine.block.TurbineControllerBlock;
import com.justreactors.turbine.block.TurbineFluidPortBlock;
import com.justreactors.turbine.block.TurbineGlassBlock;
import com.justreactors.turbine.block.TurbinePartBlock;
import com.justreactors.turbine.block.TurbinePowerTapBlock;
import com.justreactors.turbine.block.TurbineRedstonePortBlock;
import com.justreactors.turbine.block.TurbineRotorBladeBlock;
import com.justreactors.turbine.block.TurbineRotorShaftBlock;

import com.justreactors.battery.BatteryCellBlock;
import com.justreactors.battery.BatteryPowerPortBlock;
import com.justreactors.battery.BatteryRedstoneSensorBlock;
import com.justreactors.exchanger.HeatExchangerFluidPortBlock;
import com.justreactors.multiblock.BoxControllerBlock;
import com.justreactors.multiblock.ConnectedGlassBlock;
import com.justreactors.multiblock.MultiblockCasingBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Function;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, JustReactors.MOD_ID);

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory,
                                                                     BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> factory.apply(properties));
    }

    private static final java.util.function.Supplier<BlockBehaviour.Properties> GLASS = () -> BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((s, l, p, e) -> false)
                    .isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)
                    .requiresCorrectToolForDrops();

    // Ores & storage blocks
    public static final RegistryObject<Block> URANIUM_ORE = registerBlock("uranium_ore",
            p -> new DropExperienceBlock(p, UniformInt.of(2, 5)),
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> DEEPSLATE_URANIUM_ORE = registerBlock("deepslate_uranium_ore",
            p -> new DropExperienceBlock(p, UniformInt.of(2, 5)),
            BlockBehaviour.Properties.of()
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> URANIUM_BLOCK = registerBlock("uranium_block",
            Block::new,
            JustReactors.reactorBlockProperties(5.0F, 6.0F));

    public static final RegistryObject<Block> GRAPHITE_BLOCK = registerBlock("graphite_block",
            Block::new,
            JustReactors.reactorBlockProperties(4.0F, 6.0F));

    public static final RegistryObject<Block> CYANITE_BLOCK = registerBlock("cyanite_block",
            Block::new,
            JustReactors.reactorBlockProperties(5.0F, 6.0F));

    public static final RegistryObject<Block> BLUTONIUM_BLOCK = registerBlock("blutonium_block",
            Block::new,
            JustReactors.reactorBlockProperties(5.0F, 8.0F));

    // Processing machines
    public static final RegistryObject<CyaniteReprocessorBlock> CYANITE_REPROCESSOR = registerBlock("cyanite_reprocessor",
            CyaniteReprocessorBlock::new,
            JustReactors.reactorBlockProperties(3.5F, 6.0F)
                    .lightLevel(state -> state.getValue(CyaniteReprocessorBlock.LIT) ? 9 : 0));

    // Reactor multiblock parts
    public static final RegistryObject<ReactorCasingBlock> REACTOR_CASING = registerBlock("reactor_casing",
            ReactorCasingBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ReactorGlassBlock> REACTOR_GLASS = registerBlock("reactor_glass",
            ReactorGlassBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((s, l, p, e) -> false)
                    .isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<ReactorControllerBlock> REACTOR_CONTROLLER = registerBlock("reactor_controller",
            ReactorControllerBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F)
                    .lightLevel(state -> state.getValue(ReactorControllerBlock.ACTIVE) ? 10 : 0));

    public static final RegistryObject<ReactorFuelRodBlock> REACTOR_FUEL_ROD = registerBlock("reactor_fuel_rod",
            ReactorFuelRodBlock::new,
            JustReactors.reactorBlockProperties(3.5F, 6.0F)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(ReactorFuelRodBlock.ACTIVE) ? 14 : 3));

    public static final RegistryObject<ReactorControlRodBlock> REACTOR_CONTROL_ROD = registerBlock("reactor_control_rod",
            ReactorControlRodBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ReactorPowerTapBlock> REACTOR_POWER_TAP = registerBlock("reactor_power_tap",
            ReactorPowerTapBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ReactorAccessPortBlock> REACTOR_ACCESS_PORT = registerBlock("reactor_access_port",
            ReactorAccessPortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ReactorRedstonePortBlock> REACTOR_REDSTONE_PORT = registerBlock("reactor_redstone_port",
            ReactorRedstonePortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ReactorCoolantPortBlock> REACTOR_COOLANT_PORT = registerBlock("reactor_coolant_port",
            ReactorCoolantPortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    // Steam Turbine multiblock parts
    public static final RegistryObject<TurbineCasingBlock> TURBINE_CASING = registerBlock("turbine_casing",
            TurbineCasingBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<TurbineGlassBlock> TURBINE_GLASS = registerBlock("turbine_glass",
            TurbineGlassBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((s, l, p, e) -> false)
                    .isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<TurbineControllerBlock> TURBINE_CONTROLLER = registerBlock("turbine_controller",
            TurbineControllerBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F)
                    .lightLevel(state -> state.getValue(TurbineControllerBlock.ACTIVE) ? 10 : 0));

    public static final RegistryObject<TurbinePartBlock> TURBINE_ROTOR_BEARING = registerBlock("turbine_rotor_bearing",
            TurbinePartBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<TurbineRotorShaftBlock> TURBINE_ROTOR_SHAFT = registerBlock("turbine_rotor_shaft",
            TurbineRotorShaftBlock::new,
            JustReactors.reactorBlockProperties(3.5F, 6.0F)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(TurbineRotorShaftBlock.SPINNING) ? 14 : 0));

    public static final RegistryObject<TurbineRotorBladeBlock> TURBINE_ROTOR_BLADE = registerBlock("turbine_rotor_blade",
            TurbineRotorBladeBlock::new,
            JustReactors.reactorBlockProperties(3.0F, 6.0F)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(TurbineRotorBladeBlock.SPINNING) ? 12 : 0));

    public static final RegistryObject<TurbineFluidPortBlock> TURBINE_FLUID_PORT = registerBlock("turbine_fluid_port",
            TurbineFluidPortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<TurbinePowerTapBlock> TURBINE_POWER_TAP = registerBlock("turbine_power_tap",
            TurbinePowerTapBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<TurbineRedstonePortBlock> TURBINE_REDSTONE_PORT = registerBlock("turbine_redstone_port",
            TurbineRedstonePortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    // Heat Exchanger multiblock parts
    public static final RegistryObject<MultiblockCasingBlock> HEAT_EXCHANGER_CASING = registerBlock("heat_exchanger_casing",
            MultiblockCasingBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ConnectedGlassBlock> HEAT_EXCHANGER_GLASS = registerBlock("heat_exchanger_glass",
            ConnectedGlassBlock::new,
            GLASS.get());

    public static final RegistryObject<BoxControllerBlock> HEAT_EXCHANGER_CONTROLLER = registerBlock("heat_exchanger_controller",
            p -> new BoxControllerBlock(p, ModBlockEntities.HEAT_EXCHANGER_CONTROLLER),
            JustReactors.reactorBlockProperties(4.0F, 8.0F)
                    .lightLevel(state -> state.getValue(BoxControllerBlock.ACTIVE) ? 10 : 0));

    public static final RegistryObject<HeatExchangerFluidPortBlock> HEAT_EXCHANGER_FLUID_PORT = registerBlock("heat_exchanger_fluid_port",
            HeatExchangerFluidPortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<Block> HEAT_EXCHANGER_TUBE = registerBlock("heat_exchanger_tube",
            Block::new,
            JustReactors.reactorBlockProperties(3.5F, 6.0F));

    // Battery multiblock parts
    public static final RegistryObject<MultiblockCasingBlock> BATTERY_CASING = registerBlock("battery_casing",
            MultiblockCasingBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<ConnectedGlassBlock> BATTERY_GLASS = registerBlock("battery_glass",
            ConnectedGlassBlock::new,
            GLASS.get());

    public static final RegistryObject<BoxControllerBlock> BATTERY_CONTROLLER = registerBlock("battery_controller",
            p -> new BoxControllerBlock(p, ModBlockEntities.BATTERY_CONTROLLER),
            JustReactors.reactorBlockProperties(4.0F, 8.0F)
                    .lightLevel(state -> state.getValue(BoxControllerBlock.ACTIVE) ? 8 : 0));

    public static final RegistryObject<BatteryPowerPortBlock> BATTERY_POWER_PORT = registerBlock("battery_power_port",
            BatteryPowerPortBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<BatteryRedstoneSensorBlock> BATTERY_REDSTONE_SENSOR = registerBlock("battery_redstone_sensor",
            BatteryRedstoneSensorBlock::new,
            JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final RegistryObject<BatteryCellBlock> BATTERY_CELL = registerBlock("battery_cell",
            BatteryCellBlock::new,
            JustReactors.reactorBlockProperties(3.5F, 6.0F)
                    .lightLevel(state -> state.getValue(BatteryCellBlock.CHARGE) * 2));

    private ModBlocks() {
    }
}
