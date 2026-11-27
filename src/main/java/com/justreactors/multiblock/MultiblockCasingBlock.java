package com.justreactors.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Structural multiblock casing block with context-aware {@link MultiblockTrim#PART} states for box multiblocks.
 */
public class MultiblockCasingBlock extends Block {
    public MultiblockCasingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MultiblockTrim.PART, CasingPart.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MultiblockTrim.PART);
    }
}
