package com.justreactors.turbine;

import java.util.Locale;

/**
 * Controls how a Steam Turbine handles condensed water when its water tank fills up.
 */
public enum TurbineVentMode {
    OVERFLOW,
    ALL,
    NONE;

    public TurbineVentMode next() {
        TurbineVentMode[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "gui.justreactors.turbine.vent." + id();
    }

    public static TurbineVentMode byOrdinal(int ordinal) {
        TurbineVentMode[] vals = values();
        if (ordinal < 0 || ordinal >= vals.length) return OVERFLOW;
        return vals[ordinal];
    }
}
