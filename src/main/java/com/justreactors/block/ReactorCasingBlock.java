package com.justreactors.block;

import com.justreactors.multiblock.CasingPart;
import com.justreactors.multiblock.MultiblockTrim;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Structural reactor casing block with context-aware visual trim ({@link #PART}) applied when a multiblock assembles.
 */
public class ReactorCasingBlock extends ReactorPartBlock {
    public static final EnumProperty<CasingPart> PART = MultiblockTrim.PART;

    public ReactorCasingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, CasingPart.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }
}
