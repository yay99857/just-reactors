package com.justreactors.turbine.client;

import com.justreactors.client.JrButton;
import com.justreactors.client.PixelIcon;
import com.justreactors.registry.ModItems;
import com.justreactors.turbine.TurbineSimulation;
import com.justreactors.turbine.menu.TurbineControllerMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static com.justreactors.client.ScreenWidgets.*;
import static com.justreactors.client.Tips.*;

public class TurbineControllerScreen extends AbstractContainerScreen<TurbineControllerMenu> {
    private static final double MAX_RPM = TurbineSimulation.MAX_RPM;
    private static final int GY = 48, GH = 76;
    private static final int STEAM_X = 14, WATER_X = 41, ENERGY_X = 70, ENERGY_W = 34, RPM_X = 124;
    private static final int SY = 134;
    private static final int RPM_COLOR = 0xFFB07CF5;

    private final ItemStack steamIcon = new ItemStack(ModItems.STEAM_BUCKET.get());
    private final ItemStack waterIcon = new ItemStack(Items.WATER_BUCKET);
    private final ItemStack bladeIcon = new ItemStack(ModItems.TURBINE_ROTOR_BLADE.get());

    private JrButton ventButton;
    private JrButton coilsButton;

    public TurbineControllerScreen(TurbineControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 244;
        this.imageHeight = 196;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;

        int rx = leftPos + 165;
        addRenderableWidget(new JrButton(rx, topPos + 54, 35, 16, Component.translatable("gui.justreactors.button.on"),
                b -> {
                    if (!menu.isActive()) send(TurbineControllerMenu.BTN_TOGGLE_ACTIVE);
                }, ON).lit(menu::isActive));
        addRenderableWidget(new JrButton(rx + 37, topPos + 54, 35, 16, Component.translatable("gui.justreactors.button.off"),
                b -> {
                    if (menu.isActive()) send(TurbineControllerMenu.BTN_TOGGLE_ACTIVE);
                }, OFF).lit(() -> !menu.isActive()));
        coilsButton = addRenderableWidget(new JrButton(rx, topPos + 74, 72, 16, Component.translatable("gui.justreactors.turbine.coils_on"),
                b -> send(TurbineControllerMenu.BTN_TOGGLE_INDUCTOR), 0xFFE39B4A).lit(menu::isInductorEngaged));
        ventButton = addRenderableWidget(new JrButton(rx, topPos + 110, 72, 16,
                Component.translatable("gui.justreactors.turbine.vent_short.overflow"),
                b -> send(TurbineControllerMenu.BTN_CYCLE_VENT), WATER));

        addRenderableWidget(new JrButton(leftPos + 11, topPos + SY + 37, 34, 13, Component.literal("-100"),
                b -> send(TurbineControllerMenu.BTN_FLOW_MINUS_100), 0));
        addRenderableWidget(new JrButton(leftPos + 199, topPos + SY + 37, 34, 13, Component.literal("+100"),
                b -> send(TurbineControllerMenu.BTN_FLOW_PLUS_100), 0));
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (ventButton != null) {
            ventButton.setMessage(Component.translatable("gui.justreactors.turbine.vent_short." + menu.getVentMode().id()));
        }
        if (coilsButton != null) {
            coilsButton.setMessage(Component.translatable(
                    menu.isInductorEngaged() ? "gui.justreactors.turbine.coils_on" : "gui.justreactors.turbine.coils_off"));
        }
        int x = leftPos, y = topPos;
        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_TURBINE);

        inset(g, x + 8, y + 26, 150, 102);
        double maxFluid = Math.max(1, menu.getMaxFluidMb());
        icon(g, steamIcon, x + STEAM_X, y + 29);
        drawVerticalGauge(g, x + STEAM_X, y + GY, 16, GH, menu.getSteamMb() / maxFluid, STEAM);
        ruler(g, x + 33, y + GY, GH);
        icon(g, waterIcon, x + WATER_X, y + 29);
        drawVerticalGauge(g, x + WATER_X, y + GY, 16, GH, menu.getWaterMb() / maxFluid, WATER);
        vDivider(g, x + 63, y + 30, 94);

        pixelIcon(g, PixelIcon.BOLT, x + ENERGY_X + (ENERGY_W - 16) / 2, y + 29);
        drawVerticalGauge(g, x + ENERGY_X, y + GY, ENERGY_W, GH,
                (double) menu.getEnergy() / Math.max(1, menu.getMaxEnergy()), ENERGY);
        vDivider(g, x + 111, y + 30, 94);

        icon(g, bladeIcon, x + RPM_X, y + 29);
        drawVerticalGauge(g, x + RPM_X, y + GY, 16, GH, menu.getRpm() / MAX_RPM, RPM_COLOR);
        // coil sweet spots
        for (int sweet : new int[]{900, 1800}) {
            int ty = y + GY + GH - (int) Math.round(sweet / MAX_RPM * GH);
            g.fill(x + RPM_X - 3, ty, x + RPM_X + 19, ty + 1, 0xC0FFD866);
            g.fill(x + RPM_X - 3, ty - 1, x + RPM_X - 1, ty + 2, 0xFFFFD866);
            g.fill(x + RPM_X + 17, ty - 1, x + RPM_X + 19, ty + 2, 0xFFFFD866);
        }

