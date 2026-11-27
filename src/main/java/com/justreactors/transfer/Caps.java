package com.justreactors.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

/**
 * Forge 1.20.1 capability plumbing: the newer branches look handlers up through block capabilities
 * registered in one place, here each block entity answers {@code getCapability} itself.
 */
public final class Caps {
    private Caps() {
    }

    /** Handler exposed by the block entity at {@code pos}, or null. */
    @Nullable
    public static <T> T block(Level level, BlockPos pos, Capability<T> cap, @Nullable Direction side) {
        if (!level.isLoaded(pos)) return null;
        BlockEntity be = level.getBlockEntity(pos);
        return be == null ? null : be.getCapability(cap, side).resolve().orElse(null);
    }

    /** Handler exposed by an item stack (e.g. a bucket's fluid handler), or null. */
    @Nullable
    public static <T> T item(ItemStack stack, Capability<T> cap) {
        return stack.isEmpty() ? null : stack.getCapability(cap).resolve().orElse(null);
    }

    /** Wraps a handler that may be absent (e.g. a port with no assembled controller). */
    public static <T> LazyOptional<T> of(@Nullable T value) {
        return value == null ? LazyOptional.empty() : LazyOptional.of(() -> value);
    }
}
