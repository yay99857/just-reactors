package com.justreactors.turbine;

import com.justreactors.JustReactorsConfig;
import com.justreactors.registry.ModBlocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Validates and assembles a rectangular Steam Turbine multiblock starting from a {@code TurbineControllerBlock}.
 */
public final class TurbineValidator {
    private TurbineValidator() {
    }

    public sealed interface Result permits Result.Valid, Result.Invalid {
        record Valid(TurbineStructure structure) implements Result {
        }

        record Invalid(Component reason) implements Result {
        }
    }

    public static Result validate(Level level, BlockPos controllerPos) {
        int maxSize = JustReactorsConfig.get(JustReactorsConfig.MAX_TURBINE_SIZE);
        int maxHeight = JustReactorsConfig.get(JustReactorsConfig.MAX_TURBINE_HEIGHT);
        int absoluteMax = Math.max(maxSize, maxHeight);

        int minX = controllerPos.getX(), maxX = controllerPos.getX();
        int minY = controllerPos.getY(), maxY = controllerPos.getY();
        int minZ = controllerPos.getZ(), maxZ = controllerPos.getZ();

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        visited.add(controllerPos);
        queue.add(controllerPos);

        int maxShellScan = maxSize * maxSize * 2 + maxSize * maxHeight * 4 + 64;
        while (!queue.isEmpty() && visited.size() <= maxShellScan) {
            BlockPos current = queue.poll();
            if (current.getX() < minX) minX = current.getX();
            if (current.getX() > maxX) maxX = current.getX();
            if (current.getY() < minY) minY = current.getY();
            if (current.getY() > maxY) maxY = current.getY();
            if (current.getZ() < minZ) minZ = current.getZ();
            if (current.getZ() > maxZ) maxZ = current.getZ();

            if (maxX - minX + 1 > absoluteMax || maxY - minY + 1 > absoluteMax || maxZ - minZ + 1 > absoluteMax) {
                return invalid("message.justreactors.turbine.too_large", maxSize, maxHeight);
            }

            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (!visited.contains(next) && isShellBlock(level.getBlockState(next))) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        if (sizeX < 4 || sizeY < 4 || sizeZ < 4) {
            return invalid("message.justreactors.turbine.too_small");
        }

        List<BlockPos> powerTaps = new ArrayList<>();
        List<BlockPos> fluidPorts = new ArrayList<>();
        List<BlockPos> redstonePorts = new ArrayList<>();
        int controllerCount = 0;
        BlockPos bearingPos = null;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean onX = (x == minX || x == maxX);
                    boolean onY = (y == minY || y == maxY);
                    boolean onZ = (z == minZ || z == maxZ);
                    int boundaryAxes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);
                    if (boundaryAxes == 0) continue;

                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();

                    if (boundaryAxes >= 2) {
                        if (block != ModBlocks.TURBINE_CASING.get()) {
                            return invalid("message.justreactors.turbine.edge_must_be_casing", x, y, z);
                        }
                        continue;
                    }

                    if (!isShellBlock(state)) {
                        return invalid("message.justreactors.turbine.invalid_wall_block", x, y, z);
                    }
                    if (block == ModBlocks.TURBINE_CONTROLLER.get()) {
                        controllerCount++;
                        if (controllerCount > 1) return invalid("message.justreactors.turbine.multiple_controllers");
                    } else if (block == ModBlocks.TURBINE_ROTOR_BEARING.get()) {
                        if (bearingPos != null) return invalid("message.justreactors.turbine.multiple_bearings");
                        bearingPos = cursor.immutable();
                    } else if (block == ModBlocks.TURBINE_POWER_TAP.get()) {
                        powerTaps.add(cursor.immutable());
                    } else if (block == ModBlocks.TURBINE_FLUID_PORT.get()) {
                        fluidPorts.add(cursor.immutable());
                    } else if (block == ModBlocks.TURBINE_REDSTONE_PORT.get()) {
                        redstonePorts.add(cursor.immutable());
                    }
                }
            }
        }

        if (controllerCount != 1) {
            return invalid("message.justreactors.turbine.missing_controller");
        }
        if (bearingPos == null) {
            return invalid("message.justreactors.turbine.missing_bearing");
        }

        Direction inwardDir = inwardFromBearing(bearingPos, minX, maxX, minY, maxY, minZ, maxZ);
        Direction.Axis shaftAxis = inwardDir.getAxis();

        int axisLength = shaftAxis == Direction.Axis.X ? sizeX : (shaftAxis == Direction.Axis.Y ? sizeY : sizeZ);
        int perpA = shaftAxis == Direction.Axis.X ? sizeY : sizeX;
        int perpB = shaftAxis == Direction.Axis.Z ? sizeY : sizeZ;

        if (perpA < 5 || perpB < 5) {
            return invalid("message.justreactors.turbine.too_small");
        }
        if (perpA > maxSize || perpB > maxSize || axisLength > maxHeight) {
            return invalid("message.justreactors.turbine.too_large", maxSize, maxHeight);
        }

        // Validate straight rotor shaft along shaftAxis
        int shaftLength = axisLength - 2;
        List<BlockPos> shaftPosList = new ArrayList<>(shaftLength);
        Set<BlockPos> shaftPositions = new HashSet<>();
        for (int step = 1; step <= shaftLength; step++) {
            BlockPos shaftPos = bearingPos.relative(inwardDir, step);
            if (level.getBlockState(shaftPos).getBlock() != ModBlocks.TURBINE_ROTOR_SHAFT.get()) {
                return invalid("message.justreactors.turbine.missing_shaft",
                        shaftPos.getX(), shaftPos.getY(), shaftPos.getZ());
            }
            shaftPosList.add(shaftPos);
            shaftPositions.add(shaftPos);
        }

        // Scan interior for blades, coils, and invalid blocks
        List<BlockPos> bladePosList = new ArrayList<>();
        int bladeCount = 0;
        int coilCount = 0;
        double effSum = 0.0;
        double bonusSum = 0.0;
        double dragSum = 0.0;

        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                for (int z = minZ + 1; z <= maxZ - 1; z++) {
                    cursor.set(x, y, z);
                    if (shaftPositions.contains(cursor)) continue;

                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();
                    if (isShellBlock(state)) {
                        return invalid("message.justreactors.turbine.invalid_interior_block", x, y, z);
                    }
                    if (block == ModBlocks.TURBINE_ROTOR_SHAFT.get()) {
                        return invalid("message.justreactors.turbine.extra_shaft", x, y, z);
                    }
                    if (block == ModBlocks.TURBINE_ROTOR_BLADE.get()) {
                        bladePosList.add(cursor.immutable());
                        bladeCount++;
                        continue;
                    }
                    TurbineCoil coil = TurbineCoil.of(state);
                    if (coil != null && isAdjacentRingToShaft(cursor, bearingPos, shaftAxis)) {
                        coilCount++;
                        effSum += coil.efficiency();
                        bonusSum += coil.bonus();
                        dragSum += coil.drag();
                    }
                }
            }
        }

        if (bladeCount <= 0) {
            return invalid("message.justreactors.turbine.no_blades");
        }

        double avgEff = coilCount > 0 ? effSum / coilCount : 1.0;
        double avgBonus = coilCount > 0 ? bonusSum / coilCount : 1.0;
        double totalDrag = coilCount > 0 ? dragSum : 0.0;
        double rotorMass = (shaftLength + bladeCount) * 10.0;

        TurbineStructure structure = new TurbineStructure(
                new BlockPos(minX, minY, minZ),
                new BlockPos(maxX, maxY, maxZ),
                controllerPos.immutable(),
                bearingPos,
                inwardDir,
                shaftAxis,
                shaftLength,
                bladeCount,
                coilCount,
                avgEff,
                avgBonus,
                totalDrag,
                rotorMass,
                List.copyOf(shaftPosList),
                List.copyOf(bladePosList),
                List.copyOf(powerTaps),
                List.copyOf(fluidPorts),
                List.copyOf(redstonePorts)
        );
        return new Result.Valid(structure);
    }

    private static Direction inwardFromBearing(BlockPos bearing, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        if (bearing.getX() == minX) return Direction.EAST;
        if (bearing.getX() == maxX) return Direction.WEST;
        if (bearing.getY() == minY) return Direction.UP;
        if (bearing.getY() == maxY) return Direction.DOWN;
        if (bearing.getZ() == minZ) return Direction.SOUTH;
        return Direction.NORTH;
    }

    private static boolean isAdjacentRingToShaft(BlockPos pos, BlockPos bearingPos, Direction.Axis axis) {
        int dx = Math.abs(pos.getX() - bearingPos.getX());
        int dy = Math.abs(pos.getY() - bearingPos.getY());
        int dz = Math.abs(pos.getZ() - bearingPos.getZ());
        return switch (axis) {
            case X -> dy <= 1 && dz <= 1 && (dy + dz) > 0;
            case Y -> dx <= 1 && dz <= 1 && (dx + dz) > 0;
            case Z -> dx <= 1 && dy <= 1 && (dx + dy) > 0;
        };
    }

    public static boolean isShellBlock(BlockState state) {
        Block block = state.getBlock();
        return block == ModBlocks.TURBINE_CASING.get()
                || block == ModBlocks.TURBINE_GLASS.get()
                || block == ModBlocks.TURBINE_CONTROLLER.get()
                || block == ModBlocks.TURBINE_ROTOR_BEARING.get()
                || block == ModBlocks.TURBINE_FLUID_PORT.get()
                || block == ModBlocks.TURBINE_POWER_TAP.get()
                || block == ModBlocks.TURBINE_REDSTONE_PORT.get();
    }

    private static Result.Invalid invalid(String key, Object... args) {
        return new Result.Invalid(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }
}
