package com.justreactors.turbine.client;

import com.justreactors.client.ScreenWidgets;
import com.justreactors.turbine.menu.TurbineControllerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class TurbineControllerScreen extends AbstractContainerScreen<TurbineControllerMenu> {
    private Button toggleButton;
    private Button inductorButton;
    private Button ventButton;

    public TurbineControllerScreen(TurbineControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 236, 164);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;

        ventButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.turbine.vent.overflow"),
                        b -> sendButton(TurbineControllerMenu.BTN_CYCLE_VENT))
                .bounds(leftPos + 10, topPos + 102, 146, 20)
                .build());

        toggleButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.reactor.toggle"),
                        b -> sendButton(TurbineControllerMenu.BTN_TOGGLE_ACTIVE))
                .bounds(leftPos + 10, topPos + 134, 68, 20)
                .build());

        inductorButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.turbine.coils_on"),
                        b -> sendButton(TurbineControllerMenu.BTN_TOGGLE_INDUCTOR))
                .bounds(leftPos + 82, topPos + 134, 74, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("-100"),
                        b -> sendButton(TurbineControllerMenu.BTN_FLOW_MINUS_100))
                .bounds(leftPos + 162, topPos + 134, 32, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("+100"),
                        b -> sendButton(TurbineControllerMenu.BTN_FLOW_PLUS_100))
                .bounds(leftPos + 196, topPos + 134, 32, 20)
                .build());
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);

        if (toggleButton != null) {
            toggleButton.setMessage(Component.translatable(
                    menu.isActive() ? "gui.justreactors.reactor.deactivate" : "gui.justreactors.reactor.activate"));
        }
        if (inductorButton != null) {
            inductorButton.setMessage(Component.translatable(
                    menu.isInductorEngaged() ? "gui.justreactors.turbine.coils_on" : "gui.justreactors.turbine.coils_off"));
        }
        if (ventButton != null) {
            ventButton.setMessage(Component.translatable(menu.getVentMode().translationKey()));
        }

        ScreenWidgets.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);

        ScreenWidgets.drawVerticalGauge(g, leftPos + 166, topPos + 24, 16, 92,
                (double) menu.getSteamMb() / menu.getMaxFluidMb(), 0xFFDCE6EE);
        ScreenWidgets.drawVerticalGauge(g, leftPos + 188, topPos + 24, 16, 92,
                (double) menu.getWaterMb() / menu.getMaxFluidMb(), 0xFF2980B9);
        ScreenWidgets.drawVerticalGauge(g, leftPos + 210, topPos + 24, 16, 92,
                (double) menu.getEnergy() / menu.getMaxEnergy(), 0xFF2ECC71);

        ScreenWidgets.drawHorizontalBar(g, leftPos + 10, topPos + 86, 146, 10,
                Math.min(1.0, menu.getRpm() / 2200.0), 0xFF88C0D0);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, titleLabelX, titleLabelY, 0xFFE5E9F0, false);

        int statusColor = menu.isActive() ? 0xFF2ECC71 : 0xFFE74C3C;
        g.text(font, Component.translatable(
                menu.isActive() ? "gui.justreactors.status.online" : "gui.justreactors.status.offline"), 10, 23, statusColor, false);

        g.text(font, Component.translatable("gui.justreactors.output", formatNumber(menu.getGeneratedFe())), 10, 35, 0xFF88C0D0, false);
        g.text(font, Component.translatable("gui.justreactors.turbine.flow",
                formatNumber(menu.getLastSteamFlow()), formatNumber(menu.getMaxSteamFlow())), 10, 47, 0xFFD8DEE9, false);
        g.text(font, Component.translatable("gui.justreactors.turbine.rpm", formatNumber(menu.getRpm())), 10, 74, 0xFFEBCB8B, false);
        g.text(font, Component.literal("S   W   E"), 170, 119, 0xFF9AA5B1, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int x, int y) {
        super.extractTooltip(g, x, y);
        if (isHovering(166, 24, 16, 92, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.steam_tooltip",
                    formatNumber(menu.getSteamMb()), formatNumber(menu.getMaxFluidMb())), x, y);
        } else if (isHovering(188, 24, 16, 92, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.coolant_tooltip",
                    formatNumber(menu.getWaterMb()), formatNumber(menu.getMaxFluidMb())), x, y);
        } else if (isHovering(210, 24, 16, 92, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.energy_tooltip",
                    formatNumber(menu.getEnergy()), formatNumber(menu.getMaxEnergy())), x, y);
        } else if (isHovering(10, 86, 146, 10, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.rpm_tooltip",
                    formatNumber(menu.getRpm())), x, y);
        } else if (isHovering(10, 102, 146, 20, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.turbine.vent_tooltip"), x, y);
        }
    }

    private static String formatNumber(int n) {
        return String.format(Locale.ROOT, "%,d", n);
    }
}
