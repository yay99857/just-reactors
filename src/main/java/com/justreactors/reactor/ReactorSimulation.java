package com.justreactors.reactor;

/**
 * Pure-Java Extreme Reactors 2-inspired fission simulation (Passive FE and Active Steam).
 * Decoupled from Minecraft/NeoForge APIs so it can be unit-tested and backported cleanly.
 *
 * No explosions or meltdowns: high temperatures smoothly reduce fuel efficiency (classic ER2 curve),
 * rewarding good moderator layouts and control rod tuning.
 */
public final class ReactorSimulation {
    public static final double AMBIENT_TEMP = 20.0;
    public static final double BOILING_TEMP = 100.0;
    public static final int MB_PER_INGOT = 1_000;

    // Tuned so a 3x3x3 starter reactor (1 fuel rod, air) produces ~250-350 FE/t,
    // a 5x5x5 with 4-5 rods & graphite/water produces ~1,500-4,000 FE/t,
    // and large diamond/emerald/blue-ice/netherite reactors scale smoothly into 10k-100k+ FE/t.
    private static final double BASE_FUEL_BURN_PER_ROD_MB = 0.007;
    private static final double RADIATION_HEAT_PER_MB = 3_600.0;
    private static final double FE_PER_CASING_HEAT_UNIT = 18.0;
    private static final double PASSIVE_HEAT_TRANSFER_RATIO = 0.04;
    private static final double STEAM_HEAT_PER_MB = 0.04;
    private static final double AMBIENT_LOSS_FACTOR = 0.0015;

    private ReactorMode mode = ReactorMode.PASSIVE;
    private boolean active = false;
    private double fuelMb = 0.0;
    private double wasteMb = 0.0;
    private double fuelHeat = AMBIENT_TEMP;
    private double casingHeat = AMBIENT_TEMP;
    private double fuelFertility = 1.0;

    private int lastGeneratedFe = 0;
    private int lastSteamProducedMb = 0;
    private double lastFuelBurnedMb = 0.0;

    public record TickOutput(int generatedFe, int steamProducedMb, double fuelBurnedMb) {
    }

    public TickOutput tick(
            ReactorStructure structure,
            double averageControlRodInsertionPercent,
            int maxEnergyReceivable,
            int availableWaterMb,
            int maxSteamReceivableMb,
            double powerMultiplier,
            double steamMultiplier,
            double fuelUsageMultiplier
    ) {
        double insertion = Math.max(0.0, Math.min(100.0, averageControlRodInsertionPercent)) / 100.0;
        double activeFactor = 1.0 - insertion;

        double fuelBurned = 0.0;
        double fuelHeatAdded = 0.0;
        double casingHeatAdded = 0.0;

        if (active && fuelMb > 0.0001 && activeFactor > 0.0 && structure.fuelRodCount() > 0) {
            // Fuel richness: waste dilutes fuel slightly, encouraging waste ejection.
            double totalReactant = fuelMb + wasteMb;
            double fuelRichness = totalReactant > 0.0 ? (fuelMb / totalReactant) : 0.0;

            // Classic ER2 temperature penalty: above ~1000°C fuel burn rises faster than heat output.
            double tempPenalty = 1.0 + Math.max(0.0, (fuelHeat - 1000.0) / 1000.0) * 0.5;

            // Raw fuel consumed this tick across all rods
            double rawBurn = structure.fuelRodCount()
                    * BASE_FUEL_BURN_PER_ROD_MB
                    * activeFactor
                    * fuelFertility
                    * tempPenalty
                    * fuelUsageMultiplier;

            fuelBurned = Math.min(fuelMb, rawBurn);
            fuelMb -= fuelBurned;
            wasteMb += fuelBurned;

            // Radiation & heat generated from fission
            double rawRadiation = fuelBurned * RADIATION_HEAT_PER_MB * (0.5 + 0.5 * fuelRichness);
            double moderatedHeat = rawRadiation * structure.averageAbsorption() * structure.averageHeatEfficiency();
            double directCoreHeat = rawRadiation * (1.0 - structure.averageAbsorption() * 0.5);

            fuelHeatAdded = directCoreHeat / Math.max(1.0, structure.fuelRodCount() * 4.0);
            casingHeatAdded = (moderatedHeat * structure.averageModeration()) / Math.max(1.0, structure.interiorVolume() * 4.0);

            // Fertility trends smoothly toward target based on rod proximity and moderation
            double targetFertility = Math.min(4.0, structure.rodFertilityFactor() * (0.7 + 0.3 * structure.averageModeration()));
            fuelFertility += (targetFertility - fuelFertility) * 0.02;
        } else {
            // Fertility decays slowly toward 1.0 when idle
            fuelFertility += (1.0 - fuelFertility) * 0.01;
        }

        fuelHeat += fuelHeatAdded;
        casingHeat += casingHeatAdded;

        // Heat transfer: Fuel -> Casing
        double heatDelta = fuelHeat - casingHeat;
        double transfer = heatDelta * Math.min(0.45, Math.max(0.08, structure.fuelToCasingConductivity() * 0.30));
        fuelHeat -= transfer;
        casingHeat += transfer;

        // Heat extraction: Casing -> FE (Passive) OR Water -> Steam (Active)
        int generatedFe = 0;
        int steamProducedMb = 0;

        if (mode == ReactorMode.PASSIVE) {
            if (casingHeat > AMBIENT_TEMP) {
                double excessHeat = casingHeat - AMBIENT_TEMP;
                double heatToConvert = excessHeat * PASSIVE_HEAT_TRANSFER_RATIO;
                double potentialFe = heatToConvert
                        * FE_PER_CASING_HEAT_UNIT
                        * Math.sqrt(Math.max(1.0, structure.fuelRodCount()))
                        * powerMultiplier;

                int feToProduce = (int) Math.min(Integer.MAX_VALUE, Math.floor(potentialFe));
                if (maxEnergyReceivable > 0 && feToProduce > 0) {
                    generatedFe = Math.min(feToProduce, maxEnergyReceivable);
                    double usedFraction = (double) generatedFe / feToProduce;
                    casingHeat -= heatToConvert * usedFraction;
                }
            }
        } else if (mode == ReactorMode.ACTIVE) {
            if (casingHeat > BOILING_TEMP && availableWaterMb > 0 && maxSteamReceivableMb > 0) {
                double boilableExcess = casingHeat - BOILING_TEMP;
                double potentialSteam = boilableExcess
                        * 0.12
                        * Math.sqrt(Math.max(1.0, structure.fuelRodCount()))
                        * steamMultiplier
                        * 10.0;
                int desiredSteam = (int) Math.min(Integer.MAX_VALUE, Math.floor(potentialSteam));
                steamProducedMb = Math.min(desiredSteam, Math.min(availableWaterMb, maxSteamReceivableMb));
                if (steamProducedMb > 0) {
                    double heatUsed = Math.min(boilableExcess, steamProducedMb * STEAM_HEAT_PER_MB);
                    casingHeat -= heatUsed;
                }
            }
        }

        // Passive ambient cooling from reactor surface area
        if (casingHeat > AMBIENT_TEMP) {
            double cooling = (casingHeat - AMBIENT_TEMP) * AMBIENT_LOSS_FACTOR * (1.0 + structure.casingSurfaceArea() * 0.002);
            casingHeat = Math.max(AMBIENT_TEMP, casingHeat - cooling);
        }
        if (fuelHeat > AMBIENT_TEMP && !active) {
            fuelHeat = Math.max(AMBIENT_TEMP, fuelHeat - (fuelHeat - AMBIENT_TEMP) * 0.002);
        }

        // Clamp temperatures to sane bounds (no meltdown)
        fuelHeat = Math.max(AMBIENT_TEMP, Math.min(5000.0, fuelHeat));
        casingHeat = Math.max(AMBIENT_TEMP, Math.min(3500.0, casingHeat));

        this.lastGeneratedFe = generatedFe;
        this.lastSteamProducedMb = steamProducedMb;
        this.lastFuelBurnedMb = fuelBurned;
        return new TickOutput(generatedFe, steamProducedMb, fuelBurned);
    }

