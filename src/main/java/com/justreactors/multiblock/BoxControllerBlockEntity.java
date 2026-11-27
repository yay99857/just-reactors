package com.justreactors.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Shared controller logic for the box multiblocks: periodic validation, part linking and the
 * assembled/active block state of the controller.
 */
public abstract class BoxControllerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int VALIDATION_INTERVAL_TICKS = 20;

    @Nullable
    private BoxValidator.Scan scan;
    @Nullable
    private Component lastValidationError;
    protected int tickCounter;
    private boolean trimApplied = false;

    protected BoxControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract BoxValidator.Spec spec();

    /** Called after every successful validation. */
    protected abstract void onAssembled(BoxValidator.Scan scan, boolean firstTime);

    /** Called when a previously valid structure breaks. */
    protected void onDisassembled() {
    }

    /** Runs the machine for one tick while assembled; returns whether the controller should show as active. */
    protected abstract boolean tickAssembled(Level level, BoxValidator.Scan scan);

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoxControllerBlockEntity be) {
        be.tickCounter++;
        if (be.scan == null || be.tickCounter % VALIDATION_INTERVAL_TICKS == 0) {
            be.tryAssemble();
        }
        boolean active = be.scan != null && be.tickAssembled(level, be.scan);
        be.updateBlockState(level, pos, level.getBlockState(pos), be.scan != null, active);
    }

    public boolean tryAssemble() {
        if (level == null || level.isClientSide()) return false;
        BoxValidator.Result result = BoxValidator.validate(level, worldPosition, spec());
        if (result instanceof BoxValidator.Result.Valid valid) {
            boolean first = scan == null;
            boolean changed = !first && !scan.equals(valid.scan());
            if (changed) {
                unlinkParts();
                MultiblockTrim.reset(level, scan.min(), scan.max(), spec().casing().get());
            }
            scan = valid.scan();
            lastValidationError = null;
            linkParts(scan);
            if (first || changed || !trimApplied) {
                MultiblockTrim.apply(level, scan.min(), scan.max(), spec().casing().get());
                trimApplied = true;
            }
            AssembledMultiblocks.put(level, worldPosition, scan.min(), scan.max());
            onAssembled(scan, first);
            return true;
        }
        Component reason = ((BoxValidator.Result.Invalid) result).reason();
        if (scan != null) {
            unlinkParts();
            MultiblockTrim.reset(level, scan.min(), scan.max(), spec().casing().get());
            trimApplied = false;
            scan = null;
            onDisassembled();
        }
        AssembledMultiblocks.remove(level, worldPosition);
        lastValidationError = reason;
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && scan != null) {
            MultiblockTrim.reset(level, scan.min(), scan.max(), spec().casing().get());
            trimApplied = false;
        }
        super.preRemoveSideEffects(pos, state);
    }

    private void linkParts(BoxValidator.Scan s) {
        for (List<BlockPos> list : s.wallParts().values()) {
            for (BlockPos p : list) {
                if (level.getBlockEntity(p) instanceof LinkedPartBlockEntity<?> part) part.setControllerPos(worldPosition);
            }
        }
    }

    private void unlinkParts() {
        if (level == null || scan == null) return;
        for (List<BlockPos> list : scan.wallParts().values()) {
            for (BlockPos p : list) {
                if (level.getBlockEntity(p) instanceof LinkedPartBlockEntity<?> part) part.setControllerPos(null);
            }
        }
    }

    @Override
    public void setRemoved() {
        // No world edits here: this also runs on chunk unload. Parts find out through getController().
        if (level != null && !level.isClientSide()) {
            AssembledMultiblocks.remove(level, worldPosition);
        }
        super.setRemoved();
    }

    private void updateBlockState(Level level, BlockPos pos, BlockState state, boolean assembled, boolean active) {
        if (!state.hasProperty(BoxControllerBlock.ASSEMBLED)) return;
        if (state.getValue(BoxControllerBlock.ASSEMBLED) != assembled || state.getValue(BoxControllerBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(BoxControllerBlock.ASSEMBLED, assembled)
                    .setValue(BoxControllerBlock.ACTIVE, active), 3);
        }
    }

    /** Comparator output read from the controller block (0..15). */
    public int getComparatorSignal() {
        return 0;
    }

    public boolean isAssembled() {
        return scan != null;
    }

    @Nullable
    public BoxValidator.Scan getScan() {
        return scan;
    }

    public boolean contains(BlockPos pos) {
        return scan != null && scan.contains(pos);
    }

    @Nullable
    public Component getLastValidationError() {
        return lastValidationError;
    }
}
