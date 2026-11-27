package com.justreactors.client;

import com.justreactors.menu.ReactorControllerMenu;
import com.justreactors.registry.ModItems;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

import static com.justreactors.client.ScreenWidgets.*;
import static com.justreactors.client.Tips.*;

public class ReactorControllerScreen extends AbstractContainerScreen<ReactorControllerMenu> {
    private static final double HEAT_SCALE = 2000.0;

    // gauge bay
    private static final int GY = 48, GH = 76;
    private static final int FUEL_X = 14, CORE_X = 44, CASING_X = 71;
    private static final int WATER_X = 104, STEAM_X = 130, ENERGY_X = 106, ENERGY_W = 34;
    // stats strip
    private static final int SY = 134;

    private final ItemStack fuelIcon = new ItemStack(ModItems.URANIUM_INGOT.get());
    private final ItemStack wasteIcon = new ItemStack(ModItems.CYANITE_INGOT.get());
    private final ItemStack waterIcon = new ItemStack(Items.WATER_BUCKET);
    private final ItemStack steamIcon = new ItemStack(ModItems.STEAM_BUCKET.get());
    private final ItemStack coolantIcon = new ItemStack(ModItems.COOLANT_BUCKET.get());
    private final ItemStack hotCoolantIcon = new ItemStack(ModItems.HOT_COOLANT_BUCKET.get());
    private final ItemStack rodIcon = new ItemStack(ModItems.REACTOR_CONTROL_ROD.get());

