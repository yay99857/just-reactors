package com.justreactors.compat.rei;

import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

/** DEV ONLY: lets the screenshot director open a REI category without referencing REI itself. */
public final class ReiShots {
    private ReiShots() {
    }

    public static void open(String path) {
        ViewSearchBuilder.builder().addCategory(CategoryIdentifier.of(com.justreactors.JustReactors.id(path))).open();
    }
}
