package com.justreactors.turbine.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Orientable rotor shaft (X, Y, or Z axis) for Steam Turbine interiors.
 * Hides its static chunk model while {@link #SPINNING} is true.
 */
public class TurbineRotorShaftBlock extends RotatedPillarBlock {
    public static final BooleanProperty SPINNING = BooleanProperty.create("spinning");

    public TurbineRotorShaftBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(SPINNING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SPINNING);
    }
}
