package com.justreactors.client;

import com.justreactors.menu.ReactorControllerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class ReactorControllerScreen extends AbstractContainerScreen<ReactorControllerMenu> {
    private Button toggleButton;

    public ReactorControllerScreen(ReactorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 236, 154);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;

        toggleButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.reactor.toggle"),
                        btn -> sendButton(ReactorControllerMenu.BTN_TOGGLE_ACTIVE))
                .bounds(leftPos + 10, topPos + 124, 76, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.reactor.eject_waste"),
                        btn -> sendButton(ReactorControllerMenu.BTN_EJECT_WASTE))
                .bounds(leftPos + 92, topPos + 124, 74, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("-10%"),
                        btn -> sendButton(ReactorControllerMenu.BTN_RODS_MINUS_10))
                .bounds(leftPos + 172, topPos + 124, 26, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("+10%"),
                        btn -> sendButton(ReactorControllerMenu.BTN_RODS_PLUS_10))
                .bounds(leftPos + 200, topPos + 124, 26, 20)
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

        ScreenWidgets.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);

        // Fuel + Waste stacked gauge
        int maxFuel = menu.getMaxFuelMb();
        int barX = leftPos + 166;
        int barY = topPos + 24;
        int barW = 16;
        int barH = 86;
        ScreenWidgets.drawGaugeFrame(g, barX, barY, barW, barH);
        int wasteH = Math.min(barH, Math.max(0, (int) Math.round((double) menu.getWasteMb() / maxFuel * barH)));
        int fuelH = Math.min(barH - wasteH, Math.max(0, (int) Math.round((double) menu.getFuelMb() / maxFuel * barH)));
        if (wasteH > 0) {
            g.fill(barX, barY + barH - wasteH, barX + barW, barY + barH, 0xFF3498DB);
        }
        if (fuelH > 0) {
            g.fill(barX, barY + barH - wasteH - fuelH, barX + barW, barY + barH - wasteH, 0xFFF1C40F);
        }
        ScreenWidgets.drawGaugeTicks(g, barX, barY, barW, barH);

        if (menu.isActiveCooled()) {
            ScreenWidgets.drawVerticalGauge(g, leftPos + 188, topPos + 24, 16, 86,
                    (double) menu.getCoolantMb() / menu.getMaxFluidMb(), 0xFF2980B9);
            ScreenWidgets.drawVerticalGauge(g, leftPos + 210, topPos + 24, 16, 86,
                    (double) menu.getSteamMb() / menu.getMaxFluidMb(), 0xFFDCE6EE);
        } else {
            ScreenWidgets.drawVerticalGauge(g, leftPos + 196, topPos + 24, 20, 86,
                    (double) menu.getEnergy() / menu.getMaxEnergy(), 0xFF2ECC71);
        }

        ScreenWidgets.drawHorizontalBar(g, leftPos + 10, topPos + 78, 146, 8,
                Math.min(1.0, menu.getFuelHeat() / 2000.0), 0xFFE67E22);
        ScreenWidgets.drawHorizontalBar(g, leftPos + 10, topPos + 101, 146, 8,
                Math.min(1.0, menu.getCasingHeat() / 2000.0), 0xFFE74C3C);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, titleLabelX, titleLabelY, 0xFFE5E9F0, false);

        Component modeText = Component.translatable(
                menu.isActiveCooled() ? "gui.justreactors.mode.active" : "gui.justreactors.mode.passive");
        int modeColor = menu.isActiveCooled() ? 0xFF5EC8F5 : 0xFF64D99B;
        int badgeX = imageWidth - 8 - font.width(modeText);
        g.text(font, modeText, badgeX, 6, modeColor, false);

        int statusColor = menu.isActive() ? 0xFF2ECC71 : 0xFFE74C3C;
        g.text(font, Component.translatable(
                menu.isActive() ? "gui.justreactors.status.online" : "gui.justreactors.status.offline"), 10, 23, statusColor, false);

        if (menu.isActiveCooled()) {
            g.text(font, Component.translatable("gui.justreactors.steam_output", formatNumber(menu.getLastSteamMb())), 10, 34, 0xFFDCE6EE, false);
        } else {
            g.text(font, Component.translatable("gui.justreactors.output", formatNumber(menu.getGeneratedFe())), 10, 34, 0xFF88C0D0, false);
        }
        g.text(font, Component.translatable("gui.justreactors.burn_rate", String.format(Locale.ROOT, "%.3f", menu.getBurnRateMb())), 10, 45, 0xFFD8DEE9, false);
        g.text(font, Component.translatable("gui.justreactors.rods_avg", menu.getAvgInsertion()), 10, 56, 0xFFD8DEE9, false);

        g.text(font, Component.translatable("gui.justreactors.core_temp", menu.getFuelHeat()), 10, 68, 0xFFEBCB8B, false);
        g.text(font, Component.translatable("gui.justreactors.casing_temp", menu.getCasingHeat()), 10, 91, 0xFFD08770, false);

        if (menu.isActiveCooled()) {
            g.text(font, Component.literal("F   W   S"), 170, 113, 0xFF9AA5B1, false);
        } else {
            g.text(font, Component.literal("F     E"), 170, 113, 0xFF9AA5B1, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int x, int y) {
        super.extractTooltip(g, x, y);
        Component modeText = Component.translatable(
                menu.isActiveCooled() ? "gui.justreactors.mode.active" : "gui.justreactors.mode.passive");
        int badgeW = font.width(modeText);
        int badgeX = imageWidth - 8 - badgeW;
        if (isHovering(badgeX - 2, 4, badgeW + 4, 12, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable(
                    menu.isActiveCooled() ? "gui.justreactors.mode.active_tooltip" : "gui.justreactors.mode.passive_tooltip"), x, y);
        } else if (isHovering(166, 24, 16, 86, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.fuel_tooltip",
                    formatNumber(menu.getFuelMb()), formatNumber(menu.getWasteMb()), formatNumber(menu.getMaxFuelMb())), x, y);
        } else if (menu.isActiveCooled() && isHovering(188, 24, 16, 86, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.coolant_tooltip",
                    formatNumber(menu.getCoolantMb()), formatNumber(menu.getMaxFluidMb())), x, y);
        } else if (menu.isActiveCooled() && isHovering(210, 24, 16, 86, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.steam_tooltip",
                    formatNumber(menu.getSteamMb()), formatNumber(menu.getMaxFluidMb())), x, y);
        } else if (!menu.isActiveCooled() && isHovering(196, 24, 20, 86, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.energy_tooltip",
                    formatNumber(menu.getEnergy()), formatNumber(menu.getMaxEnergy())), x, y);
        } else if (isHovering(10, 78, 146, 8, x, y) || isHovering(10, 101, 146, 8, x, y)) {
            g.setTooltipForNextFrame(font, Component.translatable("gui.justreactors.heat_tooltip"), x, y);
        }
    }

    private static String formatNumber(int n) {
        return String.format(Locale.ROOT, "%,d", n);
    }
}
