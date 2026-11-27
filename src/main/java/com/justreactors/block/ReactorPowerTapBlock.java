package com.justreactors.block;

import com.justreactors.blockentity.ReactorPowerTapBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ReactorPowerTapBlock extends ReactorPartBlock implements EntityBlock {
    public ReactorPowerTapBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorPowerTapBlockEntity(pos, state);
    }
}
