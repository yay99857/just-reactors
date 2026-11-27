package com.justreactors.battery;

import com.justreactors.transfer.ReactorEnergyStorage;

import org.jetbrains.annotations.Nullable;

/** Battery buffer whose capacity follows the number of cells. Energy above a reduced capacity is lost. */
public class BatteryEnergyStorage extends ReactorEnergyStorage {
    public BatteryEnergyStorage(int capacity, @Nullable Runnable onChanged) {
        super(capacity, Integer.MAX_VALUE, Integer.MAX_VALUE, onChanged);
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
        if (energy > this.capacity) energy = this.capacity;
    }

    /** Sets the stored energy directly (used when loading before the structure is validated). */
    public void setEnergyUnchecked(int energy) {
        this.energy = Math.max(0, energy);
    }
}
