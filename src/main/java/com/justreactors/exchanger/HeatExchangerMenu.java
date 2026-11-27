package com.justreactors.exchanger;

import com.justreactors.menu.SyncedInt;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.IntSupplier;

public class HeatExchangerMenu extends AbstractContainerMenu {
    public static final int BTN_ON = 0;
    public static final int BTN_OFF = 1;

    public final HeatExchangerControllerBlockEntity controller;
    private final Level level;

    private final SyncedInt active;
    private final SyncedInt hot;
    private final SyncedInt coolant;
    private final SyncedInt water;
    private final SyncedInt steam;
    private final SyncedInt capacity;
    private final SyncedInt cooled;
    private final SyncedInt maxCooling;
    private final SyncedInt steamMade;
    private final SyncedInt tubes;
    private final SyncedInt steamPerHot;

    public HeatExchangerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public HeatExchangerMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.HEAT_EXCHANGER.get(), containerId);
        this.controller = (HeatExchangerControllerBlockEntity) entity;
        this.level = inv.player.level();
        this.active = sync(() -> controller.isActive() ? 1 : 0);
        this.hot = sync(() -> controller.getHotTank().getFluidAmount());
        this.coolant = sync(() -> controller.getCoolantTank().getFluidAmount());
        this.water = sync(() -> controller.getWaterTank().getFluidAmount());
        this.steam = sync(() -> controller.getSteamTank().getFluidAmount());
        this.capacity = sync(() -> controller.getHotTank().getCapacity());
        this.cooled = sync(controller::getLastCooledMb);
        this.maxCooling = sync(controller::getMaxCoolingMb);
        this.steamMade = sync(controller::getLastSteamMb);
        this.tubes = sync(controller::getTubes);
        this.steamPerHot = sync(HeatExchangerControllerBlockEntity::steamPerHotCoolant);
    }

    private SyncedInt sync(IntSupplier supplier) {
        SyncedInt synced = new SyncedInt(supplier);
        addDataSlot(synced.low());
        addDataSlot(synced.high());
        return synced;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case BTN_ON -> controller.setActive(true);
            case BTN_OFF -> controller.setActive(false);
            default -> {
                return false;
            }
        }
        return true;
    }

    public boolean isActive() {
        return active.get() != 0;
    }

    public int getHot() {
        return hot.get();
    }

    public int getCoolant() {
        return coolant.get();
    }

    public int getWater() {
        return water.get();
    }

    public int getSteam() {
        return steam.get();
    }

    public int getCapacity() {
        return Math.max(1, capacity.get());
    }

    public int getCooled() {
        return cooled.get();
    }

    public int getMaxCooling() {
        return Math.max(1, maxCooling.get());
    }

    public int getSteamMade() {
        return steamMade.get();
    }

    public int getTubes() {
        return tubes.get();
    }

    public int getSteamPerHot() {
        return steamPerHot.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, controller.getBlockPos()), player,
                ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
    }
}
