package com.justreactors.turbine.block;

import com.justreactors.turbine.blockentity.TurbinePowerTapBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TurbinePowerTapBlock extends TurbinePartBlock implements EntityBlock {
    public TurbinePowerTapBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TurbinePowerTapBlockEntity(pos, state);
    }
}
