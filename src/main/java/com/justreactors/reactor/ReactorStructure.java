package com.justreactors.reactor;

import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Immutable snapshot of a validated reactor multiblock and its precomputed radiation geometry.
 * Rebuilt only when the multiblock is assembled or modified, so per-tick simulation is O(1).
 */
public record ReactorStructure(
        BlockPos min,
        BlockPos max,
        BlockPos controllerPos,
        int fuelRodCount,
        int interiorVolume,
        int casingSurfaceArea,
        List<BlockPos> fuelRodPositions,
        List<BlockPos> controlRods,
        List<BlockPos> powerTaps,
        List<BlockPos> accessPorts,
        List<BlockPos> redstonePorts,
        List<BlockPos> coolantPorts,
        double fuelToCasingConductivity,
        double averageAbsorption,
        double averageModeration,
        double averageHeatEfficiency,
        double rodFertilityFactor
) {
    public int sizeX() {
        return max.getX() - min.getX() + 1;
    }

    public int sizeY() {
        return max.getY() - min.getY() + 1;
    }

    public int sizeZ() {
        return max.getZ() - min.getZ() + 1;
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }
}
