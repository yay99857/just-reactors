package com.justreactors.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Builders for the screens' multi-line tooltips: gold title, cyan readings, white details, grey hints. */
public final class Tips {
    private Tips() {
    }

    public static List<Component> tip(String titleKey) {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable(titleKey).withStyle(ChatFormatting.YELLOW));
        return list;
    }

    public static Component value(String key, Object... args) {
        return Component.literal("  ").append(Component.translatable(key, args)).withStyle(ChatFormatting.AQUA);
    }

    public static Component line(String key, Object... args) {
        return Component.translatable(key, args).withStyle(ChatFormatting.WHITE);
    }

    public static Component hint(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.GRAY);
    }

    public static List<Component> fluidTip(String titleKey, int amount, int max) {
        List<Component> tip = tip(titleKey);
        tip.add(value("gui.justreactors.tip.percent", pct((double) amount / Math.max(1, max))));
        tip.add(line("gui.justreactors.tip.fluid", fmt(amount), fmt(max)));
        return tip;
    }

    public static String pct(double frac) {
        return String.format(Locale.ROOT, "%.2f", Math.max(0, Math.min(1, frac)) * 100.0);
    }

    public static String fmt(int n) {
        return String.format(Locale.ROOT, "%,d", n);
    }

    /** Shows the tooltip, wrapping long lines (hints) to a comfortable width. */
    public static void show(GuiGraphicsExtractor g, Font font, List<Component> lines, int x, int y) {
        List<FormattedCharSequence> out = new ArrayList<>();
        for (Component c : lines) {
            if (c.getString().isEmpty()) {
                out.add(FormattedCharSequence.EMPTY);
            } else {
                out.addAll(font.split(c, 190));
            }
        }
        g.setTooltipForNextFrame(font, out, x, y);
    }
}
