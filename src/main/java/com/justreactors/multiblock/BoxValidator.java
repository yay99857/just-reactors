package com.justreactors.multiblock;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Validates simple rectangular box multiblocks (Heat Exchanger, Battery): casing edges, a wall made of the
 * listed shell blocks with exactly one controller, and an interior made of one "core" block or air.
 */
public final class BoxValidator {
    private BoxValidator() {
    }

    /**
     * @param prefix     translation prefix for the "no core blocks" message ({@code message.justreactors.<prefix>.no_core})
     * @param casing     the only block allowed on edges and corners
     * @param controller the controller block (exactly one, on a wall face)
     * @param wall       every block allowed on wall faces (casing, glass, controller, ports...)
     * @param core       the interior block that gives the machine its capacity
     */
    public record Spec(String prefix, Supplier<? extends Block> casing, Supplier<? extends Block> controller,
                       List<Supplier<? extends Block>> wall, Supplier<? extends Block> core, int minSize,
                       Supplier<Integer> maxSize) {
        boolean isWall(BlockState state) {
            Block block = state.getBlock();
            for (Supplier<? extends Block> s : wall) {
                if (s.get() == block) return true;
            }
            return false;
        }
    }

    public record Scan(BlockPos min, BlockPos max, Map<Block, List<BlockPos>> wallParts, List<BlockPos> core,
                       int interiorVolume) {
        public List<BlockPos> parts(Block block) {
            return wallParts.getOrDefault(block, List.of());
        }

        public boolean contains(BlockPos pos) {
            return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                    && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                    && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }

        public int sizeX() {
            return max.getX() - min.getX() + 1;
        }

        public int sizeY() {
            return max.getY() - min.getY() + 1;
        }

        public int sizeZ() {
            return max.getZ() - min.getZ() + 1;
        }
    }

    public sealed interface Result permits Result.Valid, Result.Invalid {
        record Valid(Scan scan) implements Result {
        }

        record Invalid(Component reason) implements Result {
        }
    }

    public static Result validate(Level level, BlockPos controllerPos, Spec spec) {
        int maxSize = spec.maxSize().get();
        int minX = controllerPos.getX(), maxX = minX;
        int minY = controllerPos.getY(), maxY = minY;
        int minZ = controllerPos.getZ(), maxZ = minZ;

        // Flood fill over wall blocks to find the bounding box.
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        visited.add(controllerPos);
        queue.add(controllerPos);
        int scanLimit = maxSize * maxSize * 6 + 64;
        while (!queue.isEmpty() && visited.size() <= scanLimit) {
            BlockPos current = queue.poll();
            minX = Math.min(minX, current.getX());
            maxX = Math.max(maxX, current.getX());
            minY = Math.min(minY, current.getY());
            maxY = Math.max(maxY, current.getY());
            minZ = Math.min(minZ, current.getZ());
            maxZ = Math.max(maxZ, current.getZ());
            if (maxX - minX + 1 > maxSize || maxY - minY + 1 > maxSize || maxZ - minZ + 1 > maxSize) {
                return invalid("message.justreactors.multiblock.too_large", maxSize);
            }
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (!visited.contains(next) && spec.isWall(level.getBlockState(next))) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        if (maxX - minX + 1 < spec.minSize() || maxY - minY + 1 < spec.minSize() || maxZ - minZ + 1 < spec.minSize()) {
            return invalid("message.justreactors.multiblock.too_small", spec.minSize());
        }

        Block casing = spec.casing().get();
        Block controller = spec.controller().get();
        Block core = spec.core().get();
        Map<Block, List<BlockPos>> wallParts = new HashMap<>();
        List<BlockPos> corePositions = new ArrayList<>();
        int controllers = 0;
        int interior = 0;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int axes = (x == minX || x == maxX ? 1 : 0) + (y == minY || y == maxY ? 1 : 0) + (z == minZ || z == maxZ ? 1 : 0);
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();
                    if (axes >= 2) {
                        if (block != casing) return invalid("message.justreactors.multiblock.edge_must_be_casing", x, y, z);
                        continue;
                    }
                    if (axes == 1) {
                        if (!spec.isWall(state)) return invalid("message.justreactors.multiblock.invalid_wall_block", x, y, z);
                        if (block == controller && ++controllers > 1) {
                            return invalid("message.justreactors.multiblock.multiple_controllers");
                        }
                        if (block != casing) wallParts.computeIfAbsent(block, b -> new ArrayList<>()).add(cursor.immutable());
                        continue;
                    }
                    interior++;
                    if (block == core) {
                        corePositions.add(cursor.immutable());
                    } else if (!state.isAir()) {
                        return invalid("message.justreactors.multiblock.invalid_interior_block", x, y, z);
                    }
                }
            }
        }
        if (controllers != 1) return invalid("message.justreactors.multiblock.missing_controller");
        if (corePositions.isEmpty()) return invalid("message.justreactors." + spec.prefix() + ".no_core");

        Map<Block, List<BlockPos>> frozen = new HashMap<>();
        wallParts.forEach((b, list) -> frozen.put(b, List.copyOf(list)));
        return new Result.Valid(new Scan(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ),
                Map.copyOf(frozen), List.copyOf(corePositions), interior));
    }

    private static Result.Invalid invalid(String key, Object... args) {
        return new Result.Invalid(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }
}
