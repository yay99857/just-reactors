package com.justreactors.turbine.menu;

import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;
import com.justreactors.turbine.TurbineRedstonePortMode;
import com.justreactors.turbine.blockentity.TurbineRedstonePortBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TurbineRedstonePortMenu extends AbstractContainerMenu {
    public static final int BTN_PREV_MODE = 0;
    public static final int BTN_NEXT_MODE = 1;
    public static final int BTN_MINUS_10 = 2;
    public static final int BTN_MINUS_5 = 3;
    public static final int BTN_PLUS_5 = 4;
    public static final int BTN_PLUS_10 = 5;

    public final TurbineRedstonePortBlockEntity port;
    private final Level level;
    private int syncedModeOrdinal;
    private int syncedThreshold;
    private int syncedSignal;

    public TurbineRedstonePortMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public TurbineRedstonePortMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.TURBINE_REDSTONE_PORT.get(), containerId);
        this.port = (TurbineRedstonePortBlockEntity) entity;
        this.level = inv.player.level();

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return port.getMode().ordinal();
            }

            @Override
            public void set(int value) {
                syncedModeOrdinal = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return port.getThresholdPercent();
            }

            @Override
            public void set(int value) {
                syncedThreshold = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return port.getCurrentDisplaySignal();
            }

            @Override
            public void set(int value) {
                syncedSignal = value;
            }
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case BTN_PREV_MODE -> port.setMode(port.getMode().prev());
            case BTN_NEXT_MODE -> port.setMode(port.getMode().next());
            case BTN_MINUS_10 -> port.adjustThreshold(-10);
            case BTN_MINUS_5 -> port.adjustThreshold(-5);
            case BTN_PLUS_5 -> port.adjustThreshold(5);
            case BTN_PLUS_10 -> port.adjustThreshold(10);
            default -> {
                return false;
            }
        }
        return true;
    }

    public TurbineRedstonePortMode getMode() {
        return TurbineRedstonePortMode.byOrdinal(syncedModeOrdinal);
    }

    public int getThresholdPercent() {
        return syncedThreshold;
    }

    public int getSignal() {
        return syncedSignal;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, port.getBlockPos()),
                player, ModBlocks.TURBINE_REDSTONE_PORT.get());
    }
}
