package com.justreactors.battery.client;

import com.justreactors.battery.BatteryMenu;
import com.justreactors.client.PixelIcon;
import com.justreactors.registry.ModItems;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.justreactors.client.ScreenWidgets.*;
import static com.justreactors.client.Tips.*;

public class BatteryScreen extends AbstractContainerScreen<BatteryMenu> {
    private static final int GX = 12, GY = 46, GW = 30, GH = 50;
    private static final int TX = 50;

    private final ItemStack cellIcon = new ItemStack(ModItems.BATTERY_CELL.get());

    public BatteryScreen(BatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 188);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = 10000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos, y = topPos;
        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_BATTERY);

        inset(g, x + 8, y + 26, 132, 74);
        pixelIcon(g, PixelIcon.BOLT, x + GX + (GW - 16) / 2, y + 28);
        drawVerticalGauge(g, x + GX, y + GY, GW, GH, (double) menu.getEnergy() / menu.getCapacity(), ENERGY);
        vDivider(g, x + 46, y + 30, 66);
        hDivider(g, x + 50, y + 80, 86);
        icon(g, cellIcon, x + TX, y + 83);

        // charge (+) above, discharge (-) below, with the flow direction between them
        drawSlotFrame(g, x + BatteryMenu.SLOT_X - 1, y + BatteryMenu.CHARGE_Y - 1, ENERGY);
        drawSlotFrame(g, x + BatteryMenu.SLOT_X - 1, y + BatteryMenu.DISCHARGE_Y - 1, 0xFFE39B4A);
        drawPlayerInventory(g, x + 7, y + BatteryMenu.INV_Y - 1);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        boolean flowing = menu.getInput() > 0 || menu.getOutput() > 0;
        drawTitle(g, font, title, 0, 0, ACCENT_BATTERY, flowing);

        String pct = pct((double) menu.getEnergy() / menu.getCapacity()) + "%";
        g.text(font, pct, TX, 30, VALUE, true);
        g.text(font, compact(menu.getEnergy()) + " FE", TX, 40, TEXT, false);
        g.text(font, "/ " + compact(menu.getCapacity()), TX, 49, TEXT_DIM, false);
        g.text(font, Component.translatable("gui.justreactors.battery.in", compact(menu.getInput())), TX, 60, ENERGY, false);
        g.text(font, Component.translatable("gui.justreactors.battery.out", compact(menu.getOutput())), TX, 69, 0xFFE39B4A, false);
        g.text(font, fmt(menu.getCells()), TX + 19, 88, VALUE, true);

        g.text(font, "+", BatteryMenu.SLOT_X + 5, BatteryMenu.CHARGE_Y + 20, ENERGY, true);
        g.text(font, "-", BatteryMenu.SLOT_X + 6, BatteryMenu.DISCHARGE_Y - 11, 0xFFE39B4A, true);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor g, int mx, int my) {
        super.extractTooltip(g, mx, my);
        if (hoveredSlot != null && hoveredSlot.hasItem()) return;
        List<Component> tip = null;
        if (isHovering(GX, 28, GW, GY - 28 + GH, mx, my)) {
            tip = tip("gui.justreactors.tip.energy.title");
            tip.add(value("gui.justreactors.tip.percent", pct((double) menu.getEnergy() / menu.getCapacity())));
            tip.add(line("gui.justreactors.tip.energy.stored", fmt(menu.getEnergy()), fmt(menu.getCapacity())));
        } else if (isHovering(TX, 58, 88, 20, mx, my)) {
            tip = tip("gui.justreactors.label.flow");
            tip.add(value("gui.justreactors.battery.in", fmt(menu.getInput())));
            tip.add(value("gui.justreactors.battery.out", fmt(menu.getOutput())));
            tip.add(line("gui.justreactors.battery.max_transfer", fmt(menu.getMaxTransfer())));
            tip.add(hint("gui.justreactors.tip.battery_ports.hint"));
        } else if (isHovering(TX, 82, 88, 18, mx, my)) {
            tip = tip("gui.justreactors.battery.cells");
            tip.add(value("gui.justreactors.battery.cell_count", fmt(menu.getCells())));
            tip.add(hint("gui.justreactors.tip.battery_cells.hint"));
        } else if (isHovering(BatteryMenu.SLOT_X - 1, BatteryMenu.CHARGE_Y - 1, 18, 18, mx, my)) {
            tip = tip("gui.justreactors.battery.charge_slot");
            tip.add(hint("gui.justreactors.tip.charge_slot.hint"));
        } else if (isHovering(BatteryMenu.SLOT_X - 1, BatteryMenu.DISCHARGE_Y - 1, 18, 18, mx, my)) {
            tip = tip("gui.justreactors.battery.discharge_slot");
            tip.add(hint("gui.justreactors.tip.discharge_slot.hint"));
        }
        if (tip != null) show(g, font, tip, mx, my);
    }
}
