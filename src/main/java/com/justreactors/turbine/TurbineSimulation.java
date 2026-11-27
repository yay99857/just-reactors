package com.justreactors.turbine;

/**
 * Pure-Java Extreme Reactors 2-inspired Steam Turbine simulation.
 * Steam imparts angular energy onto the rotor shaft; engaged coils convert kinetic energy into FE
 * with an RPM efficiency curve peaking around 900 RPM and 1800 RPM.
 */
public final class TurbineSimulation {
    public static final double MAX_RPM = 3000.0;
    private static final double STEAM_LIFT_PER_MB = 12.5;
    private static final double BASE_INDUCTION_PER_DRAG_RPM = 0.45;
    private static final double FE_PER_INDUCTION_UNIT = 2.0;

    private boolean active = false;
    private boolean inductorEngaged = true;
    private int maxSteamFlowMb = 2_000;
    private double rotorEnergy = 0.0;
    private double rpm = 0.0;

    private int lastGeneratedFe = 0;
    private int lastSteamConsumedMb = 0;

    public record TickOutput(int generatedFe, int steamConsumedMb, int waterCondensedMb) {
    }

    public TickOutput tick(
            TurbineStructure structure,
            int availableSteamMb,
            int energySpaceFe,
            double powerMultiplier,
            int configMaxFlowMb
    ) {
        double massFactor = Math.max(10.0, structure.rotorMass());
        double maxRotorEnergy = MAX_RPM * massFactor * 1.5;
        // Clamp existing energy so over-capped saves self-heal immediately.
        rotorEnergy = Math.min(rotorEnergy, maxRotorEnergy);

        int effectiveMaxFlow = Math.min(maxSteamFlowMb, Math.min(configMaxFlowMb, Math.max(25, structure.bladeCount() * 250)));
        int steamConsumed = 0;

        if (active && availableSteamMb > 0 && effectiveMaxFlow > 0) {
            steamConsumed = Math.min(availableSteamMb, effectiveMaxFlow);
            // Blade capture factor: having enough blades for the steam flow maximizes lift
            double bladeCoverage = Math.min(1.0, (structure.bladeCount() * 250.0) / Math.max(1, steamConsumed));
            rotorEnergy = Math.min(maxRotorEnergy,
                    rotorEnergy + steamConsumed * STEAM_LIFT_PER_MB * (0.4 + 0.6 * bladeCoverage));
        }

        // Compute current RPM before drag/induction
        rpm = Math.min(MAX_RPM, rotorEnergy / (massFactor * 1.5));

        // Realistic aerodynamic drag (linear + quadratic high-RPM resistance) & bearing friction
        double aeroDrag = rpm * structure.bladeCount() * 0.06 + 0.00025 * rpm * rpm;
        double bearingFriction = rpm * 0.25 + (rpm > 0.0 ? 8.0 : 0.0);
        rotorEnergy = Math.max(0.0, rotorEnergy - aeroDrag - bearingFriction);

        // Electromagnetic induction (coils -> FE)
        int generatedFe = 0;
        if (inductorEngaged && structure.coilCount() > 0 && rpm > 1.0 && energySpaceFe > 0) {
            double rpmEff = rpmEfficiency(rpm);
            double energyExtracted = Math.min(rotorEnergy, rpm * structure.coilDrag() * BASE_INDUCTION_PER_DRAG_RPM);
            if (energyExtracted > 0.0) {
                double potentialFe = energyExtracted
                        * FE_PER_INDUCTION_UNIT
                        * structure.coilEfficiency()
                        * structure.coilBonus()
                        * rpmEff
                        * powerMultiplier;
                int feToProduce = (int) Math.min(Integer.MAX_VALUE, Math.floor(potentialFe));
                if (feToProduce > 0) {
                    generatedFe = Math.min(feToProduce, energySpaceFe);
                    double usedFraction = (double) generatedFe / feToProduce;
                    rotorEnergy = Math.max(0.0, rotorEnergy - energyExtracted * usedFraction);
                }
            }
        }

        rpm = Math.min(MAX_RPM, rotorEnergy / (massFactor * 1.5));
        if (!active && rpm < 0.5) {
            rpm = 0.0;
            rotorEnergy = 0.0;
        }

        this.lastGeneratedFe = generatedFe;
        this.lastSteamConsumedMb = steamConsumed;
        return new TickOutput(generatedFe, steamConsumed, steamConsumed);
    }

    /** Classic ER2 dual-peak efficiency curve: peaks at 900 RPM and 1800 RPM. */
    private static double rpmEfficiency(double currentRpm) {
        if (currentRpm < 100.0) return 0.35 + (currentRpm / 100.0) * 0.25;
        double d900 = Math.abs(currentRpm - 900.0) / 900.0;
        double d1800 = Math.abs(currentRpm - 1800.0) / 900.0;
        double peak = Math.max(1.0 - d900 * 0.45, 1.0 - d1800 * 0.45);
        return Math.max(0.5, Math.min(1.05, peak));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInductorEngaged() {
        return inductorEngaged;
    }

    public void setInductorEngaged(boolean inductorEngaged) {
        this.inductorEngaged = inductorEngaged;
    }

    public int getMaxSteamFlowMb() {
        return maxSteamFlowMb;
    }

    public void setMaxSteamFlowMb(int maxSteamFlowMb) {
        this.maxSteamFlowMb = Math.max(0, maxSteamFlowMb);
    }

    public double getRotorEnergy() {
        return rotorEnergy;
    }

    public void setRotorEnergy(double rotorEnergy) {
        this.rotorEnergy = Math.max(0.0, rotorEnergy);
    }

    public double getRpm() {
        return rpm;
    }

    public int getLastGeneratedFe() {
        return lastGeneratedFe;
    }

    public int getLastSteamConsumedMb() {
        return lastSteamConsumedMb;
    }
}
