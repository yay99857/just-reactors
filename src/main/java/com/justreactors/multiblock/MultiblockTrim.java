package com.justreactors.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Shared helper that applies and clears {@link CasingPart} trim states across any rectangular multiblock shell.
 */
public final class MultiblockTrim {
    public static final EnumProperty<CasingPart> PART = EnumProperty.create("part", CasingPart.class);

    private MultiblockTrim() {
    }

    public static void apply(Level level, BlockPos min, BlockPos max, Block casingBlock) {
        update(level, min, max, casingBlock, true);
    }

    public static void reset(Level level, BlockPos min, BlockPos max, Block casingBlock) {
        update(level, min, max, casingBlock, false);
    }

    private static void update(Level level, BlockPos min, BlockPos max, Block casingBlock, boolean assembled) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    boolean onX = (x == min.getX() || x == max.getX());
                    boolean onY = (y == min.getY() || y == max.getY());
                    boolean onZ = (z == min.getZ() || z == max.getZ());
                    if (!onX && !onY && !onZ) continue;
                    cursor.set(x, y, z);
                    BlockState st = level.getBlockState(cursor);
                    if (assembled && st.getBlock() instanceof ConnectedGlassBlock glass) {
                        BlockState refreshed = glass.refreshConnections(st, level, cursor);
                        if (refreshed != st) {
                            level.setBlock(cursor, refreshed, Block.UPDATE_CLIENTS);
                        }
                        continue;
                    }
                    if (st.getBlock() != casingBlock || !st.hasProperty(PART)) continue;

                    CasingPart part = !assembled ? CasingPart.NONE
                            : (y == min.getY() && (onX || onZ)) ? CasingPart.BASE
                            : (y == max.getY() && onX && onZ) ? CasingPart.CORNER
                            : (y == max.getY() && (onX || onZ)) ? CasingPart.TOP_EDGE
                            : (onX && onZ) ? CasingPart.PILLAR
                            : CasingPart.PANEL;
                    if (st.getValue(PART) != part) {
                        level.setBlock(cursor, st.setValue(PART, part), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }
}
