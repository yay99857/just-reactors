package com.justreactors.client;

import com.justreactors.menu.CyaniteReprocessorMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class CyaniteReprocessorScreen extends AbstractContainerScreen<CyaniteReprocessorMenu> {
    public CyaniteReprocessorScreen(CyaniteReprocessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {

        ScreenWidgets.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);

        ScreenWidgets.drawVerticalGauge(g, leftPos + 12, topPos + 24, 12, 46,
                (double) menu.getEnergy() / menu.getMaxEnergy(), 0xFF2ECC71);
        ScreenWidgets.drawVerticalGauge(g, leftPos + 30, topPos + 24, 12, 46,
                (double) menu.getWater() / menu.getMaxWater(), 0xFF2980B9);

        ScreenWidgets.drawSlotFrame(g, leftPos + 55, topPos + 34, 0xFF3498DB);
        ScreenWidgets.drawSlotFrame(g, leftPos + 115, topPos + 34, 0xFF8E44AD);

        ScreenWidgets.drawHorizontalBar(g, leftPos + 79, topPos + 39, 30, 8,
                (double) menu.getProgress() / menu.getMaxProgress(), 0xFF88C0D0);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                ScreenWidgets.drawSlotFrame(g, leftPos + 7 + col * 18, topPos + 83 + row * 18, 0xFF181B22);
            }
        }
        for (int col = 0; col < 9; col++) {
            ScreenWidgets.drawSlotFrame(g, leftPos + 7 + col * 18, topPos + 141, 0xFF181B22);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFE5E9F0, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFD8DEE9, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int x, int y) {
        super.renderTooltip(g, x, y);
        if (isHovering(12, 24, 12, 46, x, y)) {
            g.renderTooltip(font, Component.translatable("gui.justreactors.energy_tooltip",
                    formatNumber(menu.getEnergy()), formatNumber(menu.getMaxEnergy())), x, y);
        } else if (isHovering(30, 24, 12, 46, x, y)) {
            g.renderTooltip(font, Component.translatable("gui.justreactors.coolant_tooltip",
                    formatNumber(menu.getWater()), formatNumber(menu.getMaxWater())), x, y);
        }
    }

    private static String formatNumber(int n) {
        return String.format(Locale.ROOT, "%,d", n);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
