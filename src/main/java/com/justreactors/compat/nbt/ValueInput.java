package com.justreactors.compat.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.Optional;

/**
 * 1.20.1 shim for 26.x's {@code com.justreactors.compat.nbt.ValueInput}, reading from a {@link CompoundTag}.
 */
public final class ValueInput {
    private final CompoundTag tag;

    public ValueInput(CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag tag() {
        return tag;
    }


    public int getIntOr(String key, int fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getInt(key) : fallback;
    }

    public long getLongOr(String key, long fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getLong(key) : fallback;
    }

    public Optional<Long> getLong(String key) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? Optional.of(tag.getLong(key)) : Optional.empty();
    }

    public double getDoubleOr(String key, double fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getDouble(key) : fallback;
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_ANY_NUMERIC) ? tag.getBoolean(key) : fallback;
    }

    public String getStringOr(String key, String fallback) {
        return tag.contains(key, Tag.TAG_STRING) ? tag.getString(key) : fallback;
    }

    public Optional<int[]> getIntArray(String key) {
        return tag.contains(key, Tag.TAG_INT_ARRAY) ? Optional.of(tag.getIntArray(key)) : Optional.empty();
    }

    public <T> Optional<T> read(String key, Codec<T> codec) {
        Tag raw = tag.get(key);
        if (raw == null) return Optional.empty();
        return codec.parse(NbtOps.INSTANCE, raw).result();
    }

    public Optional<ValueInput> child(String key) {
        return tag.contains(key, Tag.TAG_COMPOUND)
                ? Optional.of(new ValueInput(tag.getCompound(key)))
                : Optional.empty();
    }
}
