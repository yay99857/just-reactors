package com.justreactors.turbine.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class TurbinePartBlockEntity extends BlockEntity {
    @Nullable
    protected BlockPos controllerPos;

    protected TurbinePartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setControllerPos(@Nullable BlockPos controllerPos) {
        this.controllerPos = controllerPos == null ? null : controllerPos.immutable();
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Nullable
    public TurbineControllerBlockEntity getController() {
        if (level == null || controllerPos == null) return null;
        if (level.getBlockEntity(controllerPos) instanceof TurbineControllerBlockEntity controller && controller.isAssembled()) {
            return controller;
        }
        return null;
    }
}
