package com.justreactors.turbine.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Rotor blade block for Steam Turbine interiors. Hides its static chunk model while
 * {@link #SPINNING} is true so the animated BlockEntityRenderer renders the rotating assembly.
 */
public class TurbineRotorBladeBlock extends TurbinePartBlock {
    public static final BooleanProperty SPINNING = BooleanProperty.create("spinning");

    public TurbineRotorBladeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPINNING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPINNING);
    }
}
