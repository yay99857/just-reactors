package com.justreactors.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for reactor port/rod block entities that link to a {@link ReactorControllerBlockEntity}.
 */
public abstract class ReactorPartBlockEntity extends BlockEntity {
    @Nullable
    protected BlockPos controllerPos;

    protected ReactorPartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
    public ReactorControllerBlockEntity getController() {
        if (level == null || controllerPos == null) return null;
        if (level.getBlockEntity(controllerPos) instanceof ReactorControllerBlockEntity controller && controller.isAssembled()) {
            return controller;
        }
        return null;
    }
}
