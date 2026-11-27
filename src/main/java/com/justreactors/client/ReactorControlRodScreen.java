package com.justreactors.client;

import com.justreactors.menu.ReactorControlRodMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.justreactors.client.ScreenWidgets.*;

public class ReactorControlRodScreen extends AbstractContainerScreen<ReactorControlRodMenu> {
    private static final int CORE_X = 12, CORE_Y = 28, CORE_W = 26, CORE_H = 72;

    public ReactorControlRodScreen(ReactorControlRodMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 184;
        this.imageHeight = 110;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;
        int bx = leftPos + 48;
        addRenderableWidget(new JrButton(bx, topPos + 56, 30, 16, Component.literal("-10"), b -> send(ReactorControlRodMenu.BTN_MINUS_10), 0));
        addRenderableWidget(new JrButton(bx + 32, topPos + 56, 30, 16, Component.literal("-1"), b -> send(ReactorControlRodMenu.BTN_MINUS_1), 0));
        addRenderableWidget(new JrButton(bx + 64, topPos + 56, 30, 16, Component.literal("+1"), b -> send(ReactorControlRodMenu.BTN_PLUS_1), 0));
        addRenderableWidget(new JrButton(bx + 96, topPos + 56, 30, 16, Component.literal("+10"), b -> send(ReactorControlRodMenu.BTN_PLUS_10), 0));
        addRenderableWidget(new JrButton(bx, topPos + 80, 126, 18,
                Component.translatable("gui.justreactors.control_rod.apply_all_short"),
                b -> send(ReactorControlRodMenu.BTN_APPLY_ALL), ACCENT_REACTOR));
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_REACTOR);

        // cutaway of the fuel channel with the rod sliding in from the top
        int cx = x + CORE_X, cy = y + CORE_Y;
        inset(g, cx, cy, CORE_W, CORE_H);
        double ins = menu.getInsertion() / 100.0;
        // fuel is brighter the further the rod is pulled out
        int fuel = mix(0xFF5C5410, FUEL, (float) (1.0 - ins) * 0.85f);
        g.fill(cx + 6, cy + 2, cx + CORE_W - 6, cy + CORE_H - 2, fuel);
        g.fill(cx + 7, cy + 2, cx + 8, cy + CORE_H - 2, lighter(fuel, 0.3f));
        g.fill(cx + CORE_W - 8, cy + 2, cx + CORE_W - 6, cy + CORE_H - 2, darker(fuel, 0.3f));
        int rodH = (int) Math.round(ins * (CORE_H - 4));
        if (rodH > 0) {
            g.fill(cx + 9, cy + 2, cx + CORE_W - 9, cy + 2 + rodH, 0xFF7C8796);
            g.fill(cx + 10, cy + 2, cx + 11, cy + 2 + rodH, 0xFFA9B3C1);
            g.fill(cx + CORE_W - 10, cy + 2, cx + CORE_W - 9, cy + 2 + rodH, 0xFF4E5766);
            g.fill(cx + 9, cy + 1 + rodH, cx + CORE_W - 9, cy + 2 + rodH, 0xFF2A3038);
        }
        // rod head
        g.fill(cx + 5, cy - 1, cx + CORE_W - 5, cy + 2, 0xFF6B7585);
        g.fill(cx + 5, cy - 1, cx + CORE_W - 5, cy, 0xFF8E98A8);

        inset(g, x + 48, y + 26, 126, 24);
        drawHorizontalBar(g, x + 52, y + 41, 118, 5, ins, 0xFF8FA3BF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        drawTitle(g, font, title, 0, 0, ACCENT_REACTOR, true);
        g.drawString(font, Component.translatable("gui.justreactors.label.insertion"), 52, 30, TEXT_DIM, false);
        String v = menu.getInsertion() + "%";
        g.drawString(font, v, 170 - font.width(v), 30, VALUE, true);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
