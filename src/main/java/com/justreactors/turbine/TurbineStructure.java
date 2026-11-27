package com.justreactors.turbine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

/**
 * Immutable snapshot of a validated Steam Turbine multiblock.
 */
public record TurbineStructure(
        BlockPos min,
        BlockPos max,
        BlockPos controllerPos,
        BlockPos bearingPos,
        Direction inwardDir,
        Direction.Axis shaftAxis,
        int shaftLength,
        int bladeCount,
        int coilCount,
        double coilEfficiency,
        double coilBonus,
        double coilDrag,
        double rotorMass,
        List<BlockPos> shaftPositions,
        List<BlockPos> bladePositions,
        List<BlockPos> powerTaps,
        List<BlockPos> fluidPorts,
        List<BlockPos> redstonePorts
) {
    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }
}
