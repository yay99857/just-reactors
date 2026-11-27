package com.justreactors.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Procedural look shared by every Just Reactors screen, drawn like pixel art: flat colours,
 * 1px bevels (light top-left, dark bottom-right), recessed bays and stepped gauge fills.
 * Everything is drawn with fills, so there are no GUI textures to keep in sync.
 */
public final class ScreenWidgets {
    // Palette
    public static final int TEXT = 0xFFE8ECF2;
    public static final int TEXT_DIM = 0xFFA3AEBB;
    public static final int TEXT_MUTED = 0xFF6E7987;
    public static final int VALUE = 0xFF8ED6DE;
    public static final int TITLE_GOLD = 0xFFFFD866;

    public static final int ACCENT_REACTOR = 0xFFF2C94C;
    public static final int ACCENT_TURBINE = 0xFF56CCF2;
    public static final int ACCENT_REDSTONE = 0xFFE5484D;
    public static final int ACCENT_REPROCESSOR = 0xFF9B6CF2;
    public static final int ACCENT_EXCHANGER = 0xFFF08A3C;
    public static final int ACCENT_BATTERY = 0xFF7AD36B;

    public static final int COOLANT = 0xFF4FD6E0;
    public static final int HOT_COOLANT = 0xFFEF7A35;

    public static final int FUEL = 0xFFE6CF3A;
    public static final int WASTE = 0xFF2BB6C4;
    public static final int WATER = 0xFF2F7FE6;
    public static final int STEAM = 0xFFDCE4EC;
    public static final int ENERGY = 0xFF4CC26E;
    public static final int ON = 0xFF4CC26E;
    public static final int OFF = 0xFFE5484D;

    private static final int BORDER = 0xFF0A0C10;
    private static final int PANEL = 0xFF3A414D;
    private static final int PANEL_LIGHT = 0xFF5B6473;
    private static final int PANEL_DARK = 0xFF22272F;
    private static final int BAY = 0xFF181C23;

    private ScreenWidgets() {
    }

