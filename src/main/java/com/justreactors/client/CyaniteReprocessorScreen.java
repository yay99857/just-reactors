package com.justreactors.client;

import com.justreactors.menu.CyaniteReprocessorMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

import static com.justreactors.client.ScreenWidgets.*;
import static com.justreactors.client.Tips.*;

public class CyaniteReprocessorScreen extends AbstractContainerScreen<CyaniteReprocessorMenu> {
    private static final int GY = 27, GH = 38;

    public CyaniteReprocessorScreen(CyaniteReprocessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
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
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos, y = topPos;
        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_REPROCESSOR);
        inset(g, x + 8, y + 24, 160, 44);

        drawVerticalGauge(g, x + 13, y + GY, 10, GH, (double) menu.getEnergy() / Math.max(1, menu.getMaxEnergy()), ENERGY);
        drawVerticalGauge(g, x + 28, y + GY, 10, GH, (double) menu.getWater() / Math.max(1, menu.getMaxWater()), WATER);
        vDivider(g, x + 45, y + 27, 38);

        drawSlotFrame(g, x + 55, y + 34, WASTE);
        drawSlotFrame(g, x + 115, y + 34, 0xFF4F7BFF);
        double progress = (double) menu.getProgress() / Math.max(1, menu.getMaxProgress());
        arrowRight(g, x + 80, y + 38, 24, progress, ACCENT_REPROCESSOR);
        drawPlayerInventory(g, x + 7, y + 83);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        boolean working = menu.getProgress() > 0;
        drawTitle(g, font, title, 0, 0, working ? ON : 0xFF7A8594, working);
        g.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_DIM, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int mx, int my) {
        super.extractTooltip(g, mx, my);
        List<Component> tip = null;
        if (isHovering(13, GY, 10, GH, mx, my)) {
            tip = tip("gui.justreactors.tip.energy.title");
            tip.add(line("gui.justreactors.tip.energy.stored", fmt(menu.getEnergy()), fmt(menu.getMaxEnergy())));
        } else if (isHovering(28, GY, 10, GH, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.water.title", menu.getWater(), menu.getMaxWater());
        } else if (isHovering(80, 36, 30, 12, mx, my)) {
            tip = tip("gui.justreactors.tip.progress.title");
            tip.add(value("gui.justreactors.tip.percent", pct((double) menu.getProgress() / Math.max(1, menu.getMaxProgress()))));
        }
        if (tip != null) {
            show(g, font, tip, mx, my);
        }
    }
}
