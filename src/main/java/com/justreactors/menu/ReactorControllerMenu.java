package com.justreactors.menu;

import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.reactor.ReactorMode;
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

public class ReactorControllerMenu extends AbstractContainerMenu {
    public static final int BTN_TOGGLE_ACTIVE = 0;
    public static final int BTN_EJECT_WASTE = 1;
    public static final int BTN_RODS_MINUS_10 = 2;
    public static final int BTN_RODS_PLUS_10 = 3;

    public final ReactorControllerBlockEntity controller;
    private final Level level;

    private final SyncedInt active;
    private final SyncedInt mode;
    private final SyncedInt energy;
    private final SyncedInt maxEnergy;
    private final SyncedInt generatedFe;
    private final SyncedInt coolantMb;
    private final SyncedInt steamMb;
    private final SyncedInt maxFluidMb;
    private final SyncedInt lastSteamMb;
    private final SyncedInt fuelMb;
    private final SyncedInt wasteMb;
    private final SyncedInt maxFuelMb;
    private final SyncedInt fuelHeat;
    private final SyncedInt casingHeat;
    private final SyncedInt burnRateMilliMb; // mB/t * 1000 for 3 decimal places
    private final SyncedInt avgInsertion;

    public ReactorControllerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ReactorControllerMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.REACTOR_CONTROLLER.get(), containerId);
        this.controller = (ReactorControllerBlockEntity) entity;
        this.level = inv.player.level();

        this.active = syncInt(() -> controller.getSimulation().isActive() ? 1 : 0);
        this.mode = syncInt(() -> controller.getSimulation().getMode() == ReactorMode.ACTIVE ? 1 : 0);
        this.energy = syncInt(() -> controller.getEnergyStorage().getEnergyStored());
        this.maxEnergy = syncInt(() -> controller.getEnergyStorage().getMaxEnergyStored());
        this.generatedFe = syncInt(() -> controller.getSimulation().getLastGeneratedFe());
        this.coolantMb = syncInt(() -> controller.getCoolantTank().getFluidAmount());
        this.steamMb = syncInt(() -> controller.getSteamTank().getFluidAmount());
        this.maxFluidMb = syncInt(() -> controller.getCoolantTank().getCapacity());
        this.lastSteamMb = syncInt(() -> controller.getSimulation().getLastSteamProducedMb());
        this.fuelMb = syncInt(() -> (int) Math.round(controller.getSimulation().getFuelMb()));
        this.wasteMb = syncInt(() -> (int) Math.round(controller.getSimulation().getWasteMb()));
        this.maxFuelMb = syncInt(controller::getTotalFuelCapacityMb);
        this.fuelHeat = syncInt(() -> (int) Math.round(controller.getSimulation().getFuelHeat()));
        this.casingHeat = syncInt(() -> (int) Math.round(controller.getSimulation().getCasingHeat()));
        this.burnRateMilliMb = syncInt(() -> (int) Math.round(controller.getSimulation().getLastFuelBurnedMb() * 1000.0));
        this.avgInsertion = syncInt(controller::getAverageControlRodInsertionInt);
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
            case BTN_EJECT_WASTE -> controller.ejectWaste();
            case BTN_RODS_MINUS_10 -> controller.setAllControlRods(controller.getAverageControlRodInsertionInt() - 10);
            case BTN_RODS_PLUS_10 -> controller.setAllControlRods(controller.getAverageControlRodInsertionInt() + 10);
            default -> {
                return false;
            }
        }
        return true;
    }

    public boolean isActive() {
        return active.get() != 0;
    }

    public boolean isActiveCooled() {
        return mode.get() != 0;
    }

    public int getEnergy() {
        return energy.get();
    }

    public int getMaxEnergy() {
        return Math.max(1, maxEnergy.get());
    }

    public int getGeneratedFe() {
        return generatedFe.get();
    }

    public int getCoolantMb() {
        return coolantMb.get();
    }

    public int getSteamMb() {
        return steamMb.get();
    }

    public int getMaxFluidMb() {
        return Math.max(1, maxFluidMb.get());
    }

    public int getLastSteamMb() {
        return lastSteamMb.get();
    }

    public int getFuelMb() {
        return fuelMb.get();
    }

    public int getWasteMb() {
        return wasteMb.get();
    }

    public int getMaxFuelMb() {
        return Math.max(1, maxFuelMb.get());
    }

    public int getFuelHeat() {
        return fuelHeat.get();
    }

    public int getCasingHeat() {
        return casingHeat.get();
    }

    public double getBurnRateMb() {
        return burnRateMilliMb.get() / 1000.0;
    }

    public int getAvgInsertion() {
        return avgInsertion.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, controller.getBlockPos()),
                player, ModBlocks.REACTOR_CONTROLLER.get());
    }
}
