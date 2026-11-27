package com.justreactors.exchanger;

import com.justreactors.JustReactorsConfig;

/**
 * What one Heat Exchanger operation does, for recipe viewers: Hot Coolant + Water -> Coolant + Steam.
 */
public record ExchangerConversion(int hotCoolantMb, int waterMb, int coolantMb, int steamMb, int perTubeMb) {
    public static ExchangerConversion current() {
        int k = HeatExchangerControllerBlockEntity.steamPerHotCoolant();
        return new ExchangerConversion(1_000, 1_000 * k, 1_000, 1_000 * k,
                JustReactorsConfig.get(JustReactorsConfig.EXCHANGER_HOT_COOLANT_PER_TUBE));
    }
}
