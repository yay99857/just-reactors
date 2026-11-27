package com.justreactors.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Interior uranium fuel column block. {@link #ACTIVE} toggles block light emission while the reactor is running.
 */
public class ReactorFuelRodBlock extends ReactorPartBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ReactorFuelRodBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}
