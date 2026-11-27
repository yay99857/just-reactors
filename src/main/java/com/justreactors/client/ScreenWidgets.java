package com.justreactors.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Shared dark-industrial beveled panel and instrumentation gauge drawing helpers.
 */
public final class ScreenWidgets {
    private ScreenWidgets() {
    }

    public static void drawPanel(GuiGraphics g, int left, int top, int width, int height) {
        // Outer dark border
        g.fill(left, top, left + width, top + height, 0xFF0F1217);
        // 3D metallic bevel (light top/left, dark bottom/right)
        g.fill(left + 1, top + 1, left + width - 1, top + height - 1, 0xFF1A1E26);
        g.fill(left + 1, top + 1, left + width - 2, top + 2, 0xFF4D5769);
        g.fill(left + 1, top + 1, left + 2, top + height - 2, 0xFF4D5769);
        // Inner body
        g.fill(left + 2, top + 2, left + width - 2, top + height - 2, 0xFF272C36);
        // Header band + divider
        g.fill(left + 2, top + 2, left + width - 2, top + 18, 0xFF1B1F27);
        g.fill(left + 2, top + 18, left + width - 2, top + 19, 0xFF12151B);
        g.fill(left + 2, top + 19, left + width - 2, top + 20, 0xFF394150);
    }

    public static void drawVerticalGauge(GuiGraphics g, int x, int y, int w, int h, double frac, int fill) {
        drawGaugeFrame(g, x, y, w, h);
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, frac)) * h);
        if (filled > 0) {
            g.fill(x, y + h - filled, x + w, y + h, fill);
            // Subtle top highlight on liquid/energy surface
            g.fill(x, y + h - filled, x + w, y + h - filled + 1, 0x40FFFFFF);
        }
        drawGaugeTicks(g, x, y, w, h);
    }

    public static void drawGaugeFrame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF101318);
        g.fill(x, y, x + w, y + h, 0xFF14181F);
    }

    public static void drawGaugeTicks(GuiGraphics g, int x, int y, int w, int h) {
        for (int q = 1; q <= 3; q++) {
            int ty = y + (h * q) / 4;
            g.fill(x, ty, x + Math.min(5, w / 2), ty + 1, 0x559AA5B1);
        }
    }

    public static void drawHorizontalBar(GuiGraphics g, int x, int y, int w, int h, double frac, int fill) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF101318);
        g.fill(x, y, x + w, y + h, 0xFF14181F);
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, frac)) * w);
        if (filled > 0) {
            g.fill(x, y, x + filled, y + h, fill);
        }
    }

    public static void drawSlotFrame(GuiGraphics g, int x, int y, int borderColor) {
        g.fill(x, y, x + 18, y + 18, borderColor);
        g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF14181F);
    }
}
