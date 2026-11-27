package com.justreactors.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

/**
 * Flat bevelled push button. With {@link #lit} it behaves like a latching switch: while the supplier
 * is true the face takes a muted accent colour and is drawn pressed in (ON/OFF pairs, selected modes).
 */
public class JrButton extends Button {
    private final int accent;
    private BooleanSupplier lit = () -> false;
    private ItemStack icon = ItemStack.EMPTY;

    public JrButton(int x, int y, int w, int h, Component message, OnPress onPress, int accent) {
        super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
        this.accent = accent;
    }

    public JrButton lit(BooleanSupplier lit) {
        this.lit = lit;
        return this;
    }

    public JrButton icon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean on = lit.getAsBoolean() && active;
        boolean hover = isHoveredOrFocused() && active;

        // flat face with a 1px bevel, like vanilla buttons; a lit button is drawn pressed in
        int face, text;
        if (!active) {
            face = 0xFF2A2F37;
            text = 0xFF5E6672;
        } else if (on) {
            face = ScreenWidgets.mix(accent, 0xFF2A2F37, hover ? 0.3f : 0.42f);
            text = 0xFFFFFFFF;
        } else {
            face = hover ? 0xFF59647A : 0xFF484F5D;
            text = hover ? 0xFFFFFFA0 : ScreenWidgets.TEXT;
        }
        int light = ScreenWidgets.lighter(face, 0.22f), dark = ScreenWidgets.darker(face, 0.45f);
        g.fill(x, y, x + w, y + h, 0xFF0A0C10);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, face);
        if (active) {
            int tl = on ? dark : light, br = on ? light : dark;
            g.fill(x + 1, y + 1, x + w - 1, y + 2, tl);
            g.fill(x + 1, y + 2, x + 2, y + h - 1, tl);
            g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, br);
            g.fill(x + w - 2, y + 1, x + w - 1, y + h - 2, br);
        }

        Font font = Minecraft.getInstance().font;
        Component msg = getMessage();
        int textW = font.width(msg);
        int contentW = textW + (icon.isEmpty() ? 0 : 18);
        int cx = x + (w - contentW) / 2;
        int dy = on ? 1 : 0;
        if (!icon.isEmpty()) {
            g.item(icon, cx, y + (h - 16) / 2 + dy);
            cx += 18;
        }
        g.text(font, msg, cx, y + (h - 8) / 2 + dy, text, active);
    }
}