        // control column
        inset(g, x + 166, y + 27, 70, 20);
        hDivider(g, x + 165, y + 95, 72);

        // stats strip
        inset(g, x + 8, y + SY, 228, 54);
        vDivider(g, x + 122, y + SY + 3, 14);
        pixelIcon(g, PixelIcon.BOLT, x + 11, y + SY + 1);
        icon(g, bladeIcon, x + 127, y + SY + 1);
        icon(g, steamIcon, x + 11, y + SY + 19);
        drawHorizontalBar(g, x + 49, y + SY + 40, 146, 7,
                (double) menu.getLastSteamFlow() / Math.max(1, menu.getMaxSteamFlow()), STEAM);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        boolean on = menu.isActive();
        drawTitle(g, font, title, 0, 0, on ? ON : OFF, on);
        int rpm = menu.getRpm();
        boolean sweet = Math.abs(rpm - 900) <= 100 || Math.abs(rpm - 1800) <= 100;
        if (sweet && on) {
            badge(g, font, Component.translatable("gui.justreactors.turbine.sweet_spot"), imageWidth - 6, 5, TITLE_GOLD);
        }

        Component status = Component.translatable(on ? "gui.justreactors.lcd.online" : "gui.justreactors.lcd.offline");
        int sc = on ? ON : OFF;
        int sx = 166 + (70 - font.width(status)) / 2;
        g.drawString(font, status, sx, 33, sc, false);

        g.drawString(font, Component.translatable("gui.justreactors.label.vent"), 166, 99, TEXT_DIM, false);

        g.drawString(font, fmt(menu.getGeneratedFe()) + " FE/t", 31, SY + 5, VALUE, true);
        g.drawString(font, fmt(rpm) + " RPM", 147, SY + 5, VALUE, true);
        g.drawString(font, Component.translatable("gui.justreactors.label.flow"), 31, SY + 23, TEXT_DIM, false);
        String flow = fmt(menu.getLastSteamFlow()) + " / " + fmt(menu.getMaxSteamFlow()) + " mB/t";
        g.drawString(font, flow, 233 - font.width(flow), SY + 23, VALUE, true);
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mx, int my) {
        super.renderTooltip(g, mx, my);
        List<Component> tip = null;
        int h = GY - 29 + GH;
        if (isHovering(STEAM_X, 29, 16, h, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.steam.title", menu.getSteamMb(), menu.getMaxFluidMb());
        } else if (isHovering(WATER_X, 29, 16, h, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.water.title", menu.getWaterMb(), menu.getMaxFluidMb());
            tip.add(hint("gui.justreactors.tip.turbine_water.hint"));
        } else if (isHovering(ENERGY_X, 29, ENERGY_W, h, mx, my)) {
            tip = tip("gui.justreactors.tip.energy.title");
            tip.add(value("gui.justreactors.tip.percent", pct((double) menu.getEnergy() / Math.max(1, menu.getMaxEnergy()))));
            tip.add(line("gui.justreactors.tip.energy.stored", fmt(menu.getEnergy()), fmt(menu.getMaxEnergy())));
            tip.add(line("gui.justreactors.tip.energy.output", fmt(menu.getGeneratedFe())));
        } else if (isHovering(RPM_X - 3, 29, 22, h, mx, my)) {
            tip = tip("gui.justreactors.tip.rotor.title");
            tip.add(value("gui.justreactors.tip.rpm", fmt(menu.getRpm())));
            tip.add(hint("gui.justreactors.rpm_tooltip_hint"));
        } else if (isHovering(165, 74, 72, 16, mx, my)) {
            tip = tip("gui.justreactors.label.coils");
            tip.add(value(menu.isInductorEngaged() ? "gui.justreactors.turbine.coils_on" : "gui.justreactors.turbine.coils_off"));
            tip.add(hint("gui.justreactors.tip.coils.hint"));
        } else if (isHovering(165, 110, 72, 16, mx, my)) {
            tip = tip(menu.getVentMode().translationKey());
            tip.add(hint("gui.justreactors.turbine.vent_tooltip"));
        } else if (isHovering(49, SY + 18, 146, 31, mx, my)) {
            tip = tip("gui.justreactors.label.flow");
            tip.add(value("gui.justreactors.turbine.flow", fmt(menu.getLastSteamFlow()), fmt(menu.getMaxSteamFlow())));
            tip.add(hint("gui.justreactors.tip.flow.hint"));
        }
        if (tip == null && isHovering(9, SY + 1, 112, 17, mx, my)) {
            tip = tip("gui.justreactors.label.output");
            tip.add(value("gui.justreactors.tip.energy.output", fmt(menu.getGeneratedFe())));
        } else if (tip == null && isHovering(124, SY + 1, 112, 17, mx, my)) {
            tip = tip("gui.justreactors.tip.rotor.title");
            tip.add(value("gui.justreactors.tip.rpm", fmt(menu.getRpm())));
            tip.add(hint("gui.justreactors.rpm_tooltip_hint"));
        }
        if (tip != null) {
            show(g, font, tip, mx, my);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
