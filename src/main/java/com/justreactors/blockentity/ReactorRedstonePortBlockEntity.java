package com.justreactors.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.justreactors.block.ReactorRedstonePortBlock;
import com.justreactors.menu.ReactorRedstonePortMenu;
import com.justreactors.reactor.ReactorMode;
import com.justreactors.reactor.RedstonePortMode;
import com.justreactors.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Configurable redstone automation port for Just Reactors:
 * supports ON/OFF toggle, analog Control Rod control, and threshold-based comparator outputs.
 */
public class ReactorRedstonePortBlockEntity extends ReactorPartBlockEntity implements MenuProvider {
    private RedstonePortMode mode = RedstonePortMode.TOGGLE_ON_SIGNAL;
    private int thresholdPercent = 50;
    private int emittedSignal = 0;
    private int lastInputSignal = 0;

    public ReactorRedstonePortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_REDSTONE_PORT.get(), pos, state);
    }

    public void onRedstoneChanged(boolean powered) {
        onRedstoneInput(powered ? 15 : 0);
    }

    /**
     * Called by the block on neighbor updates. Only reacts when the input signal actually changes, so a
     * neighbor update without a signal change (e.g. the controller toggling its own block state) does not
     * switch the machine off.
     */
    public void onNeighborSignal(int signal) {
        if (Math.max(0, Math.min(15, signal)) != lastInputSignal) onRedstoneInput(signal);
    }

    public void onRedstoneInput(int signal) {
        int clamped = Math.max(0, Math.min(15, signal));
        this.lastInputSignal = clamped;
        if (mode.isOutput()) return;

        ReactorControllerBlockEntity controller = getController();
        if (controller == null) return;

        switch (mode) {
            case TOGGLE_ON_SIGNAL -> controller.setActive(clamped > 0);
            case CONTROL_ROD_SIGNAL -> controller.setAllControlRods((int) Math.round(clamped * 100.0 / 15.0));
            default -> {
            }
        }
    }

    public void tickOutput(ReactorControllerBlockEntity controller) {
        if (level == null || level.isClientSide()) return;
        if (!mode.isOutput()) {
            if (emittedSignal != 0) {
                setEmittedSignal(0);
            }
            return;
        }

        boolean conditionMet = evaluateCondition(controller);
        setEmittedSignal(conditionMet ? 15 : 0);
    }

    private boolean evaluateCondition(ReactorControllerBlockEntity controller) {
        return switch (mode) {
            case OUTPUT_ENERGY_BELOW -> bufferPercent(controller) < thresholdPercent;
            case OUTPUT_ENERGY_ABOVE -> bufferPercent(controller) >= thresholdPercent;
            case OUTPUT_FUEL_TEMP_ABOVE -> controller.getSimulation().getFuelHeat() >= thresholdPercent * 20.0;
            case OUTPUT_FUEL_BELOW -> {
                int maxFuel = Math.max(1, controller.getTotalFuelCapacityMb());
                double fuelPct = (controller.getSimulation().getFuelMb() / maxFuel) * 100.0;
                yield fuelPct < thresholdPercent;
            }
            default -> false;
        };
    }

    private static double bufferPercent(ReactorControllerBlockEntity controller) {
        if (controller.getSimulation().getMode() == ReactorMode.ACTIVE) {
            int maxSteam = Math.max(1, controller.getSteamTank().getCapacity());
            return (controller.getSteamTank().getFluidAmount() * 100.0) / maxSteam;
        }
        int maxFe = Math.max(1, controller.getEnergyStorage().getMaxEnergyStored());
        return (controller.getEnergyStorage().getEnergyStored() * 100.0) / maxFe;
    }

    private void setEmittedSignal(int next) {
        if (emittedSignal == next) return;
        emittedSignal = next;
        updatePoweredState(next > 0);
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        setChanged();
    }

    private void updatePoweredState(boolean powered) {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (state.getBlock() == getBlockState().getBlock()
                && state.hasProperty(ReactorRedstonePortBlock.POWERED)
                && state.getValue(ReactorRedstonePortBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(ReactorRedstonePortBlock.POWERED, powered), 3);
        }
    }

    public RedstonePortMode getMode() {
        return mode;
    }

    public void setMode(RedstonePortMode newMode) {
        if (this.mode == newMode) return;
        this.mode = newMode;
        setChanged();
        if (level != null && !level.isClientSide()) {
            if (newMode.isOutput()) {
                ReactorControllerBlockEntity ctrl = getController();
                if (ctrl != null) {
                    tickOutput(ctrl);
                } else {
                    setEmittedSignal(0);
                }
            } else {
                setEmittedSignal(0);
                int sig = level.getBestNeighborSignal(worldPosition);
                updatePoweredState(sig > 0);
                onRedstoneInput(sig);
            }
        }
    }

    public int getThresholdPercent() {
        return thresholdPercent;
    }

    public void setThresholdPercent(int thresholdPercent) {
        int clamped = Math.max(0, Math.min(100, thresholdPercent));
        if (this.thresholdPercent != clamped) {
            this.thresholdPercent = clamped;
            setChanged();
            ReactorControllerBlockEntity ctrl = getController();
            if (ctrl != null && level != null && !level.isClientSide()) {
                tickOutput(ctrl);
            }
        }
    }

    public void adjustThreshold(int delta) {
        setThresholdPercent(this.thresholdPercent + delta);
    }

    public int getEmittedSignal() {
        return emittedSignal;
    }

    public int getCurrentDisplaySignal() {
        return mode.isOutput() ? emittedSignal : lastInputSignal;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.reactor_redstone_port");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReactorRedstonePortMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        output.putInt("Mode", mode.ordinal());
        output.putInt("Threshold", thresholdPercent);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);
        this.mode = RedstonePortMode.byOrdinal(input.getIntOr("Mode", 0));
        this.thresholdPercent = Math.max(0, Math.min(100, input.getIntOr("Threshold", 50)));
    }
}
