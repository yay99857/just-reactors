package com.justreactors.turbine;

import java.util.Locale;

/**
 * Operating modes for the {@code TurbineRedstonePortBlock}.
 */
public enum TurbineRedstonePortMode {
    /** Input: turns turbine ON when signal > 0, OFF when 0. */
    TOGGLE_ON_SIGNAL(false),
    /** Input: engages induction coils when signal > 0, disengages when 0. */
    TOGGLE_COILS_SIGNAL(false),
    /** Input: scales max steam flow rate from 0 to configured max based on signal (0..15). */
    FLOW_RATE_SIGNAL(false),
    /** Output: emits signal 15 when rotor speed is at or above thresholdPercent * 30 RPM. */
    OUTPUT_RPM_ABOVE(true),
    /** Output: emits signal 15 when rotor speed is below thresholdPercent * 30 RPM. */
    OUTPUT_RPM_BELOW(true),
    /** Output: emits signal 15 when energy buffer fill is at or above thresholdPercent%. */
    OUTPUT_ENERGY_ABOVE(true),
    /** Output: emits signal 15 when energy buffer fill is below thresholdPercent%. */
    OUTPUT_ENERGY_BELOW(true);

    private final boolean output;

    TurbineRedstonePortMode(boolean output) {
        this.output = output;
    }

    public boolean isOutput() {
        return output;
    }

    public boolean isRpmThreshold() {
        return this == OUTPUT_RPM_ABOVE || this == OUTPUT_RPM_BELOW;
    }

    public TurbineRedstonePortMode next() {
        TurbineRedstonePortMode[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }

    public TurbineRedstonePortMode prev() {
        TurbineRedstonePortMode[] vals = values();
        return vals[(ordinal() - 1 + vals.length) % vals.length];
    }

    public String translationKey() {
        return "gui.justreactors.turbine_redstone_port.mode." + name().toLowerCase(Locale.ROOT);
    }

    public static TurbineRedstonePortMode byOrdinal(int ordinal) {
        TurbineRedstonePortMode[] vals = values();
        if (ordinal < 0 || ordinal >= vals.length) return TOGGLE_ON_SIGNAL;
        return vals[ordinal];
    }
}
