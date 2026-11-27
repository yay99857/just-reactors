package com.justreactors.client;

import com.justreactors.menu.ReactorAccessPortMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReactorAccessPortScreen extends AbstractContainerScreen<ReactorAccessPortMenu> {
    private Button modeButton;

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

        modeButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.justreactors.access_port.inlet"),
                        b -> {
                            if (minecraft != null && minecraft.gameMode != null) {
                                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ReactorAccessPortMenu.BTN_TOGGLE_MODE);
                            }
                        })
                .bounds(leftPos + 48, topPos + 56, 80, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        if (modeButton != null) {
            modeButton.setMessage(Component.translatable(
                    menu.isOutlet() ? "gui.justreactors.access_port.outlet" : "gui.justreactors.access_port.inlet"));
        }

        ScreenWidgets.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);

        ScreenWidgets.drawSlotFrame(g, leftPos + 43, topPos + 34, 0xFFF1C40F);
        ScreenWidgets.drawSlotFrame(g, leftPos + 115, topPos + 34, 0xFF3498DB);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                ScreenWidgets.drawSlotFrame(g, leftPos + 7 + col * 18, topPos + 83 + row * 18, 0xFF181B22);
            }
        }
        for (int col = 0; col < 9; col++) {
            ScreenWidgets.drawSlotFrame(g, leftPos + 7 + col * 18, topPos + 141, 0xFF181B22);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFE5E9F0, false);
        g.drawString(font, Component.translatable("gui.justreactors.access_port.fuel"), 40, 23, 0xFFF1C40F, false);
        g.drawString(font, Component.translatable("gui.justreactors.access_port.waste"), 110, 23, 0xFF3498DB, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFD8DEE9, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
