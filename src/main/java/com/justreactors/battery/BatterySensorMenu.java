package com.justreactors.battery;

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

public class BatterySensorMenu extends AbstractContainerMenu {
    public static final int BTN_PREV_MODE = 0;
    public static final int BTN_NEXT_MODE = 1;
    public static final int BTN_MINUS_10 = 2;
    public static final int BTN_MINUS_5 = 3;
    public static final int BTN_PLUS_5 = 4;
    public static final int BTN_PLUS_10 = 5;

    public final BatteryRedstoneSensorBlockEntity sensor;
    private final Level level;
    private final DataSlot mode = DataSlot.standalone();
    private final DataSlot threshold = DataSlot.standalone();
    private final DataSlot signal = DataSlot.standalone();
    private final DataSlot charge = DataSlot.standalone();

    public BatterySensorMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BatterySensorMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(ModMenus.BATTERY_SENSOR.get(), containerId);
        this.sensor = (BatteryRedstoneSensorBlockEntity) entity;
        this.level = inv.player.level();
        addDataSlot(mode);
        addDataSlot(threshold);
        addDataSlot(signal);
        addDataSlot(charge);
    }

    @Override
    public void broadcastChanges() {
        mode.set(sensor.getMode().ordinal());
        threshold.set(sensor.getThresholdPercent());
        signal.set(sensor.getSignal());
        BatteryControllerBlockEntity c = sensor.getController();
        charge.set(c == null ? -1 : (int) Math.floor(100.0 * c.getEnergyStorage().getEnergyStored()
                / Math.max(1, c.getEnergyStorage().getMaxEnergyStored())));
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case BTN_PREV_MODE -> sensor.setMode(sensor.getMode().prev());
            case BTN_NEXT_MODE -> sensor.setMode(sensor.getMode().next());
            case BTN_MINUS_10 -> sensor.adjustThreshold(-10);
            case BTN_MINUS_5 -> sensor.adjustThreshold(-5);
            case BTN_PLUS_5 -> sensor.adjustThreshold(5);
            case BTN_PLUS_10 -> sensor.adjustThreshold(10);
            default -> {
                return false;
            }
        }
        return true;
    }

    public BatterySensorMode getMode() {
        return BatterySensorMode.byOrdinal(mode.get());
    }

    public int getThresholdPercent() {
        return threshold.get();
    }

    public int getSignal() {
        return signal.get();
    }

    /** Battery charge in percent, or -1 when the sensor is not part of an assembled battery. */
    public int getChargePercent() {
        return charge.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, sensor.getBlockPos()), player, ModBlocks.BATTERY_REDSTONE_SENSOR.get());
    }
}
