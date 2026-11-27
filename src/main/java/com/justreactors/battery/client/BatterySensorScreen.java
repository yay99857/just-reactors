package com.justreactors.battery.client;

import com.justreactors.battery.BatterySensorMenu;
import com.justreactors.battery.BatterySensorMode;
import com.justreactors.client.JrButton;
import com.justreactors.client.RedstonePortView;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BatterySensorScreen extends AbstractContainerScreen<BatterySensorMenu> {
    private JrButton minus10;
    private JrButton minus5;
    private JrButton plus5;
    private JrButton plus10;

    public BatterySensorScreen(BatterySensorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 216, 136);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;
        addRenderableWidget(new JrButton(leftPos + 8, topPos + 28, 20, 20, Component.literal("<"),
                b -> send(BatterySensorMenu.BTN_PREV_MODE), 0));
        addRenderableWidget(new JrButton(leftPos + 188, topPos + 28, 20, 20, Component.literal(">"),
                b -> send(BatterySensorMenu.BTN_NEXT_MODE), 0));
        minus10 = addRenderableWidget(new JrButton(leftPos + 8, topPos + 108, 46, 18, Component.literal("-10%"),
                b -> send(BatterySensorMenu.BTN_MINUS_10), 0));
        minus5 = addRenderableWidget(new JrButton(leftPos + 58, topPos + 108, 44, 18, Component.literal("-5%"),
                b -> send(BatterySensorMenu.BTN_MINUS_5), 0));
        plus5 = addRenderableWidget(new JrButton(leftPos + 114, topPos + 108, 44, 18, Component.literal("+5%"),
                b -> send(BatterySensorMenu.BTN_PLUS_5), 0));
        plus10 = addRenderableWidget(new JrButton(leftPos + 162, topPos + 108, 46, 18, Component.literal("+10%"),
                b -> send(BatterySensorMenu.BTN_PLUS_10), 0));
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        boolean threshold = menu.getMode().usesThreshold();
        minus10.active = threshold;
        minus5.active = threshold;
        plus5.active = threshold;
        plus10.active = threshold;
        RedstonePortView.background(g, leftPos, topPos, imageWidth, true, menu.getSignal(),
                threshold ? menu.getThresholdPercent() : Math.max(0, menu.getChargePercent()));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        BatterySensorMode mode = menu.getMode();
        int charge = menu.getChargePercent();
        Component line = mode.usesThreshold()
                ? Component.translatable("gui.justreactors.battery_sensor.threshold", menu.getThresholdPercent(),
                charge < 0 ? "-" : String.valueOf(charge))
                : charge < 0 ? Component.translatable("gui.justreactors.battery_sensor.no_battery")
                : Component.translatable("gui.justreactors.battery_sensor.charge", charge);
        RedstonePortView.labels(g, font, title, imageWidth, true, Component.translatable(mode.translationKey()),
                menu.getSignal(), line);
    }
}
