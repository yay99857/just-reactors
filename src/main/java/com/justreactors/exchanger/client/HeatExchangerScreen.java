package com.justreactors.exchanger.client;

import com.justreactors.client.JrButton;
import com.justreactors.exchanger.HeatExchangerMenu;
import com.justreactors.registry.ModItems;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static com.justreactors.client.ScreenWidgets.*;
import static com.justreactors.client.Tips.*;

public class HeatExchangerScreen extends AbstractContainerScreen<HeatExchangerMenu> {
    private static final int GY = 48, GH = 76;
    private static final int HOT_X = 16, COOL_X = 50, WATER_X = 92, STEAM_X = 126;
    private static final int SY = 134;

    private final ItemStack hotIcon = new ItemStack(ModItems.HOT_COOLANT_BUCKET.get());
    private final ItemStack coolIcon = new ItemStack(ModItems.COOLANT_BUCKET.get());
    private final ItemStack waterIcon = new ItemStack(Items.WATER_BUCKET);
    private final ItemStack steamIcon = new ItemStack(ModItems.STEAM_BUCKET.get());
    private final ItemStack tubeIcon = new ItemStack(ModItems.HEAT_EXCHANGER_TUBE.get());

    public HeatExchangerScreen(HeatExchangerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 244;
        this.imageHeight = 176;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;
        int rx = leftPos + 165;
        addRenderableWidget(new JrButton(rx, topPos + 54, 35, 16, Component.translatable("gui.justreactors.button.on"),
                b -> send(HeatExchangerMenu.BTN_ON), ON).lit(menu::isActive));
        addRenderableWidget(new JrButton(rx + 37, topPos + 54, 35, 16, Component.translatable("gui.justreactors.button.off"),
                b -> send(HeatExchangerMenu.BTN_OFF), OFF).lit(() -> !menu.isActive()));
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_EXCHANGER);

        inset(g, x + 8, y + 26, 150, 102);
        double cap = menu.getCapacity();
        double flow = menu.getCooled() / (double) menu.getMaxCooling();

        // primary loop: Hot Coolant -> Coolant
        icon(g, hotIcon, x + HOT_X, y + 29);
        drawVerticalGauge(g, x + HOT_X, y + GY, 16, GH, menu.getHot() / cap, HOT_COOLANT);
        arrowRight(g, x + HOT_X + 19, y + GY + GH / 2 - 4, 7, flow, COOLANT);
        icon(g, coolIcon, x + COOL_X, y + 29);
        drawVerticalGauge(g, x + COOL_X, y + GY, 16, GH, menu.getCoolant() / cap, COOLANT);

        vDivider(g, x + 79, y + 30, 94);

        // secondary loop: Water -> Steam
        icon(g, waterIcon, x + WATER_X, y + 29);
        drawVerticalGauge(g, x + WATER_X, y + GY, 16, GH, menu.getWater() / cap, WATER);
        arrowRight(g, x + WATER_X + 19, y + GY + GH / 2 - 4, 7, flow, STEAM);
        icon(g, steamIcon, x + STEAM_X, y + 29);
        drawVerticalGauge(g, x + STEAM_X, y + GY, 16, GH, menu.getSteam() / cap, STEAM);

        // control column
        inset(g, x + 166, y + 27, 70, 20);
        hDivider(g, x + 165, y + 76, 72);
        icon(g, tubeIcon, x + 166, y + 82);

        // stats strip
        inset(g, x + 8, y + SY, 228, 34);
        icon(g, coolIcon, x + 11, y + SY + 1);
        icon(g, steamIcon, x + 127, y + SY + 1);
        drawHorizontalBar(g, x + 12, y + SY + 22, 220, 7, flow, COOLANT);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        boolean on = menu.isActive();
        boolean working = menu.getCooled() > 0;
        drawTitle(g, font, title, 0, 0, on ? ON : OFF, on && working);

        Component status = Component.translatable(!on ? "gui.justreactors.lcd.offline"
                : working ? "gui.justreactors.lcd.online" : "gui.justreactors.heat_exchanger.idle");
        int sc = !on ? OFF : working ? ON : TITLE_GOLD;
        g.drawString(font, status, 166 + (70 - font.width(status)) / 2, 33, sc, false);

        g.drawString(font, fmt(menu.getTubes()), 186, 86, VALUE, true);
        g.drawString(font, Component.translatable("gui.justreactors.heat_exchanger.tubes"), 186, 97, TEXT_DIM, false);

        g.drawString(font, fmt(menu.getCooled()) + " / " + fmt(menu.getMaxCooling()) + " mB/t", 31, SY + 6, VALUE, true);
        String steam = fmt(menu.getSteamMade()) + " mB/t";
        g.drawString(font, steam, 233 - font.width(steam), SY + 6, VALUE, true);
    }

    /** Why the exchanger is not cooling, checked in the order the limits apply. */
    private String idleReason() {
        int k = Math.max(1, menu.getSteamPerHot());
        if (!menu.isActive()) return "gui.justreactors.heat_exchanger.reason.off";
        if (menu.getCooled() > 0) return "gui.justreactors.heat_exchanger.reason.running";
        if (menu.getHot() <= 0) return "gui.justreactors.heat_exchanger.reason.no_hot";
        if (menu.getWater() < k) return "gui.justreactors.heat_exchanger.reason.no_water";
        if (menu.getCapacity() - menu.getSteam() < k) return "gui.justreactors.heat_exchanger.reason.steam_full";
        return "gui.justreactors.heat_exchanger.reason.coolant_full";
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mx, int my) {
        super.renderTooltip(g, mx, my);
        List<Component> tip = null;
        int h = GY - 29 + GH;
        if (isHovering(HOT_X, 29, 16, h, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.hot_coolant.title", menu.getHot(), menu.getCapacity());
            tip.add(hint("gui.justreactors.tip.hot_coolant.hint"));
        } else if (isHovering(COOL_X, 29, 16, h, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.coolant.title", menu.getCoolant(), menu.getCapacity());
            tip.add(hint("gui.justreactors.tip.exchanger_coolant.hint"));
        } else if (isHovering(WATER_X, 29, 16, h, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.water.title", menu.getWater(), menu.getCapacity());
            tip.add(hint("gui.justreactors.tip.exchanger_water.hint"));
        } else if (isHovering(STEAM_X, 29, 16, h, mx, my)) {
            tip = fluidTip("gui.justreactors.tip.steam.title", menu.getSteam(), menu.getCapacity());
        } else if (isHovering(166, 27, 70, 20, mx, my)) {
            tip = tip("gui.justreactors.heat_exchanger.status");
            tip.add(value(idleReason()));
        } else if (isHovering(165, 80, 72, 30, mx, my)) {
            tip = tip("gui.justreactors.heat_exchanger.tubes");
            tip.add(value("gui.justreactors.heat_exchanger.tube_rate", fmt(menu.getMaxCooling())));
            tip.add(hint("gui.justreactors.tip.tubes.hint"));
        } else if (isHovering(8, SY, 228, 34, mx, my)) {
            tip = tip("gui.justreactors.label.flow");
            tip.add(value("gui.justreactors.heat_exchanger.cooling", fmt(menu.getCooled()), fmt(menu.getMaxCooling())));
            tip.add(value("gui.justreactors.heat_exchanger.steam_rate", fmt(menu.getSteamMade()), menu.getSteamPerHot()));
            tip.add(hint("gui.justreactors.tip.exchanger_flow.hint"));
        }
        if (tip != null) show(g, font, tip, mx, my);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
