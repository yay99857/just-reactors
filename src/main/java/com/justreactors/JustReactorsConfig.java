package com.justreactors;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Synced server config for Just Reactors. Every getter falls back to its default before config load.
 */
public final class JustReactorsConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_REACTOR_SIZE;
    public static final ForgeConfigSpec.IntValue MAX_REACTOR_HEIGHT;
    public static final ForgeConfigSpec.IntValue ENERGY_BUFFER_CAPACITY;
    public static final ForgeConfigSpec.IntValue POWER_TAP_MAX_EXTRACT;
    public static final ForgeConfigSpec.DoubleValue POWER_PRODUCTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FUEL_USAGE_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue FUEL_CAPACITY_PER_ROD_MB;
    public static final ForgeConfigSpec.DoubleValue STEAM_PRODUCTION_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue COOLANT_TANK_PER_ROD_MB;

    public static final ForgeConfigSpec.IntValue MAX_TURBINE_SIZE;
    public static final ForgeConfigSpec.IntValue MAX_TURBINE_HEIGHT;
    public static final ForgeConfigSpec.IntValue TURBINE_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue TURBINE_POWER_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue TURBINE_MAX_STEAM_FLOW;

    public static final ForgeConfigSpec.IntValue COOLANT_HEAT_RATIO;

    public static final ForgeConfigSpec.IntValue MAX_EXCHANGER_SIZE;
    public static final ForgeConfigSpec.IntValue EXCHANGER_HOT_COOLANT_PER_TUBE;
    public static final ForgeConfigSpec.IntValue EXCHANGER_STEAM_PER_HOT_COOLANT;

    public static final ForgeConfigSpec.IntValue MAX_BATTERY_SIZE;
    public static final ForgeConfigSpec.IntValue BATTERY_CELL_CAPACITY;
    public static final ForgeConfigSpec.IntValue BATTERY_CELL_TRANSFER;

    public static final ForgeConfigSpec.IntValue REPROCESSOR_ENERGY_COST;
    public static final ForgeConfigSpec.IntValue REPROCESSOR_WATER_COST;
    public static final ForgeConfigSpec.IntValue REPROCESSOR_TICKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

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
        COOLANT_HEAT_RATIO = b.comment("How many mB of steam worth of heat 1 mB of Coolant carries out of an active reactor as Hot Coolant.")
                .defineInRange("coolantHeatRatio", 4, 1, 64);
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

        b.push("heatExchanger");
        MAX_EXCHANGER_SIZE = b.comment("Maximum outer dimension of a Heat Exchanger multiblock (minimum is 3).")
                .defineInRange("maxSize", 16, 3, 32);
        EXCHANGER_HOT_COOLANT_PER_TUBE = b.comment("Hot Coolant in mB/t each Heat Exchanger Tube can cool down.")
                .defineInRange("hotCoolantPerTube", 25, 1, 10_000);
        EXCHANGER_STEAM_PER_HOT_COOLANT = b.comment("Steam in mB produced per mB of Hot Coolant cooled (water is consumed 1:1 with steam).",
                        "Higher than reactor.coolantHeatRatio, so a closed coolant loop gives more steam than boiling water directly.")
                .defineInRange("steamPerHotCoolant", 5, 1, 64);
        b.pop();

        b.push("battery");
        MAX_BATTERY_SIZE = b.comment("Maximum outer dimension of a Battery multiblock (minimum is 3).")
                .defineInRange("maxSize", 16, 3, 32);
        BATTERY_CELL_CAPACITY = b.comment("FE stored per Battery Cell. The total is capped at 2,147,483,647 FE.")
                .defineInRange("cellCapacity", 10_000_000, 10_000, Integer.MAX_VALUE);
        BATTERY_CELL_TRANSFER = b.comment("FE/t of input and of output each Battery Cell adds to the battery.")
                .defineInRange("cellTransfer", 10_000, 100, Integer.MAX_VALUE);
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

    public static int get(ForgeConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static double get(ForgeConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
