package com.justreactors.transfer;

import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Passive FE buffer owned by a ReactorControllerBlockEntity.
 */
public class ReactorEnergyStorage extends EnergyStorage {
    @Nullable
    private final Runnable onChanged;

    public ReactorEnergyStorage(int capacity, int maxExtract, @Nullable Runnable onChanged) {
        this(capacity, 0, maxExtract, onChanged);
    }

    public ReactorEnergyStorage(int capacity, int maxReceive, int maxExtract, @Nullable Runnable onChanged) {
        super(capacity, maxReceive, maxExtract, 0);
        this.onChanged = onChanged;
    }

    private void changed() {
        if (onChanged != null) onChanged.run();
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = super.receiveEnergy(toReceive, simulate);
        if (received > 0 && !simulate) changed();
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int extracted = super.extractEnergy(toExtract, simulate);
        if (extracted > 0 && !simulate) changed();
        return extracted;
    }

    public boolean consumeEnergy(int amount) {
        if (amount <= 0) return true;
        if (energy < amount) return false;
        energy -= amount;
        changed();
        return true;
    }

    public int getSpace() {
        return capacity - energy;
    }

    public int generateEnergy(int amount) {
        int added = Math.max(0, Math.min(capacity - energy, amount));
        if (added > 0) {
            energy += added;
            changed();
        }
        return added;
    }

    public int extractInternal(int amount) {
        int taken = Math.max(0, Math.min(energy, amount));
        if (taken > 0) {
            energy -= taken;
            changed();
        }
        return taken;
    }

    public static int pushEnergy(@Nullable IEnergyStorage target, int amount) {
        if (target == null || amount <= 0 || !target.canReceive()) return 0;
        return target.receiveEnergy(amount, false);
    }

    public void serialize(ValueOutput output) {
        output.putInt("Energy", energy);
    }

    public void deserialize(ValueInput input) {
        energy = Math.max(0, Math.min(input.getIntOr("Energy", 0), capacity));
    }
}
