package com.justreactors.transfer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Passive FE buffer owned by a ReactorControllerBlockEntity.
 */
public class ReactorEnergyStorage extends SimpleEnergyHandler {
    @Nullable
    private final Runnable onChanged;

    public ReactorEnergyStorage(int capacity, int maxExtract, @Nullable Runnable onChanged) {
        this(capacity, 0, maxExtract, onChanged);
    }

    public ReactorEnergyStorage(int capacity, int maxReceive, int maxExtract, @Nullable Runnable onChanged) {
        super(capacity, maxReceive, maxExtract, 0);
        this.onChanged = onChanged;
    }

    public boolean consumeEnergy(int amount) {
        if (amount <= 0) return true;
        if (energy < amount) return false;
        energy -= amount;
        if (onChanged != null) onChanged.run();
        return true;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        if (onChanged != null) onChanged.run();
    }

    public int getEnergyStored() {
        return energy;
    }

    public int getMaxEnergyStored() {
        return capacity;
    }

    public int getSpace() {
        return capacity - energy;
    }

    public int generateEnergy(int amount) {
        int added = Math.max(0, Math.min(capacity - energy, amount));
        if (added > 0) {
            energy += added;
            if (onChanged != null) onChanged.run();
        }
        return added;
    }

    public int extractInternal(int amount) {
        int taken = Math.max(0, Math.min(energy, amount));
        if (taken > 0) {
            energy -= taken;
            if (onChanged != null) onChanged.run();
        }
        return taken;
    }

    @SuppressWarnings("deprecation")
    public static int pushEnergy(@Nullable EnergyHandler target, int amount) {
        if (target == null || amount <= 0) return 0;
        try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
            int inserted = target.insert(amount, tx);
            if (inserted > 0) tx.commit();
            return inserted;
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("Energy", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        energy = Math.max(0, Math.min(input.getIntOr("Energy", 0), capacity));
    }
}
