package com.justreactors.turbine.block;

import com.justreactors.multiblock.CasingPart;
import com.justreactors.multiblock.MultiblockTrim;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Structural Steam Turbine casing block with context-aware {@link MultiblockTrim#PART} trim states.
 */
public class TurbineCasingBlock extends TurbinePartBlock {
    public TurbineCasingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MultiblockTrim.PART, CasingPart.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MultiblockTrim.PART);
    }
}