    // ---------------------------------------------------------------- colours

    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = a >>> 24, ar = a >> 16 & 0xFF, ag = a >> 8 & 0xFF, ab = a & 0xFF;
        int ba = b >>> 24, br = b >> 16 & 0xFF, bg = b >> 8 & 0xFF, bb = b & 0xFF;
        return Math.round(aa + (ba - aa) * t) << 24 | Math.round(ar + (br - ar) * t) << 16
                | Math.round(ag + (bg - ag) * t) << 8 | Math.round(ab + (bb - ab) * t);
    }

    public static int lighter(int c, float t) {
        return mix(c, 0xFFFFFFFF, t);
    }

    public static int darker(int c, float t) {
        return mix(c, 0xFF000000, t);
    }

    public static int withAlpha(int c, int alpha) {
        return (alpha & 0xFF) << 24 | (c & 0xFFFFFF);
    }

    private static final int[] HEAT_STOPS = {0xFF2E5BDB, 0xFF27B5D9, 0xFF3FD18A, 0xFFF0D23C, 0xFFF0862E, 0xFFE0352F};

    /** Thermal colour map: 0 = cold blue, 1 = white-hot red. */
    public static int heatColor(double t) {
        t = Math.max(0.0, Math.min(1.0, t)) * (HEAT_STOPS.length - 1);
        int i = Math.min(HEAT_STOPS.length - 2, (int) t);
        return mix(HEAT_STOPS[i], HEAT_STOPS[i + 1], (float) (t - i));
    }

    private static int px(double frac, int size) {
        if (Double.isNaN(frac)) return 0;
        return (int) Math.round(Math.max(0.0, Math.min(1.0, frac)) * size);
    }

    // ---------------------------------------------------------------- panels

    /** Main window: outline with rounded corners, flat bevelled body and an accent line under the title. */
    public static void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h, int accent) {
        g.fill(x + 1, y, x + w - 1, y + h, BORDER);
        g.fill(x, y + 1, x + w, y + h - 1, BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL);
        g.fill(x + 1, y + 1, x + w - 2, y + 2, PANEL_LIGHT);
        g.fill(x + 1, y + 2, x + 2, y + h - 2, PANEL_LIGHT);
        g.fill(x + 2, y + h - 2, x + w - 1, y + h - 1, PANEL_DARK);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 2, PANEL_DARK);
        // title strip: engraved groove with a solid accent line
        g.fill(x + 4, y + 18, x + w - 4, y + 19, PANEL_DARK);
        g.fill(x + 4, y + 19, x + w - 4, y + 20, mix(accent, PANEL, 0.25f));
        g.fill(x + 4, y + 20, x + w - 4, y + 21, PANEL_LIGHT);

        rivet(g, x + 4, y + h - 7);
        rivet(g, x + w - 7, y + h - 7);
    }

    public static void rivet(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x, y, x + 3, y + 3, PANEL_DARK);
        g.fill(x, y, x + 2, y + 2, 0xFF7D8797);
    }

    /** Title text with the window's status LED on the left. */
    public static void drawTitle(GuiGraphicsExtractor g, Font font, Component title, int x, int y, int ledColor, boolean ledOn) {
        led(g, x + 7, y + 7, ledColor, ledOn);
        g.text(font, title, x + 17, y + 7, TEXT, true);
    }

    /** Small framed badge (e.g. mode) drawn right-aligned at {@code right}. Returns its left x. */
    public static int badge(GuiGraphicsExtractor g, Font font, Component text, int right, int y, int color) {
        int w = font.width(text) + 7;
        int x = right - w;
        g.fill(x, y, x + w, y + 11, darker(color, 0.45f));
        g.fill(x + 1, y + 1, x + w - 1, y + 10, BAY);
        g.text(font, text, x + 4, y + 2, color, false);
        return x;
    }

    /** Recessed dark area (an LCD/instrument bay). Coordinates are the inner area. */
    public static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w, y, PANEL_DARK);
        g.fill(x - 1, y, x, y + h, PANEL_DARK);
        g.fill(x, y + h, x + w + 1, y + h + 1, PANEL_LIGHT);
        g.fill(x + w, y - 1, x + w + 1, y + h, PANEL_LIGHT);
        g.fill(x, y, x + w, y + h, BAY);
    }

    /** Thin engraved separator. */
    public static void vDivider(GuiGraphicsExtractor g, int x, int y, int h) {
        g.fill(x, y, x + 1, y + h, PANEL_DARK);
        g.fill(x + 1, y, x + 2, y + h, PANEL_LIGHT);
    }

    public static void hDivider(GuiGraphicsExtractor g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, PANEL_DARK);
        g.fill(x, y + 1, x + w, y + 2, PANEL_LIGHT);
    }

    // ---------------------------------------------------------------- gauges

    private static void gaugeBack(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        inset(g, x, y, w, h);
        g.fill(x, y, x + w, y + h, 0xFF101318);
    }

    private static void gaugeGlass(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        // scale marks on the right
        for (int q = 1; q < 8; q++) {
            int ty = y + h - (h * q) / 8;
            int len = q % 2 == 0 ? 3 : 2;
            g.fill(x + w - len, ty, x + w, ty + 1, q % 2 == 0 ? 0xA0C3CAD4 : 0x60C3CAD4);
        }
    }

    /** Liquid column: flat colour with a pixel highlight and a shaded column. */
    private static void liquid(GuiGraphicsExtractor g, int x, int top, int w, int f, int color, boolean surface) {
        if (f <= 0) return;
        g.fill(x, top, x + w, top + f, color);
        g.fill(x + 1, top, x + 2, top + f, lighter(color, 0.3f));
        g.fill(x + w - 2, top, x + w, top + f, darker(color, 0.25f));
        if (surface) g.fill(x, top, x + w, top + 1, lighter(color, 0.45f));
    }

    public static void drawVerticalGauge(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac, int color) {
        gaugeBack(g, x, y, w, h);
        int f = px(frac, h);
        liquid(g, x, y + h - f, w, f, color, true);
        gaugeGlass(g, x, y, w, h);
    }

    /** Two liquids stacked in one column (e.g. waste below fuel). */
    public static void drawStackedGauge(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                        double bottomFrac, int bottomColor, double topFrac, int topColor) {
        gaugeBack(g, x, y, w, h);
        int b = px(bottomFrac, h);
        int t = Math.min(h - b, px(topFrac, h));
        liquid(g, x, y + h - b, w, b, bottomColor, t == 0);
        liquid(g, x, y + h - b - t, w, t, topColor, true);
        if (b > 0 && t > 0) g.fill(x, y + h - b, x + w, y + h - b + 1, 0x60000000);
        gaugeGlass(g, x, y, w, h);
    }

    /** Thermometer column painted with the thermal colour map in 4px bands, like a palette ramp. */
    public static void drawHeatGauge(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac) {
        gaugeBack(g, x, y, w, h);
        int f = px(frac, h);
        for (int i = 0; i < f; i++) {
            int c = heatColor((i / 4) * 4 / (double) (h - 1));
            int row = y + h - 1 - i;
            g.fill(x, row, x + w, row + 1, c);
            g.fill(x + 1, row, x + 2, row + 1, lighter(c, 0.3f));
            g.fill(x + w - 2, row, x + w, row + 1, darker(c, 0.25f));
        }
        if (f > 0) {
            g.fill(x, y + h - f, x + w, y + h - f + 1, lighter(heatColor(((f - 1) / 4) * 4 / (double) (h - 1)), 0.45f));
        }
        gaugeGlass(g, x, y, w, h);
    }

    /** ER-style ruler between two gauges. */
    public static void ruler(GuiGraphicsExtractor g, int x, int y, int h) {
        g.fill(x + 2, y, x + 3, y + h, 0xFF5A6474);
        for (int i = 0; i <= 10; i++) {
            int ty = y + (h - 1) * i / 10;
            int len = i % 5 == 0 ? 5 : 3;
            int lx = x + (5 - len) / 2;
            g.fill(lx, ty, lx + len, ty + 1, i % 5 == 0 ? 0xFFA9B3C1 : 0xFF6D7888);
        }
    }

    public static void drawHorizontalBar(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac, int color) {
        inset(g, x, y, w, h);
        int f = px(frac, w);
        if (f > 0) {
            g.fill(x, y, x + f, y + h, color);
            g.fill(x, y, x + f, y + 1, lighter(color, 0.3f));
            g.fill(x, y + h - 1, x + f, y + h, darker(color, 0.3f));
        }
        for (int i = 1; i < 10; i++) {
            int tx = x + w * i / 10;
            g.fill(tx, y + h - (i == 5 ? 3 : 2), tx + 1, y + h, 0x50FFFFFF);
        }
    }

    /** LED segment bar: each lit cell takes the colour of its position on the thermal map. */
    public static void drawSegmentBar(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac, boolean thermal, int color) {
        inset(g, x, y, w, h);
        int cell = 4;
        int cells = (w - 1) / cell;
        int lit = px(frac, cells);
        for (int i = 0; i < cells; i++) {
            int cx = x + 1 + i * cell;
            int c = thermal ? heatColor(i / (double) (cells - 1)) : color;
            if (i < lit) {
                g.fill(cx, y + 1, cx + cell - 1, y + h - 1, c);
                g.fill(cx, y + 1, cx + cell - 1, y + 2, lighter(c, 0.3f));
            } else {
                g.fill(cx, y + 1, cx + cell - 1, y + h - 1, mix(c, BAY, 0.8f));
            }
        }
    }

    /** Small downward marker above a bar, e.g. a target or sweet spot. */
    public static void marker(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x - 2, y, x + 3, y + 1, color);
        g.fill(x - 1, y + 1, x + 2, y + 2, color);
        g.fill(x, y + 2, x + 1, y + 3, color);
    }

    // ---------------------------------------------------------------- LEDs, icons, slots

    public static void led(GuiGraphicsExtractor g, int x, int y, int color, boolean on) {
        int c = on ? color : mix(color, PANEL_DARK, 0.7f);
        g.fill(x, y - 1, x + 5, y + 6, BORDER);
        g.fill(x - 1, y, x + 6, y + 5, BORDER);
        g.fill(x + 1, y, x + 4, y + 5, c);
        g.fill(x, y + 1, x + 5, y + 4, c);
        g.fill(x + 3, y + 3, x + 5, y + 4, darker(c, 0.3f));
        g.fill(x + 1, y + 1, x + 2, y + 2, lighter(c, on ? 0.7f : 0.15f));
    }

    /** Item icon on a faint backplate, used above gauges and in stat rows. */
    public static void icon(GuiGraphicsExtractor g, ItemStack stack, int x, int y) {
        g.item(stack, x, y);
    }

    public static void pixelIcon(GuiGraphicsExtractor g, PixelIcon icon, int x, int y) {
        icon.draw(g, x, y);
    }

    /** An inventory slot; with an accent the slot gets a 1px coloured rim. */
    public static void drawSlotFrame(GuiGraphicsExtractor g, int x, int y, int accent) {
        if (accent != 0) g.outline(x - 1, y - 1, 20, 20, accent);
        // vanilla-style slot: shadow top-left, light bottom-right
        g.fill(x, y, x + 18, y + 18, PANEL_LIGHT);
        g.fill(x, y, x + 17, y + 17, PANEL_DARK);
        g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF262B34);
    }

    /** Player inventory (3x9 + hotbar) at the usual offsets; x/y are the first slot's frame. */
    public static void drawPlayerInventory(GuiGraphicsExtractor g, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(g, x + col * 18, y + row * 18, 0);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(g, x + col * 18, y + 58, 0);
        }
    }

    /** Redstone strength meter: 15 lamp cells. */
    public static void signalBar(GuiGraphicsExtractor g, int x, int y, int signal) {
        inset(g, x, y, 15 * 6 + 1, 9);
        for (int i = 0; i < 15; i++) {
            int cx = x + 1 + i * 6;
            if (i < signal) {
                g.fill(cx, y + 1, cx + 5, y + 8, 0xFFC8362B);
                g.fill(cx, y + 1, cx + 5, y + 2, 0xFFF06A5A);
                g.fill(cx, y + 7, cx + 5, y + 8, 0xFF8A2019);
            } else {
                g.fill(cx, y + 1, cx + 5, y + 8, 0xFF2E1A1A);
            }
        }
    }

    /** Arrow pointing right, used for in -> out flow. */
    public static void arrowRight(GuiGraphicsExtractor g, int x, int y, int w, double frac, int color) {
        int f = px(frac, w);
        for (int i = 0; i < w; i++) {
            int c = i < f ? color : 0xFF2A303A;
            g.fill(x + i, y + 3, x + i + 1, y + 6, c);
        }
        for (int i = 0; i < 5; i++) {
            int c = f >= w ? color : 0xFF2A303A;
            g.fill(x + w + i, y + i - 0, x + w + i + 1, y + 9 - i, c);
        }
    }
}
