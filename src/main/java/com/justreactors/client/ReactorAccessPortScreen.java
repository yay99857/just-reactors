package com.justreactors.client;

import com.justreactors.menu.ReactorAccessPortMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.justreactors.client.ScreenWidgets.*;

public class ReactorAccessPortScreen extends AbstractContainerScreen<ReactorAccessPortMenu> {
    private JrButton modeButton;

    public ReactorAccessPortScreen(ReactorAccessPortMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
        modeButton = addRenderableWidget(new JrButton(leftPos + 66, topPos + 34, 44, 18,
                Component.translatable("gui.justreactors.access_port.in"),
                b -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ReactorAccessPortMenu.BTN_TOGGLE_MODE);
                    }
                }, 0));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        boolean outlet = menu.isOutlet();
        if (modeButton != null) {
            modeButton.setMessage(Component.translatable(outlet ? "gui.justreactors.access_port.out" : "gui.justreactors.access_port.in"));
        }
        int x = leftPos, y = topPos;
        drawPanel(g, x, y, imageWidth, imageHeight, ACCENT_REACTOR);

        inset(g, x + 8, y + 24, 160, 42);
        // the side the port is working on gets the bright rim: fuel when inlet, waste when outlet
        drawSlotFrame(g, x + 43, y + 34, outlet ? darker(FUEL, 0.55f) : FUEL);
        drawSlotFrame(g, x + 115, y + 34, outlet ? WASTE : darker(WASTE, 0.55f));
        drawPlayerInventory(g, x + 7, y + 83);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        boolean outlet = menu.isOutlet();
        drawTitle(g, font, title, 0, 0, outlet ? WASTE : FUEL, true);
        Component fuel = Component.translatable("gui.justreactors.access_port.fuel");
        g.drawString(font, fuel, 52 - font.width(fuel) / 2, 56, FUEL, false);
        Component waste = Component.translatable("gui.justreactors.access_port.waste");
        g.drawString(font, waste, 124 - font.width(waste) / 2, 56, WASTE, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_DIM, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
