package com.justreactors.exchanger;

import com.justreactors.JustReactors;
import com.justreactors.JustReactorsConfig;
import com.justreactors.multiblock.BoxControllerBlockEntity;
import com.justreactors.multiblock.BoxValidator;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Heat Exchanger: cools the reactor's Hot Coolant back into Coolant and uses that heat to boil Water into Steam.
 * Each interior Heat Exchanger Tube adds throughput.
 */
public class HeatExchangerControllerBlockEntity extends BoxControllerBlockEntity {
    public static final BoxValidator.Spec SPEC = new BoxValidator.Spec("heat_exchanger",
            ModBlocks.HEAT_EXCHANGER_CASING, ModBlocks.HEAT_EXCHANGER_CONTROLLER,
            List.of(ModBlocks.HEAT_EXCHANGER_CASING, ModBlocks.HEAT_EXCHANGER_GLASS, ModBlocks.HEAT_EXCHANGER_CONTROLLER,
                    ModBlocks.HEAT_EXCHANGER_FLUID_PORT),
            ModBlocks.HEAT_EXCHANGER_TUBE, 3, () -> JustReactorsConfig.get(JustReactorsConfig.MAX_EXCHANGER_SIZE));

    private static final int BASE_TANK_MB = 16_000;
    private static final int TANK_PER_TUBE_MB = 2_000;
    private static final int EJECT_RATE_MB = 10_000;

    private final ReactorFluidTank hotTank;
    private final ReactorFluidTank coolantTank;
    private final ReactorFluidTank waterTank;
    private final ReactorFluidTank steamTank;
    private boolean active = true;
    private int tubes = 0;
    private int lastCooledMb = 0;
    private int lastSteamMb = 0;

    public HeatExchangerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_EXCHANGER_CONTROLLER.get(), pos, state);
        this.hotTank = new ReactorFluidTank(BASE_TANK_MB, s -> ModFluids.isHotCoolant(s.getFluid()), this::setChanged);
        this.coolantTank = new ReactorFluidTank(BASE_TANK_MB, s -> ModFluids.isCoolant(s.getFluid()), this::setChanged);
        this.waterTank = new ReactorFluidTank(BASE_TANK_MB, s -> s.is(Fluids.WATER), this::setChanged);
        this.steamTank = new ReactorFluidTank(BASE_TANK_MB, s -> s.is(ModFluids.STEAM_SOURCE.get()), this::setChanged);
    }

    @Override
    protected BoxValidator.Spec spec() {
        return SPEC;
    }

    @Override
    protected void onAssembled(BoxValidator.Scan scan, boolean firstTime) {
        tubes = scan.core().size();
        int cap = BASE_TANK_MB + TANK_PER_TUBE_MB * tubes;
        hotTank.setCapacity(cap);
        coolantTank.setCapacity(cap);
        waterTank.setCapacity(cap);
        steamTank.setCapacity(cap);
    }

    @Override
    protected void onDisassembled() {
        lastCooledMb = 0;
        lastSteamMb = 0;
    }

    /** Maximum Hot Coolant this exchanger can cool per tick. */
    public int getMaxCoolingMb() {
        return tubes * JustReactorsConfig.get(JustReactorsConfig.EXCHANGER_HOT_COOLANT_PER_TUBE);
    }

    public static int steamPerHotCoolant() {
        return JustReactorsConfig.get(JustReactorsConfig.EXCHANGER_STEAM_PER_HOT_COOLANT);
    }

    @Override
    protected boolean tickAssembled(Level level, BoxValidator.Scan scan) {
        int k = steamPerHotCoolant();
        int cooled = 0;
        if (active) {
            cooled = Math.min(hotTank.getFluidAmount(), getMaxCoolingMb());
            cooled = Math.min(cooled, coolantTank.getSpace());
            cooled = Math.min(cooled, waterTank.getFluidAmount() / k);
            cooled = Math.min(cooled, steamTank.getSpace() / k);
        }
        if (cooled > 0) {
            hotTank.drainInternal(cooled);
            coolantTank.fillInternal(new FluidStack(ModFluids.COOLANT_SOURCE.get(), cooled));
            waterTank.drainInternal(cooled * k);
            steamTank.fillInternal(new FluidStack(ModFluids.STEAM_SOURCE.get(), cooled * k));
        }
        lastCooledMb = cooled;
        lastSteamMb = cooled * k;

        for (BlockPos p : scan.parts(ModBlocks.HEAT_EXCHANGER_FLUID_PORT.get())) {
            if (level.getBlockEntity(p) instanceof HeatExchangerFluidPortBlockEntity port) {
                switch (port.getMode()) {
                    case OUTPUT_COOLANT -> port.pushFluidToNeighbors(coolantTank, EJECT_RATE_MB);
                    case OUTPUT_STEAM -> port.pushFluidToNeighbors(steamTank, EJECT_RATE_MB);
                    default -> {
                    }
                }
            }
        }
        return cooled > 0;
    }

    @Override
    public int getComparatorSignal() {
        if (!isAssembled() || hotTank.getCapacity() <= 0) return 0;
        // Hot Coolant backlog: high signal means the exchanger is falling behind the reactor.
        return (int) Math.ceil(15.0 * hotTank.getFluidAmount() / hotTank.getCapacity());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            setChanged();
        }
    }

    public int getTubes() {
        return tubes;
    }

    public int getLastCooledMb() {
        return lastCooledMb;
    }

    public int getLastSteamMb() {
        return lastSteamMb;
    }

    public ReactorFluidTank getHotTank() {
        return hotTank;
    }

    public ReactorFluidTank getCoolantTank() {
        return coolantTank;
    }

    public ReactorFluidTank getWaterTank() {
        return waterTank;
    }

    public ReactorFluidTank getSteamTank() {
        return steamTank;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block." + JustReactors.MOD_ID + ".heat_exchanger_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HeatExchangerMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        hotTank.save(output, "HotTank");
        coolantTank.save(output, "CoolantTank");
        waterTank.save(output, "WaterTank");
        steamTank.save(output, "SteamTank");
        output.putBoolean("Active", active);
        output.putInt("Tubes", tubes);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // Restore capacity first so saved amounts above the base capacity are not clamped.
        tubes = Math.max(0, input.getIntOr("Tubes", 0));
        int cap = BASE_TANK_MB + TANK_PER_TUBE_MB * tubes;
        hotTank.setCapacity(cap);
        coolantTank.setCapacity(cap);
        waterTank.setCapacity(cap);
        steamTank.setCapacity(cap);
        hotTank.load(input, "HotTank");
        coolantTank.load(input, "CoolantTank");
        waterTank.load(input, "WaterTank");
        steamTank.load(input, "SteamTank");
        active = input.getBooleanOr("Active", true);
    }
}
