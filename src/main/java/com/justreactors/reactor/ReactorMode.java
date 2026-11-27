package com.justreactors.reactor;

/**
 * Operating mode of a reactor multiblock.
 * PASSIVE generates Forge Energy directly; ACTIVE (planned for v1.1) will boil coolant into steam for Turbines.
 */
public enum ReactorMode {
    PASSIVE,
    ACTIVE
}
