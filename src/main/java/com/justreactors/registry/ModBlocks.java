package com.justreactors.registry;

import com.justreactors.JustReactors;
import com.justreactors.block.CyaniteReprocessorBlock;
import com.justreactors.block.ReactorAccessPortBlock;
import com.justreactors.block.ReactorControlRodBlock;
import com.justreactors.block.ReactorControllerBlock;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.block.ReactorGlassBlock;
import com.justreactors.block.ReactorPartBlock;
import com.justreactors.block.ReactorPowerTapBlock;
import com.justreactors.block.ReactorRedstonePortBlock;
import com.justreactors.turbine.block.TurbineControllerBlock;
import com.justreactors.turbine.block.TurbineFluidPortBlock;
import com.justreactors.turbine.block.TurbineGlassBlock;
import com.justreactors.turbine.block.TurbinePartBlock;
import com.justreactors.turbine.block.TurbinePowerTapBlock;
import com.justreactors.turbine.block.TurbineRedstonePortBlock;
import com.justreactors.turbine.block.TurbineRotorBladeBlock;
import com.justreactors.turbine.block.TurbineRotorShaftBlock;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JustReactors.MOD_ID);

    // Ores & storage blocks
    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.registerBlock("uranium_ore",
            p -> new DropExperienceBlock(UniformInt.of(2, 5), p),
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.registerBlock("deepslate_uranium_ore",
            p -> new DropExperienceBlock(UniformInt.of(2, 5), p),
            () -> BlockBehaviour.Properties.of()
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.registerBlock("uranium_block",
            Block::new,
            () -> JustReactors.reactorBlockProperties(5.0F, 6.0F));

    public static final DeferredBlock<Block> GRAPHITE_BLOCK = BLOCKS.registerBlock("graphite_block",
            Block::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 6.0F));

    public static final DeferredBlock<Block> CYANITE_BLOCK = BLOCKS.registerBlock("cyanite_block",
            Block::new,
            () -> JustReactors.reactorBlockProperties(5.0F, 6.0F));

    public static final DeferredBlock<Block> BLUTONIUM_BLOCK = BLOCKS.registerBlock("blutonium_block",
            Block::new,
            () -> JustReactors.reactorBlockProperties(5.0F, 8.0F));

    // Processing machines
    public static final DeferredBlock<CyaniteReprocessorBlock> CYANITE_REPROCESSOR = BLOCKS.registerBlock("cyanite_reprocessor",
            CyaniteReprocessorBlock::new,
            () -> JustReactors.reactorBlockProperties(3.5F, 6.0F)
                    .lightLevel(state -> state.getValue(CyaniteReprocessorBlock.LIT) ? 9 : 0));

    // Reactor multiblock parts
    public static final DeferredBlock<ReactorPartBlock> REACTOR_CASING = BLOCKS.registerBlock("reactor_casing",
            ReactorPartBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<ReactorGlassBlock> REACTOR_GLASS = BLOCKS.registerBlock("reactor_glass",
            ReactorGlassBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((s, l, p, e) -> false)
                    .isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<ReactorControllerBlock> REACTOR_CONTROLLER = BLOCKS.registerBlock("reactor_controller",
            ReactorControllerBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F)
                    .lightLevel(state -> state.getValue(ReactorControllerBlock.ACTIVE) ? 10 : 0));

    public static final DeferredBlock<ReactorPartBlock> REACTOR_FUEL_ROD = BLOCKS.registerBlock("reactor_fuel_rod",
            ReactorPartBlock::new,
            () -> JustReactors.reactorBlockProperties(3.5F, 6.0F)
                    .noOcclusion()
                    .lightLevel(state -> 4));

    public static final DeferredBlock<ReactorControlRodBlock> REACTOR_CONTROL_ROD = BLOCKS.registerBlock("reactor_control_rod",
            ReactorControlRodBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<ReactorPowerTapBlock> REACTOR_POWER_TAP = BLOCKS.registerBlock("reactor_power_tap",
            ReactorPowerTapBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<ReactorAccessPortBlock> REACTOR_ACCESS_PORT = BLOCKS.registerBlock("reactor_access_port",
            ReactorAccessPortBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<ReactorRedstonePortBlock> REACTOR_REDSTONE_PORT = BLOCKS.registerBlock("reactor_redstone_port",
            ReactorRedstonePortBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<ReactorCoolantPortBlock> REACTOR_COOLANT_PORT = BLOCKS.registerBlock("reactor_coolant_port",
            ReactorCoolantPortBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    // Steam Turbine multiblock parts
    public static final DeferredBlock<TurbinePartBlock> TURBINE_CASING = BLOCKS.registerBlock("turbine_casing",
            TurbinePartBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<TurbineGlassBlock> TURBINE_GLASS = BLOCKS.registerBlock("turbine_glass",
            TurbineGlassBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .isValidSpawn((s, l, p, e) -> false)
                    .isRedstoneConductor((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<TurbineControllerBlock> TURBINE_CONTROLLER = BLOCKS.registerBlock("turbine_controller",
            TurbineControllerBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F)
                    .lightLevel(state -> state.getValue(TurbineControllerBlock.ACTIVE) ? 10 : 0));

    public static final DeferredBlock<TurbinePartBlock> TURBINE_ROTOR_BEARING = BLOCKS.registerBlock("turbine_rotor_bearing",
            TurbinePartBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<TurbineRotorShaftBlock> TURBINE_ROTOR_SHAFT = BLOCKS.registerBlock("turbine_rotor_shaft",
            TurbineRotorShaftBlock::new,
            () -> JustReactors.reactorBlockProperties(3.5F, 6.0F).noOcclusion());

    public static final DeferredBlock<TurbineRotorBladeBlock> TURBINE_ROTOR_BLADE = BLOCKS.registerBlock("turbine_rotor_blade",
            TurbineRotorBladeBlock::new,
            () -> JustReactors.reactorBlockProperties(3.0F, 6.0F).noOcclusion());

    public static final DeferredBlock<TurbineFluidPortBlock> TURBINE_FLUID_PORT = BLOCKS.registerBlock("turbine_fluid_port",
            TurbineFluidPortBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<TurbinePowerTapBlock> TURBINE_POWER_TAP = BLOCKS.registerBlock("turbine_power_tap",
            TurbinePowerTapBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    public static final DeferredBlock<TurbineRedstonePortBlock> TURBINE_REDSTONE_PORT = BLOCKS.registerBlock("turbine_redstone_port",
            TurbineRedstonePortBlock::new,
            () -> JustReactors.reactorBlockProperties(4.0F, 8.0F));

    private ModBlocks() {
    }
}
