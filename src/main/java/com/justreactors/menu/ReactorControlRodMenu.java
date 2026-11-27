package com.justreactors.menu;

import com.justreactors.blockentity.ReactorControlRodBlockEntity;
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

public class ReactorControlRodMenu extends AbstractContainerMenu {
    public static final int BTN_MINUS_10 = 0;
    public static final int BTN_MINUS_1 = 1;
    public static final int BTN_PLUS_1 = 2;
    public static final int BTN_PLUS_10 = 3;
    public static final int BTN_APPLY_ALL = 4;

    public final ReactorControlRodBlockEntity rod;
    private final Level level;
    private int syncedInsertion;

    public ReactorControlRodMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ReactorControlRodMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.REACTOR_CONTROL_ROD.get(), containerId);
        this.rod = (ReactorControlRodBlockEntity) entity;
        this.level = inv.player.level();

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return rod.getInsertion();
            }

            @Override
            public void set(int value) {
                syncedInsertion = value;
            }
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case BTN_MINUS_10 -> rod.setInsertion(rod.getInsertion() - 10);
            case BTN_MINUS_1 -> rod.setInsertion(rod.getInsertion() - 1);
            case BTN_PLUS_1 -> rod.setInsertion(rod.getInsertion() + 1);
            case BTN_PLUS_10 -> rod.setInsertion(rod.getInsertion() + 10);
            case BTN_APPLY_ALL -> rod.applyToAllRods();
            default -> {
                return false;
            }
        }
        return true;
    }

    public int getInsertion() {
        return syncedInsertion;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, rod.getBlockPos()),
                player, ModBlocks.REACTOR_CONTROL_ROD.get());
    }
}
