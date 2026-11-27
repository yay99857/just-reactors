package com.justreactors.exchanger;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Heat Exchanger Fluid Port role: accepts Hot Coolant and Water, or outputs cooled Coolant, or outputs Steam.
 */
public enum ExchangerPortMode implements StringRepresentable {
    INPUT,
    OUTPUT_COOLANT,
    OUTPUT_STEAM;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ExchangerPortMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public String translationKey() {
        return "message.justreactors.heat_exchanger_port." + getSerializedName();
    }
}
