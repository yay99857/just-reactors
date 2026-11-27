package com.justreactors.menu;

import net.minecraft.world.inventory.DataSlot;

import java.util.function.IntSupplier;

/**
 * Splits a 32-bit integer into two 16-bit vanilla DataSlots so energy and fuel values > 32,767 sync accurately.
 */
public final class SyncedInt {
    private int value;
    private final DataSlot low;
    private final DataSlot high;

    public SyncedInt(IntSupplier supplier) {
        this.low = new DataSlot() {
            @Override
            public int get() {
                return supplier.getAsInt() & 0xFFFF;
            }

            @Override
            public void set(int v) {
                value = (value & 0xFFFF0000) | (v & 0xFFFF);
            }
        };
        this.high = new DataSlot() {
            @Override
            public int get() {
                return (supplier.getAsInt() >>> 16) & 0xFFFF;
            }

            @Override
            public void set(int v) {
                value = (value & 0x0000FFFF) | ((v & 0xFFFF) << 16);
            }
        };
    }

    public DataSlot low() {
        return low;
    }

    public DataSlot high() {
        return high;
    }

    public int get() {
        return value;
    }
}
