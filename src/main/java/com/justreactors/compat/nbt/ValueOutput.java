package com.justreactors.compat.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

/**
 * 1.20.1 shim for 26.x's {@code com.justreactors.compat.nbt.ValueOutput}: lets the block entities keep
 * the same save code as the 26.3 branch while writing into a plain {@link CompoundTag}.
 */
public final class ValueOutput {
    private final CompoundTag tag;

    public ValueOutput(CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag tag() {
        return tag;
    }


    public void putInt(String key, int value) {
        tag.putInt(key, value);
    }

    public void putLong(String key, long value) {
        tag.putLong(key, value);
    }

    public void putDouble(String key, double value) {
        tag.putDouble(key, value);
    }

    public void putBoolean(String key, boolean value) {
        tag.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        tag.putString(key, value);
    }

    public void putIntArray(String key, int[] value) {
        tag.putIntArray(key, value);
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value)
                .result().ifPresent(encoded -> tag.put(key, encoded));
    }

    public ValueOutput child(String key) {
        CompoundTag child = new CompoundTag();
        tag.put(key, child);
        return new ValueOutput(child);
    }
}
