package com.justreactors.multiblock;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Context-aware exterior trim role assigned to a multiblock casing block while assembled.
 */
public enum CasingPart implements StringRepresentable {
    NONE,
    BASE,
    PILLAR,
    TOP_EDGE,
    CORNER,
    PANEL;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
