package com.justreactors.client;

import com.justreactors.menu.ReactorControlRodMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReactorControlRodScreen extends AbstractContainerScreen<ReactorControlRodMenu> {
    public ReactorControlRodScreen(ReactorControlRodMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 180;
        this.imageHeight = 108;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;

        addRenderableWidget(Button.builder(Component.literal("-10%"), b -> send(ReactorControlRodMenu.BTN_MINUS_10))
                .bounds(leftPos + 10, topPos + 50, 36, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-1%"), b -> send(ReactorControlRodMenu.BTN_MINUS_1))
                .bounds(leftPos + 50, topPos + 50, 36, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+1%"), b -> send(ReactorControlRodMenu.BTN_PLUS_1))
                .bounds(leftPos + 94, topPos + 50, 36, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+10%"), b -> send(ReactorControlRodMenu.BTN_PLUS_10))
                .bounds(leftPos + 134, topPos + 50, 36, 20).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.control_rod.apply_all"),
                        b -> send(ReactorControlRodMenu.BTN_APPLY_ALL))
                .bounds(leftPos + 10, topPos + 78, 160, 20).build());
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        ScreenWidgets.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);
        ScreenWidgets.drawHorizontalBar(g, leftPos + 10, topPos + 34, 160, 10,
                menu.getInsertion() / 100.0, 0xFF88C0D0);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFE5E9F0, false);
        g.drawString(font, Component.translatable("gui.justreactors.control_rod.insertion", menu.getInsertion()), 10, 23, 0xFFD8DEE9, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
