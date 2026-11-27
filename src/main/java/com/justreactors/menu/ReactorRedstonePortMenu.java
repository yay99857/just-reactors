package com.justreactors.menu;

import com.justreactors.blockentity.ReactorRedstonePortBlockEntity;
import com.justreactors.reactor.RedstonePortMode;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ReactorRedstonePortMenu extends AbstractContainerMenu {
    public static final int BTN_PREV_MODE = 0;
    public static final int BTN_NEXT_MODE = 1;
    public static final int BTN_MINUS_10 = 2;
    public static final int BTN_MINUS_5 = 3;
    public static final int BTN_PLUS_5 = 4;
    public static final int BTN_PLUS_10 = 5;

    public final ReactorRedstonePortBlockEntity port;
    private final Level level;
    private int syncedModeOrdinal;
    private int syncedThreshold;
    private int syncedSignal;

    public ReactorRedstonePortMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ReactorRedstonePortMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.REACTOR_REDSTONE_PORT.get(), containerId);
        this.port = (ReactorRedstonePortBlockEntity) entity;
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

    public RedstonePortMode getMode() {
        return RedstonePortMode.byOrdinal(syncedModeOrdinal);
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
                player, ModBlocks.REACTOR_REDSTONE_PORT.get());
    }
}
