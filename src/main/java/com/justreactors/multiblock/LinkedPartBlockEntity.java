package com.justreactors.multiblock;

import com.justreactors.transfer.Caps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import com.justreactors.transfer.ReactorEnergyStorage;
import com.justreactors.transfer.ReactorFluidTank;

/**
 * Wall part of a box multiblock that is linked to its controller after assembly.
 */
public abstract class LinkedPartBlockEntity<C extends BoxControllerBlockEntity> extends BlockEntity {
    private final Class<C> controllerType;
    @Nullable
    private BlockPos controllerPos;

    protected LinkedPartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<C> controllerType) {
        super(type, pos, state);
        this.controllerType = controllerType;
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        this.controllerPos = pos == null ? null : pos.immutable();
    }

    @Nullable
    public C getController() {
        if (level == null || controllerPos == null) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (controllerType.isInstance(be)) {
            C controller = controllerType.cast(be);
            if (controller.isAssembled()) return controller;
        }
        return null;
    }

    /** Pushes up to {@code maxRate} mB of the tank into neighbors outside the multiblock. */
    public int pushFluidToNeighbors(ReactorFluidTank tank, int maxRate) {
        if (level == null || level.isClientSide() || tank.isEmpty()) return 0;
        C controller = getController();
        int remaining = Math.min(maxRate, tank.getFluidAmount());
        int total = 0;
        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            if (controller != null && controller.contains(neighborPos)) continue;
            IFluidHandler neighbor = Caps.block(level, neighborPos, ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
            if (neighbor == null) continue;
            FluidStack toPush = ReactorFluidTank.withAmount(tank.getFluid(), remaining);
            int pushed = ReactorFluidTank.pushFluid(neighbor, toPush);
            if (pushed > 0) {
                tank.drainInternal(pushed);
                remaining -= pushed;
                total += pushed;
            }
        }
        return total;
    }

    /** Pushes up to {@code maxRate} FE from the buffer into neighbors outside the multiblock. */
    public int pushEnergyToNeighbors(ReactorEnergyStorage buffer, int maxRate) {
        if (level == null || level.isClientSide()) return 0;
        C controller = getController();
        int remaining = Math.min(maxRate, buffer.getEnergyStored());
        int total = 0;
        for (Direction dir : Direction.values()) {
            if (remaining <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            if (controller != null && controller.contains(neighborPos)) continue;
            IEnergyStorage neighbor = Caps.block(level, neighborPos, ForgeCapabilities.ENERGY, dir.getOpposite());
            if (neighbor == null) continue;
            int pushed = ReactorEnergyStorage.pushEnergy(neighbor, remaining);
            if (pushed > 0) {
                buffer.extractInternal(pushed);
                remaining -= pushed;
                total += pushed;
            }
        }
        return total;
    }
}
