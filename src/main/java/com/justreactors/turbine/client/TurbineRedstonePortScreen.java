package com.justreactors.turbine.client;

import com.justreactors.client.JrButton;
import com.justreactors.client.RedstonePortView;
import com.justreactors.turbine.TurbineRedstonePortMode;
import com.justreactors.turbine.menu.TurbineRedstonePortMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TurbineRedstonePortScreen extends AbstractContainerScreen<TurbineRedstonePortMenu> {
    private JrButton minus10;
    private JrButton minus5;
    private JrButton plus5;
    private JrButton plus10;

    public TurbineRedstonePortScreen(TurbineRedstonePortMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 216;
        this.imageHeight = 136;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;
        addRenderableWidget(new JrButton(leftPos + 8, topPos + 28, 20, 20, Component.literal("<"),
                b -> send(TurbineRedstonePortMenu.BTN_PREV_MODE), 0));
        addRenderableWidget(new JrButton(leftPos + 188, topPos + 28, 20, 20, Component.literal(">"),
                b -> send(TurbineRedstonePortMenu.BTN_NEXT_MODE), 0));
        minus10 = addRenderableWidget(new JrButton(leftPos + 8, topPos + 108, 46, 18, Component.literal("-10%"),
                b -> send(TurbineRedstonePortMenu.BTN_MINUS_10), 0));
        minus5 = addRenderableWidget(new JrButton(leftPos + 58, topPos + 108, 44, 18, Component.literal("-5%"),
                b -> send(TurbineRedstonePortMenu.BTN_MINUS_5), 0));
        plus5 = addRenderableWidget(new JrButton(leftPos + 114, topPos + 108, 44, 18, Component.literal("+5%"),
                b -> send(TurbineRedstonePortMenu.BTN_PLUS_5), 0));
        plus10 = addRenderableWidget(new JrButton(leftPos + 162, topPos + 108, 46, 18, Component.literal("+10%"),
                b -> send(TurbineRedstonePortMenu.BTN_PLUS_10), 0));
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        TurbineRedstonePortMode mode = menu.getMode();
        boolean out = mode.isOutput();
        minus10.active = out;
        minus5.active = out;
        plus5.active = out;
        plus10.active = out;
        RedstonePortView.background(g, leftPos, topPos, imageWidth, out, menu.getSignal(), menu.getThresholdPercent());
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        TurbineRedstonePortMode mode = menu.getMode();
        Component threshold = null;
        if (mode.isOutput()) {
            threshold = mode.isRpmThreshold()
                    ? Component.translatable("gui.justreactors.turbine_redstone_port.threshold_rpm",
                    menu.getThresholdPercent() * 30, menu.getThresholdPercent())
                    : Component.translatable("gui.justreactors.redstone_port.threshold", menu.getThresholdPercent());
        }
        RedstonePortView.labels(g, font, title, imageWidth, mode.isOutput(),
                Component.translatable(mode.translationKey()), menu.getSignal(), threshold);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
