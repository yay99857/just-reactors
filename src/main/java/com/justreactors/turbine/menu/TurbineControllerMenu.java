package com.justreactors.turbine.menu;

import com.justreactors.menu.SyncedInt;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModMenus;
import com.justreactors.turbine.TurbineVentMode;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.IntSupplier;

public class TurbineControllerMenu extends AbstractContainerMenu {
    public static final int BTN_TOGGLE_ACTIVE = 0;
    public static final int BTN_TOGGLE_INDUCTOR = 1;
    public static final int BTN_FLOW_MINUS_100 = 2;
    public static final int BTN_FLOW_PLUS_100 = 3;
    public static final int BTN_CYCLE_VENT = 4;

    public final TurbineControllerBlockEntity controller;
    private final Level level;

    private final SyncedInt active;
    private final SyncedInt inductor;
    private final SyncedInt rpm;
    private final SyncedInt generatedFe;
    private final SyncedInt energy;
    private final SyncedInt maxEnergy;
    private final SyncedInt steamMb;
    private final SyncedInt waterMb;
    private final SyncedInt maxFluidMb;
    private final SyncedInt maxSteamFlow;
    private final SyncedInt lastSteamFlow;
    private final SyncedInt ventMode;

    public TurbineControllerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public TurbineControllerMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.TURBINE_CONTROLLER.get(), containerId);
        this.controller = (TurbineControllerBlockEntity) entity;
        this.level = inv.player.level();

        this.active = syncInt(() -> controller.getSimulation().isActive() ? 1 : 0);
        this.inductor = syncInt(() -> controller.getSimulation().isInductorEngaged() ? 1 : 0);
        this.rpm = syncInt(() -> (int) Math.round(controller.getSimulation().getRpm()));
        this.generatedFe = syncInt(() -> controller.getSimulation().getLastGeneratedFe());
        this.energy = syncInt(() -> controller.getEnergyStorage().getEnergyStored());
        this.maxEnergy = syncInt(() -> controller.getEnergyStorage().getMaxEnergyStored());
        this.steamMb = syncInt(() -> controller.getSteamTank().getFluidAmount());
        this.waterMb = syncInt(() -> controller.getWaterTank().getFluidAmount());
        this.maxFluidMb = syncInt(() -> controller.getSteamTank().getCapacity());
        this.maxSteamFlow = syncInt(() -> controller.getSimulation().getMaxSteamFlowMb());
        this.lastSteamFlow = syncInt(() -> controller.getSimulation().getLastSteamConsumedMb());
        this.ventMode = syncInt(() -> controller.getVentMode().ordinal());
    }

    private SyncedInt syncInt(IntSupplier supplier) {
        SyncedInt synced = new SyncedInt(supplier);
        addDataSlot(synced.low());
        addDataSlot(synced.high());
        return synced;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case BTN_TOGGLE_ACTIVE -> controller.toggleActive();
            case BTN_TOGGLE_INDUCTOR -> controller.toggleInductor();
            case BTN_FLOW_MINUS_100 -> controller.adjustMaxSteamFlow(-100);
            case BTN_FLOW_PLUS_100 -> controller.adjustMaxSteamFlow(100);
            case BTN_CYCLE_VENT -> controller.cycleVentMode();
            default -> {
                return false;
            }
        }
        return true;
    }

    public boolean isActive() {
        return active.get() != 0;
    }

    public boolean isInductorEngaged() {
        return inductor.get() != 0;
    }

    public int getRpm() {
        return rpm.get();
    }

    public int getGeneratedFe() {
        return generatedFe.get();
    }

    public int getEnergy() {
        return energy.get();
    }

    public int getMaxEnergy() {
        return Math.max(1, maxEnergy.get());
    }

    public int getSteamMb() {
        return steamMb.get();
    }

    public int getWaterMb() {
        return waterMb.get();
    }

    public int getMaxFluidMb() {
        return Math.max(1, maxFluidMb.get());
    }

    public int getMaxSteamFlow() {
        return maxSteamFlow.get();
    }

    public int getLastSteamFlow() {
        return lastSteamFlow.get();
    }

    public TurbineVentMode getVentMode() {
        return TurbineVentMode.byOrdinal(ventMode.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, controller.getBlockPos()),
                player, ModBlocks.TURBINE_CONTROLLER.get());
    }
}