    public int getTotalFuelCapacityMb(ReactorStructure structure, int perRodMb) {
        return Math.max(MB_PER_INGOT, structure.fuelRodCount() * perRodMb);
    }

    public double insertFuelMb(double amountMb, int totalCapacityMb) {
        double space = Math.max(0.0, totalCapacityMb - (fuelMb + wasteMb));
        double inserted = Math.min(space, Math.max(0.0, amountMb));
        fuelMb += inserted;
        return inserted;
    }

    /** Extracts whole waste ingots (1,000 mB each) up to maxIngots. */
    public int extractWasteIngots(int maxIngots) {
        int available = (int) Math.floor(wasteMb / MB_PER_INGOT);
        int taken = Math.min(available, Math.max(0, maxIngots));
        wasteMb -= taken * (double) MB_PER_INGOT;
        if (wasteMb < 0.0) wasteMb = 0.0;
        return taken;
    }

    public ReactorMode getMode() {
        return mode;
    }

    public void setMode(ReactorMode mode) {
        this.mode = mode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getFuelMb() {
        return fuelMb;
    }

    public void setFuelMb(double fuelMb) {
        this.fuelMb = Math.max(0.0, fuelMb);
    }

    public double getWasteMb() {
        return wasteMb;
    }

    public void setWasteMb(double wasteMb) {
        this.wasteMb = Math.max(0.0, wasteMb);
    }

    public double getFuelHeat() {
        return fuelHeat;
    }

    public void setFuelHeat(double fuelHeat) {
        this.fuelHeat = Math.max(AMBIENT_TEMP, fuelHeat);
    }

    public double getCasingHeat() {
        return casingHeat;
    }

    public void setCasingHeat(double casingHeat) {
        this.casingHeat = Math.max(AMBIENT_TEMP, casingHeat);
    }

    public double getFuelFertility() {
        return fuelFertility;
    }

    public void setFuelFertility(double fuelFertility) {
        this.fuelFertility = Math.max(1.0, fuelFertility);
    }

    public int getLastGeneratedFe() {
        return lastGeneratedFe;
    }

    public int getLastSteamProducedMb() {
        return lastSteamProducedMb;
    }

    public double getLastFuelBurnedMb() {
        return lastFuelBurnedMb;
    }
}
