package com.justreactors.battery;

import java.util.Locale;

/** What the Battery Redstone Sensor reports. */
public enum BatterySensorMode {
    CHARGE_ABOVE(true),
    CHARGE_BELOW(true),
    FULL(false),
    EMPTY(false),
    DISABLED(false);

    private final boolean usesThreshold;

    BatterySensorMode(boolean usesThreshold) {
        this.usesThreshold = usesThreshold;
    }

    public boolean usesThreshold() {
        return usesThreshold;
    }

    public BatterySensorMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public BatterySensorMode prev() {
        return values()[(ordinal() - 1 + values().length) % values().length];
    }

    public String translationKey() {
        return "gui.justreactors.battery_sensor.mode." + name().toLowerCase(Locale.ROOT);
    }

    public static BatterySensorMode byOrdinal(int ordinal) {
        BatterySensorMode[] v = values();
        return ordinal >= 0 && ordinal < v.length ? v[ordinal] : CHARGE_ABOVE;
    }

    /** Whether the sensor should emit for the given charge. */
    public boolean test(long energy, long capacity, int thresholdPercent) {
        if (capacity <= 0) return false;
        double pct = energy * 100.0 / capacity;
        return switch (this) {
            case CHARGE_ABOVE -> pct >= thresholdPercent;
            case CHARGE_BELOW -> pct <= thresholdPercent;
            case FULL -> energy >= capacity;
            case EMPTY -> energy <= 0;
            case DISABLED -> false;
        };
    }
}
