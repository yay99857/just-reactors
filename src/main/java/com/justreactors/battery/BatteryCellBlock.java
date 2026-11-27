package com.justreactors.battery;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Interior storage cell of the Battery. CHARGE shows the battery's fill level (0..4) through the glass. */
public class BatteryCellBlock extends Block {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 4);

    public BatteryCellBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }
}
