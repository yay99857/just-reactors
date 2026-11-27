package com.justreactors.client;

import com.justreactors.menu.ReactorRedstonePortMenu;
import com.justreactors.reactor.RedstonePortMode;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReactorRedstonePortScreen extends AbstractContainerScreen<ReactorRedstonePortMenu> {
    private Button minus10;
    private Button minus5;
    private Button plus5;
    private Button plus10;

    public ReactorRedstonePortScreen(ReactorRedstonePortMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 216, 132);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> send(ReactorRedstonePortMenu.BTN_PREV_MODE))
                .bounds(leftPos + 10, topPos + 32, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> send(ReactorRedstonePortMenu.BTN_NEXT_MODE))
                .bounds(leftPos + 186, topPos + 32, 20, 20).build());

        minus10 = addRenderableWidget(Button.builder(Component.literal("-10%"), b -> send(ReactorRedstonePortMenu.BTN_MINUS_10))
                .bounds(leftPos + 10, topPos + 102, 44, 20).build());
        minus5 = addRenderableWidget(Button.builder(Component.literal("-5%"), b -> send(ReactorRedstonePortMenu.BTN_MINUS_5))
                .bounds(leftPos + 58, topPos + 102, 42, 20).build());
        plus5 = addRenderableWidget(Button.builder(Component.literal("+5%"), b -> send(ReactorRedstonePortMenu.BTN_PLUS_5))
                .bounds(leftPos + 116, topPos + 102, 42, 20).build());
        plus10 = addRenderableWidget(Button.builder(Component.literal("+10%"), b -> send(ReactorRedstonePortMenu.BTN_PLUS_10))
                .bounds(leftPos + 162, topPos + 102, 44, 20).build());
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        ScreenWidgets.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);

        RedstonePortMode mode = menu.getMode();
        boolean isOut = mode.isOutput();
        minus10.active = isOut;
        minus5.active = isOut;
        plus5.active = isOut;
        plus10.active = isOut;

        int barColor = isOut ? 0xFFE74C3C : 0xFF4F5D73;
        double ratio = isOut ? menu.getThresholdPercent() / 100.0 : menu.getSignal() / 15.0;
        ScreenWidgets.drawHorizontalBar(g, leftPos + 10, topPos + 84, 196, 9, ratio, barColor);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, titleLabelX, titleLabelY, 0xFFE5E9F0, false);

        RedstonePortMode mode = menu.getMode();
        Component kindBadge = Component.translatable(
                mode.isOutput() ? "gui.justreactors.redstone_port.output" : "gui.justreactors.redstone_port.input");
        int badgeColor = mode.isOutput() ? 0xFFE74C3C : 0xFF5EC8F5;
        g.text(font, kindBadge, imageWidth - 8 - font.width(kindBadge), 6, badgeColor, false);

        Component modeName = Component.translatable(mode.translationKey());
        int centeredX = (imageWidth - font.width(modeName)) / 2;
        g.text(font, modeName, centeredX, 38, 0xFFF0F4F8, false);

        if (mode.isOutput()) {
            Component thresh = mode == RedstonePortMode.OUTPUT_FUEL_TEMP_ABOVE
                    ? Component.translatable("gui.justreactors.redstone_port.threshold_temp",
                    menu.getThresholdPercent() * 20, menu.getThresholdPercent())
                    : Component.translatable("gui.justreactors.redstone_port.threshold", menu.getThresholdPercent());
            g.text(font, thresh, 10, 62, 0xFFD8DEE9, false);
        } else {
            g.text(font, Component.translatable("gui.justreactors.redstone_port.input_hint"), 10, 62, 0xFF9AA5B1, false);
        }

        Component sig = Component.translatable("gui.justreactors.redstone_port.signal", menu.getSignal());
        g.text(font, sig, 10, 73, 0xFFEBCB8B, false);
    }
}
