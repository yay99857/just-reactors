package com.justreactors.compat.emi;

import dev.emi.emi.api.EmiApi;

/** DEV ONLY: lets the screenshot director open an EMI category without referencing EMI itself. */
public final class EmiShots {
    private EmiShots() {
    }

    public static void open(String path) {
        switch (path) {
            case "heat_exchanging" -> EmiApi.displayRecipeCategory(JustReactorsEmiPlugin.EXCHANGER);
            case "moderators" -> EmiApi.displayRecipeCategory(JustReactorsEmiPlugin.MODERATORS);
            default -> EmiApi.displayRecipeCategory(JustReactorsEmiPlugin.REPROCESSING);
        }
    }
}
