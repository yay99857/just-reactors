package com.justreactors.reactor;

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
 * Validates and assembles a rectangular reactor multiblock (3x3x3 up to configured max dimensions)
 * starting from a {@code ReactorControllerBlock}.
 */
public final class ReactorValidator {
    private static final int RADIATION_RANGE = 4;

    private ReactorValidator() {
    }

    public sealed interface Result permits Result.Valid, Result.Invalid {
        record Valid(ReactorStructure structure) implements Result {
        }

        record Invalid(Component reason) implements Result {
        }
    }

    public static Result validate(Level level, BlockPos controllerPos) {
        int maxSize = JustReactorsConfig.get(JustReactorsConfig.MAX_REACTOR_SIZE);
        int maxHeight = JustReactorsConfig.get(JustReactorsConfig.MAX_REACTOR_HEIGHT);

        // Flood-fill connected exterior reactor shell blocks to find the bounding box.
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

            if (maxX - minX + 1 > maxSize || maxZ - minZ + 1 > maxSize || maxY - minY + 1 > maxHeight) {
                return invalid("message.justreactors.too_large", maxSize, maxHeight);
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
        if (sizeX < 3 || sizeY < 3 || sizeZ < 3) {
            return invalid("message.justreactors.too_small");
        }
        if (sizeX > maxSize || sizeZ > maxSize || sizeY > maxHeight) {
            return invalid("message.justreactors.too_large", maxSize, maxHeight);
        }

        List<BlockPos> controlRods = new ArrayList<>();
        List<BlockPos> powerTaps = new ArrayList<>();
        List<BlockPos> accessPorts = new ArrayList<>();
        List<BlockPos> redstonePorts = new ArrayList<>();
        List<BlockPos> coolantPorts = new ArrayList<>();
        int controllerCount = 0;

        // Pass 1: Validate outer shell (edges must be Casing, faces valid shell parts).
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean onX = (x == minX || x == maxX);
                    boolean onY = (y == minY || y == maxY);
                    boolean onZ = (z == minZ || z == maxZ);
                    int boundaryAxes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);
                    if (boundaryAxes == 0) continue; // interior

                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();

                    if (boundaryAxes >= 2) {
                        // Frame edge or corner: must be Reactor Casing.
                        if (block != ModBlocks.REACTOR_CASING.get()) {
                            return invalid("message.justreactors.edge_must_be_casing", x, y, z);
                        }
                        continue;
                    }

                    // Face block (boundaryAxes == 1)
                    if (!isShellBlock(state)) {
                        return invalid("message.justreactors.invalid_wall_block", x, y, z);
                    }
                    if (block == ModBlocks.REACTOR_CONTROLLER.get()) {
                        if (onY) return invalid("message.justreactors.controller_on_side", x, y, z);
                        controllerCount++;
                        if (controllerCount > 1) return invalid("message.justreactors.multiple_controllers");
                    } else if (block == ModBlocks.REACTOR_CONTROL_ROD.get()) {
                        if (y != maxY) return invalid("message.justreactors.control_rod_top_only", x, y, z);
                        controlRods.add(cursor.immutable());
                    } else if (block == ModBlocks.REACTOR_POWER_TAP.get()) {
                        powerTaps.add(cursor.immutable());
                    } else if (block == ModBlocks.REACTOR_ACCESS_PORT.get()) {
                        accessPorts.add(cursor.immutable());
                    } else if (block == ModBlocks.REACTOR_REDSTONE_PORT.get()) {
                        redstonePorts.add(cursor.immutable());
                    } else if (block == ModBlocks.REACTOR_COOLANT_PORT.get()) {
                        coolantPorts.add(cursor.immutable());
                    }
                }
            }
        }

        if (controllerCount != 1) {
            return invalid("message.justreactors.missing_controller");
        }
        if (controlRods.isEmpty()) {
            return invalid("message.justreactors.no_fuel_rods");
        }

        // Pass 2: Validate interior (vertical Fuel Rod columns topped by Control Rods; no stray shell blocks).
        Set<Long> controlRodColumns = new HashSet<>();
        for (BlockPos rodPos : controlRods) {
            controlRodColumns.add(columnKey(rodPos.getX(), rodPos.getZ()));
        }

        int fuelRodCount = 0;
        double conductivitySum = 0.0;
        int moderatorBlocks = 0;
        Set<BlockPos> fuelRodPositions = new HashSet<>();

        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int z = minZ + 1; z <= maxZ - 1; z++) {
                boolean hasControlRod = controlRodColumns.contains(columnKey(x, z));
                for (int y = minY + 1; y <= maxY - 1; y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    Block block = state.getBlock();

                    if (hasControlRod) {
                        if (block != ModBlocks.REACTOR_FUEL_ROD.get()) {
                            return invalid("message.justreactors.missing_fuel_rod", x, y, z);
                        }
                        fuelRodCount++;
                        fuelRodPositions.add(cursor.immutable());
                    } else {
                        if (block == ModBlocks.REACTOR_FUEL_ROD.get()) {
                            return invalid("message.justreactors.missing_control_rod", x, maxY, z);
                        }
                        if (isShellBlock(state)) {
                            return invalid("message.justreactors.invalid_interior_block", x, y, z);
                        }
                        ReactorModerator mod = ReactorModerator.of(state);
                        conductivitySum += mod.heatConductivity();
                        moderatorBlocks++;
                    }
                }
            }
        }

        // Pass 3: Precompute ER2-style radiation geometry along the 4 horizontal axes from each fuel rod.
        double totalAbsorption = 0.0;
        double totalModeration = 0.0;
        double totalHeatEfficiency = 0.0;
        int raySamples = 0;
        int rodHits = 0;

        Direction[] horizontals = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (BlockPos rodPos : fuelRodPositions) {
            for (Direction dir : horizontals) {
                for (int dist = 1; dist <= RADIATION_RANGE; dist++) {
                    BlockPos samplePos = rodPos.relative(dir, dist);
                    if (samplePos.getX() <= minX || samplePos.getX() >= maxX
                            || samplePos.getZ() <= minZ || samplePos.getZ() >= maxZ) {
                        break; // hit reactor wall
                    }
                    if (fuelRodPositions.contains(samplePos)) {
                        rodHits++;
                        break; // hit another fuel rod -> boosts fertility!
                    }
                    ReactorModerator mod = ReactorModerator.of(level.getBlockState(samplePos));
                    totalAbsorption += mod.absorption();
                    totalModeration += mod.moderation();
                    totalHeatEfficiency += mod.heatEfficiency();
                    raySamples++;
                }
            }
        }

        int interiorVolume = (sizeX - 2) * (sizeY - 2) * (sizeZ - 2);
        int casingSurfaceArea = 2 * (sizeX * sizeY + sizeX * sizeZ + sizeY * sizeZ);

        double avgConductivity = moderatorBlocks > 0 ? conductivitySum / moderatorBlocks : ReactorModerator.AIR.heatConductivity();
        double fuelToCasingConductivity = Math.max(0.05, avgConductivity) * (0.25 + fuelRodCount * 0.08);
        double avgAbsorption = raySamples > 0 ? totalAbsorption / raySamples : ReactorModerator.AIR.absorption();
        double avgModeration = raySamples > 0 ? totalModeration / raySamples : ReactorModerator.AIR.moderation();
        double avgHeatEfficiency = raySamples > 0 ? totalHeatEfficiency / raySamples : ReactorModerator.AIR.heatEfficiency();
        // More neighbouring fuel rods -> higher fertility bonus (classic ER2 checkerboard / cluster payoff)
        double rodFertilityFactor = 1.0 + (fuelRodCount > 0 ? (double) rodHits / (fuelRodCount * 2.0) : 0.0);

        ReactorStructure structure = new ReactorStructure(
                new BlockPos(minX, minY, minZ),
                new BlockPos(maxX, maxY, maxZ),
                controllerPos.immutable(),
                fuelRodCount,
                interiorVolume,
                casingSurfaceArea,
                List.copyOf(fuelRodPositions),
                List.copyOf(controlRods),
                List.copyOf(powerTaps),
                List.copyOf(accessPorts),
                List.copyOf(redstonePorts),
                List.copyOf(coolantPorts),
                fuelToCasingConductivity,
                avgAbsorption,
                avgModeration,
                avgHeatEfficiency,
                rodFertilityFactor
        );
        return new Result.Valid(structure);
    }

    public static boolean isShellBlock(BlockState state) {
        Block block = state.getBlock();
        return block == ModBlocks.REACTOR_CASING.get()
                || block == ModBlocks.REACTOR_GLASS.get()
                || block == ModBlocks.REACTOR_CONTROLLER.get()
                || block == ModBlocks.REACTOR_CONTROL_ROD.get()
                || block == ModBlocks.REACTOR_POWER_TAP.get()
                || block == ModBlocks.REACTOR_ACCESS_PORT.get()
                || block == ModBlocks.REACTOR_REDSTONE_PORT.get()
                || block == ModBlocks.REACTOR_COOLANT_PORT.get();
    }

    private static long columnKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xFFFFFFFFL);
    }

    private static Result.Invalid invalid(String key, Object... args) {
        return new Result.Invalid(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }
}
