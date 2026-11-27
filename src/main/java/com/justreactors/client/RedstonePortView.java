package com.justreactors.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.justreactors.client.ScreenWidgets.*;

/** Shared layout for the reactor and turbine Redstone Port screens. */
public final class RedstonePortView {
    private static final ItemStack DUST = new ItemStack(Items.REDSTONE);
    private static final ItemStack LEVER = new ItemStack(Items.LEVER);
    private static final ItemStack COMPARATOR = new ItemStack(Items.COMPARATOR);

    private RedstonePortView() {
    }

    public static void background(GuiGraphics g, int x, int y, int w, boolean output, int signal, int thresholdPercent) {
        drawPanel(g, x, y, w, 136, ACCENT_REDSTONE);
        // mode selector LCD
        inset(g, x + 32, y + 29, w - 64, 18);
        // readings bay
        inset(g, x + 8, y + 56, w - 16, 44);
        icon(g, DUST, x + 11, y + 58);
        signalBar(g, x + w - 103, y + 61, signal);
        hDivider(g, x + 12, y + 77, w - 24);
        icon(g, output ? COMPARATOR : LEVER, x + 11, y + 81);
        if (output) {
            drawHorizontalBar(g, x + 30, y + 91, w - 42, 5, thresholdPercent / 100.0, ACCENT_REDSTONE);
        }
    }

    public static void labels(GuiGraphics g, Font font, Component title, int w, boolean output,
                              Component modeName, int signal, Component threshold) {
        drawTitle(g, font, title, 0, 0, ACCENT_REDSTONE, signal > 0);
        badge(g, font, Component.translatable(output ? "gui.justreactors.redstone_port.output" : "gui.justreactors.redstone_port.input"),
                w - 6, 5, output ? 0xFFFF8A65 : ACCENT_TURBINE);

        int mx = (w - font.width(modeName)) / 2;
        g.drawString(font, modeName, mx, 34, output ? 0xFFFFB199 : 0xFF9FDCF5, true);

        g.drawString(font, Component.translatable("gui.justreactors.label.signal"), 30, 62, TEXT_DIM, false);
        String sig = signal + "/15";
        g.drawString(font, sig, w - 108 - font.width(sig), 62, VALUE, false);

        if (threshold != null) {
            g.drawString(font, threshold, 30, 81, TEXT, false);
        } else {
            g.drawString(font, Component.translatable("gui.justreactors.redstone_port.input_hint"), 30, 85, TEXT_DIM, false);
        }
    }
}
