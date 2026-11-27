package com.justreactors.reactor;

/**
 * Operating modes for the {@code ReactorRedstonePortBlock}.
 */
public enum RedstonePortMode {
    /** Input: turns reactor ON when signal > 0, OFF when 0. */
    TOGGLE_ON_SIGNAL(false),
    /** Input: sets all control rods to {@code round(signal * 100 / 15)}% insertion. */
    CONTROL_ROD_SIGNAL(false),
    /** Output: emits signal 15 when buffer/steam fill is below threshold%. */
    OUTPUT_ENERGY_BELOW(true),
    /** Output: emits signal 15 when buffer/steam fill is at or above threshold%. */
    OUTPUT_ENERGY_ABOVE(true),
    /** Output: emits signal 15 when core fuel temperature is at or above thresholdPercent * 20 C. */
    OUTPUT_FUEL_TEMP_ABOVE(true),
    /** Output: emits signal 15 when fuel reserve is below threshold%. */
    OUTPUT_FUEL_BELOW(true);

    private final boolean output;

    RedstonePortMode(boolean output) {
        this.output = output;
    }

    public boolean isOutput() {
        return output;
    }

    public RedstonePortMode next() {
        RedstonePortMode[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }

    public RedstonePortMode prev() {
        RedstonePortMode[] vals = values();
        return vals[(ordinal() - 1 + vals.length) % vals.length];
    }

    public String translationKey() {
        return "gui.justreactors.redstone_port.mode." + name().toLowerCase();
    }

    public static RedstonePortMode byOrdinal(int ordinal) {
        RedstonePortMode[] vals = values();
        if (ordinal < 0 || ordinal >= vals.length) return TOGGLE_ON_SIGNAL;
        return vals[ordinal];
    }
}