    public ReactorControllerScreen(ReactorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 244, 178);
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
                    if (!menu.isActive()) send(ReactorControllerMenu.BTN_TOGGLE_ACTIVE);
                }, ON).lit(menu::isActive));
        addRenderableWidget(new JrButton(rx + 37, topPos + 54, 35, 16, Component.translatable("gui.justreactors.button.off"),
                b -> {
                    if (menu.isActive()) send(ReactorControllerMenu.BTN_TOGGLE_ACTIVE);
                }, OFF).lit(() -> !menu.isActive()));
        addRenderableWidget(new JrButton(rx, topPos + 74, 72, 16, Component.translatable("gui.justreactors.reactor.eject_waste"),
                b -> send(ReactorControllerMenu.BTN_EJECT_WASTE), WASTE));
        addRenderableWidget(new JrButton(rx, topPos + 113, 35, 14, Component.literal("-10"),
                b -> send(ReactorControllerMenu.BTN_RODS_MINUS_10), 0));
        addRenderableWidget(new JrButton(rx + 37, topPos + 113, 35, 14, Component.literal("+10"),
                b -> send(ReactorControllerMenu.BTN_RODS_PLUS_10), 0));
    }

    private void send(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos, y = topPos;
        boolean steam = menu.isActiveCooled();

        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_REACTOR);

        // gauge bay
        inset(g, x + 8, y + 26, 150, 102);
        double maxFuel = Math.max(1, menu.getMaxFuelMb());
        icon(g, fuelIcon, x + FUEL_X, y + 29);
        drawStackedGauge(g, x + FUEL_X, y + GY, 16, GH,
                menu.getWasteMb() / maxFuel, WASTE, menu.getFuelMb() / maxFuel, FUEL);
        vDivider(g, x + 37, y + 30, 94);

        pixelIcon(g, PixelIcon.FLAME, x + CORE_X, y + 29);
        drawHeatGauge(g, x + CORE_X, y + GY, 16, GH, menu.getFuelHeat() / HEAT_SCALE);
        ruler(g, x + 63, y + GY, GH);
        pixelIcon(g, PixelIcon.THERMOMETER, x + CASING_X, y + 29);
        drawHeatGauge(g, x + CASING_X, y + GY, 16, GH, menu.getCasingHeat() / HEAT_SCALE);
        vDivider(g, x + 94, y + 30, 94);

        boolean loop = menu.isCoolantLoop();
        if (steam) {
            double maxFluid = Math.max(1, menu.getMaxFluidMb());
            icon(g, loop ? coolantIcon : waterIcon, x + WATER_X, y + 29);
            drawVerticalGauge(g, x + WATER_X, y + GY, 16, GH, menu.getCoolantMb() / maxFluid, loop ? COOLANT : WATER);
            ruler(g, x + 123, y + GY, GH);
            icon(g, loop ? hotCoolantIcon : steamIcon, x + STEAM_X, y + 29);
            drawVerticalGauge(g, x + STEAM_X, y + GY, 16, GH, menu.getSteamMb() / maxFluid, loop ? HOT_COOLANT : STEAM);
        } else {
            pixelIcon(g, PixelIcon.BOLT, x + ENERGY_X + (ENERGY_W - 16) / 2, y + 29);
            drawVerticalGauge(g, x + ENERGY_X, y + GY, ENERGY_W, GH,
                    (double) menu.getEnergy() / Math.max(1, menu.getMaxEnergy()), ENERGY);
        }

        // control column
        inset(g, x + 166, y + 27, 70, 20);
        hDivider(g, x + 165, y + 94, 72);
        drawHorizontalBar(g, x + 166, y + 106, 70, 4, menu.getAvgInsertion() / 100.0, 0xFF8FA3BF);

        // stats strip
        inset(g, x + 8, y + SY, 228, 36);
        vDivider(g, x + 122, y + SY + 3, 30);
        if (steam) {
            icon(g, loop ? hotCoolantIcon : steamIcon, x + 11, y + SY + 1);
        } else {
            pixelIcon(g, PixelIcon.BOLT, x + 11, y + SY + 1);
        }
        icon(g, fuelIcon, x + 127, y + SY + 1);
        pixelIcon(g, PixelIcon.FLAME, x + 11, y + SY + 19);
        pixelIcon(g, PixelIcon.THERMOMETER, x + 127, y + SY + 19);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        boolean steam = menu.isActiveCooled();
        boolean on = menu.isActive();

        drawTitle(g, font, title, 0, 0, on ? ON : OFF, on);
        badge(g, font, Component.translatable(!steam ? "gui.justreactors.mode.passive"
                        : menu.isCoolantLoop() ? "gui.justreactors.mode.active_coolant" : "gui.justreactors.mode.active"),
                imageWidth - 6, 5, steam ? ACCENT_TURBINE : ENERGY);

        // status display
        Component status = Component.translatable(on ? "gui.justreactors.lcd.online" : "gui.justreactors.lcd.offline");
        int sc = on ? ON : OFF;
        int sx = 166 + (70 - font.width(status)) / 2;
        g.text(font, status, sx, 33, sc, false);

        // control rods
        g.text(font, Component.translatable("gui.justreactors.label.rods"), 166, 98, TEXT_DIM, false);
        String rods = menu.getAvgInsertion() + "%";
        g.text(font, rods, 236 - font.width(rods), 98, VALUE, false);

        // stats
        g.text(font, steam ? fmt(menu.getLastOutputMb()) + " mB/t" : fmt(menu.getGeneratedFe()) + " FE/t", 31, SY + 5, VALUE, true);
        g.text(font, String.format(Locale.ROOT, "%.3f mB/t", menu.getBurnRateMb()), 147, SY + 5, VALUE, true);
        g.text(font, fmt(menu.getFuelHeat()) + " °C", 31, SY + 23, VALUE, true);
        g.text(font, fmt(menu.getCasingHeat()) + " °C", 147, SY + 23, VALUE, true);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int mx, int my) {
        super.extractTooltip(g, mx, my);
        boolean steam = menu.isActiveCooled();
        List<Component> tip = null;
        if (isHovering(FUEL_X, 29, 16, GY - 29 + GH, mx, my)) {
            double max = Math.max(1, menu.getMaxFuelMb());
            tip = tip("gui.justreactors.tip.fuel.title");
            tip.add(value("gui.justreactors.tip.fuel.full", pct((menu.getFuelMb() + menu.getWasteMb()) / max)));
            tip.add(value("gui.justreactors.tip.fuel.depleted",
                    pct(menu.getWasteMb() / Math.max(1.0, menu.getFuelMb() + menu.getWasteMb()))));
            tip.add(Component.empty());
            tip.add(line("gui.justreactors.tip.fuel.fuel", fmt(menu.getFuelMb())));
            tip.add(line("gui.justreactors.tip.fuel.waste", fmt(menu.getWasteMb())));
            tip.add(line("gui.justreactors.tip.capacity", fmt(menu.getMaxFuelMb())));
        } else if (isHovering(CORE_X, 29, 16, GY - 29 + GH, mx, my)) {
            tip = tip("gui.justreactors.tip.core.title");
            tip.add(value("gui.justreactors.tip.temp", fmt(menu.getFuelHeat())));
            tip.add(hint("gui.justreactors.tip.core.hint"));
        } else if (isHovering(CASING_X, 29, 16, GY - 29 + GH, mx, my)) {
            tip = tip("gui.justreactors.tip.casing.title");
            tip.add(value("gui.justreactors.tip.temp", fmt(menu.getCasingHeat())));
            tip.add(hint("gui.justreactors.tip.casing.hint"));
        } else if (steam && isHovering(WATER_X, 29, 16, GY - 29 + GH, mx, my)) {
            tip = fluidTip(menu.isCoolantLoop() ? "gui.justreactors.tip.coolant.title" : "gui.justreactors.tip.water.title",
                    menu.getCoolantMb(), menu.getMaxFluidMb());
            tip.add(hint("gui.justreactors.tip.reactor_coolant.hint"));
        } else if (steam && isHovering(STEAM_X, 29, 16, GY - 29 + GH, mx, my)) {
            if (menu.isCoolantLoop()) {
                tip = fluidTip("gui.justreactors.tip.hot_coolant.title", menu.getSteamMb(), menu.getMaxFluidMb());
                tip.add(line("gui.justreactors.tip.hot_coolant.rate", fmt(menu.getLastOutputMb())));
                tip.add(hint("gui.justreactors.tip.hot_coolant.hint"));
            } else {
                tip = fluidTip("gui.justreactors.tip.steam.title", menu.getSteamMb(), menu.getMaxFluidMb());
                tip.add(line("gui.justreactors.tip.steam.rate", fmt(menu.getLastSteamMb())));
            }
        } else if (!steam && isHovering(ENERGY_X, 29, ENERGY_W, GY - 29 + GH, mx, my)) {
            tip = tip("gui.justreactors.tip.energy.title");
            tip.add(value("gui.justreactors.tip.percent", pct((double) menu.getEnergy() / Math.max(1, menu.getMaxEnergy()))));
            tip.add(line("gui.justreactors.tip.energy.stored", fmt(menu.getEnergy()), fmt(menu.getMaxEnergy())));
            tip.add(line("gui.justreactors.tip.energy.output", fmt(menu.getGeneratedFe())));
        } else if (isHovering(imageWidth - 6 - 90, 4, 90, 13, mx, my)) {
            tip = tip(steam ? "gui.justreactors.mode.active" : "gui.justreactors.mode.passive");
            tip.add(hint(steam ? "gui.justreactors.mode.active_tooltip" : "gui.justreactors.mode.passive_tooltip"));
        }
        if (tip == null && isHovering(9, SY + 1, 112, 17, mx, my)) {
            boolean loop = menu.isCoolantLoop();
            tip = tip(!steam ? "gui.justreactors.label.output" : loop ? "gui.justreactors.tip.hot_coolant.title" : "gui.justreactors.label.steam");
            tip.add(value(!steam ? "gui.justreactors.tip.energy.output" : loop ? "gui.justreactors.tip.hot_coolant.rate" : "gui.justreactors.tip.steam.rate",
                    fmt(steam ? menu.getLastOutputMb() : menu.getGeneratedFe())));
        } else if (tip == null && isHovering(124, SY + 1, 112, 17, mx, my)) {
            tip = tip("gui.justreactors.label.burn");
            tip.add(value("gui.justreactors.tip.burn", String.format(Locale.ROOT, "%.3f", menu.getBurnRateMb())));
            tip.add(hint("gui.justreactors.tip.burn.hint"));
        } else if (tip == null && isHovering(9, SY + 19, 112, 17, mx, my)) {
            tip = tip("gui.justreactors.tip.core.title");
            tip.add(value("gui.justreactors.tip.temp", fmt(menu.getFuelHeat())));
        } else if (tip == null && isHovering(124, SY + 19, 112, 17, mx, my)) {
            tip = tip("gui.justreactors.tip.casing.title");
            tip.add(value("gui.justreactors.tip.temp", fmt(menu.getCasingHeat())));
        }
        if (tip != null) {
            show(g, font, tip, mx, my);
        }
    }
}
