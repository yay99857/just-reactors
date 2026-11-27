package com.justreactors.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side index of every assembled multiblock's bounding box, keyed by controller position.
 * Used to protect formed machines (e.g. from Carry On) without scanning the world.
 */
public final class AssembledMultiblocks {
    private record Box(BlockPos min, BlockPos max) {
        boolean contains(BlockPos p) {
            return p.getX() >= min.getX() && p.getX() <= max.getX()
                    && p.getY() >= min.getY() && p.getY() <= max.getY()
                    && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
        }
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, Box>> BOXES = new ConcurrentHashMap<>();

    private AssembledMultiblocks() {
    }

    public static void put(Level level, BlockPos controller, BlockPos min, BlockPos max) {
        if (level.isClientSide()) return;
        BOXES.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .put(controller.immutable(), new Box(min.immutable(), max.immutable()));
    }

    public static void remove(Level level, BlockPos controller) {
        if (level.isClientSide()) return;
        Map<BlockPos, Box> map = BOXES.get(level.dimension());
        if (map != null) map.remove(controller);
    }

    public static boolean isInside(Level level, BlockPos pos) {
        Map<BlockPos, Box> map = BOXES.get(level.dimension());
        if (map == null) return false;
        for (Box box : map.values()) {
            if (box.contains(pos)) return true;
        }
        return false;
    }

    public static void clear() {
        BOXES.clear();
    }
}
