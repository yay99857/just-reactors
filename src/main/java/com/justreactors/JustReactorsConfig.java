package com.justreactors;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Synced server config for Just Reactors. Every getter falls back to its default before config load.
 */
public final class JustReactorsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_REACTOR_SIZE;
    public static final ModConfigSpec.IntValue MAX_REACTOR_HEIGHT;
    public static final ModConfigSpec.IntValue ENERGY_BUFFER_CAPACITY;
    public static final ModConfigSpec.IntValue POWER_TAP_MAX_EXTRACT;
    public static final ModConfigSpec.DoubleValue POWER_PRODUCTION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FUEL_USAGE_MULTIPLIER;
    public static final ModConfigSpec.IntValue FUEL_CAPACITY_PER_ROD_MB;
    public static final ModConfigSpec.DoubleValue STEAM_PRODUCTION_MULTIPLIER;
    public static final ModConfigSpec.IntValue COOLANT_TANK_PER_ROD_MB;

    public static final ModConfigSpec.IntValue MAX_TURBINE_SIZE;
    public static final ModConfigSpec.IntValue MAX_TURBINE_HEIGHT;
    public static final ModConfigSpec.IntValue TURBINE_ENERGY_CAPACITY;
    public static final ModConfigSpec.DoubleValue TURBINE_POWER_MULTIPLIER;
    public static final ModConfigSpec.IntValue TURBINE_MAX_STEAM_FLOW;

    public static final ModConfigSpec.IntValue REPROCESSOR_ENERGY_COST;
    public static final ModConfigSpec.IntValue REPROCESSOR_WATER_COST;
    public static final ModConfigSpec.IntValue REPROCESSOR_TICKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("reactor");
        MAX_REACTOR_SIZE = b.comment("Maximum X/Z outer dimension of a reactor multiblock (minimum is 3).")
                .defineInRange("maxSize", 32, 3, 64);
        MAX_REACTOR_HEIGHT = b.comment("Maximum Y outer dimension of a reactor multiblock (minimum is 3).")
                .defineInRange("maxHeight", 32, 3, 64);
        ENERGY_BUFFER_CAPACITY = b.comment("Internal passive energy buffer capacity in FE.")
                .defineInRange("energyBufferCapacity", 10_000_000, 100_000, Integer.MAX_VALUE);
        POWER_TAP_MAX_EXTRACT = b.comment("Maximum FE/t pushed out per Reactor Power Tap.")
                .defineInRange("powerTapMaxExtract", 1_000_000, 1_000, Integer.MAX_VALUE);
        POWER_PRODUCTION_MULTIPLIER = b.comment("Global multiplier for passive reactor FE/t output.")
                .defineInRange("powerProductionMultiplier", 1.0, 0.05, 100.0);
        FUEL_USAGE_MULTIPLIER = b.comment("Global multiplier for reactor fuel burn rate.")
                .defineInRange("fuelUsageMultiplier", 1.0, 0.05, 100.0);
        FUEL_CAPACITY_PER_ROD_MB = b.comment("Fuel capacity per Reactor Fuel Rod block in mB (1,000 mB = 1 ingot).")
                .defineInRange("fuelCapacityPerRodMb", 4_000, 1_000, 64_000);
        STEAM_PRODUCTION_MULTIPLIER = b.comment("Global multiplier for active-cooled reactor steam production.")
                .defineInRange("steamProductionMultiplier", 1.0, 0.05, 100.0);
        COOLANT_TANK_PER_ROD_MB = b.comment("Coolant & steam tank capacity per Reactor Fuel Rod in mB.")
                .defineInRange("coolantTankPerRodMb", 5_000, 1_000, 100_000);
        b.pop();

        b.push("turbine");
        MAX_TURBINE_SIZE = b.comment("Maximum perpendicular outer dimension of a Steam Turbine multiblock.")
                .defineInRange("maxSize", 16, 5, 32);
        MAX_TURBINE_HEIGHT = b.comment("Maximum length along the rotor shaft axis of a Steam Turbine multiblock.")
                .defineInRange("maxHeight", 32, 4, 64);
        TURBINE_ENERGY_CAPACITY = b.comment("Internal FE buffer capacity of a Steam Turbine.")
                .defineInRange("energyCapacity", 10_000_000, 100_000, Integer.MAX_VALUE);
        TURBINE_POWER_MULTIPLIER = b.comment("Global multiplier for Steam Turbine FE/t output.")
                .defineInRange("powerMultiplier", 1.0, 0.05, 100.0);
        TURBINE_MAX_STEAM_FLOW = b.comment("Maximum steam intake rate in mB/t for a Steam Turbine.")
                .defineInRange("maxSteamFlow", 2_000, 100, 50_000);
        b.pop();

        b.push("reprocessor");
        REPROCESSOR_ENERGY_COST = b.comment("FE consumed per Cyanite Reprocessor operation.")
                .defineInRange("energyCost", 20_000, 1_000, 1_000_000);
        REPROCESSOR_WATER_COST = b.comment("Water in mB consumed per Cyanite Reprocessor operation.")
                .defineInRange("waterCost", 1_000, 100, 8_000);
        REPROCESSOR_TICKS = b.comment("Ticks required per Cyanite Reprocessor operation.")
                .defineInRange("ticks", 100, 10, 2_400);
        b.pop();

        SPEC = b.build();
    }

    private JustReactorsConfig() {
    }

    public static int get(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.getAsInt() : value.getDefault();
    }

    public static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.getAsDouble() : value.getDefault();
    }
}
