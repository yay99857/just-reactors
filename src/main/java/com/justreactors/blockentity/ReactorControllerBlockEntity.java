package com.justreactors.blockentity;

import com.justreactors.JustReactorsConfig;
import com.justreactors.block.ReactorControllerBlock;
import com.justreactors.client.ClientMachineSounds;
import com.justreactors.menu.ReactorControllerMenu;
import com.justreactors.reactor.ReactorMode;
import com.justreactors.reactor.ReactorSimulation;
import com.justreactors.reactor.ReactorStructure;
import com.justreactors.reactor.ReactorValidator;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorEnergyStorage;
import com.justreactors.transfer.ReactorFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Brain of a Just Reactors multiblock. Owns the simulation state, energy buffer,
 * coolant/steam tanks, periodic structure validation, and coordinates linked ports and control rods.
 */
public class ReactorControllerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int VALIDATION_INTERVAL_TICKS = 20;
    private static final int STEAM_EJECT_RATE_MB = 10_000;

    private final ReactorSimulation simulation = new ReactorSimulation();
    private final ReactorEnergyStorage energyStorage;
    private final ReactorFluidTank coolantTank;
    private final ReactorFluidTank steamTank;

    @Nullable
    private ReactorStructure structure;
    @Nullable
    private Component lastValidationError;
    private int tickCounter = 0;

    // Server sync tracking
    private boolean lastSyncedAssembled = false;
    private boolean lastSyncedActive = false;
    private float lastSyncedFuelRatio = -1.0F;
    private float lastSyncedWasteRatio = -1.0F;

    // Client render & sound state
    private boolean clientAssembled = false;
    private boolean clientActive = false;
    @Nullable
    private BlockPos clientMin;
    @Nullable
    private BlockPos clientMax;
    private float clientFuelRatio = 0.0F;
    private float clientWasteRatio = 0.0F;
    private int[] clientFuelRodOffsets = new int[0];

    public ReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROLLER.get(), pos, state);
        this.energyStorage = new ReactorEnergyStorage(
                JustReactorsConfig.get(JustReactorsConfig.ENERGY_BUFFER_CAPACITY),
                JustReactorsConfig.get(JustReactorsConfig.POWER_TAP_MAX_EXTRACT),
                this::setChanged
        );
        int defaultTank = Math.max(10_000, JustReactorsConfig.get(JustReactorsConfig.COOLANT_TANK_PER_ROD_MB));
        this.coolantTank = new ReactorFluidTank(defaultTank, stack -> stack.is(Fluids.WATER), this::setChanged);
        this.steamTank = new ReactorFluidTank(defaultTank, stack -> stack.is(ModFluids.STEAM_SOURCE.get()), this::setChanged);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ReactorControllerBlockEntity be) {
        ClientMachineSounds.tickReactorSound(be);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ReactorControllerBlockEntity controller) {
        controller.tickCounter++;

        // Periodic validation catches blocks added/broken anywhere in the multiblock or interior moderators.
        if (controller.structure == null || controller.tickCounter % VALIDATION_INTERVAL_TICKS == 0) {
            controller.tryAssemble();
        }

        if (controller.structure == null) {
            controller.updateBlockState(level, pos, state, false, false);
            controller.syncClientRenderData(level, pos, level.getBlockState(pos));
            return;
        }

        ReactorStructure s = controller.structure;
        int fuelCapacityMb = controller.getTotalFuelCapacityMb();

        // 1. Pull fuel from linked Access Ports
        controller.pullFuelFromPorts(level, s, fuelCapacityMb);

        // 2. Average control rod insertion
        double avgInsertion = controller.getAverageControlRodInsertion(level, s);

        // 3. Run ER2-style fission simulation
        ReactorSimulation.TickOutput output = controller.simulation.tick(
                s,
                avgInsertion,
                controller.energyStorage.getSpace(),
                controller.coolantTank.getFluidAmount(),
                controller.steamTank.getSpace(),
                JustReactorsConfig.get(JustReactorsConfig.POWER_PRODUCTION_MULTIPLIER),
                JustReactorsConfig.get(JustReactorsConfig.STEAM_PRODUCTION_MULTIPLIER),
                JustReactorsConfig.get(JustReactorsConfig.FUEL_USAGE_MULTIPLIER)
        );
        if (output.generatedFe() > 0) {
            controller.energyStorage.generateEnergy(output.generatedFe());
        }
        if (output.steamProducedMb() > 0) {
            controller.coolantTank.drainInternal(output.steamProducedMb());
            controller.steamTank.fillInternal(new FluidStack(ModFluids.STEAM_SOURCE.get(), output.steamProducedMb()));
        }

        // 4. Push FE out through all linked Power Taps (Passive mode)
        if (controller.energyStorage.getEnergyStored() > 0) {
            int maxPerTap = JustReactorsConfig.get(JustReactorsConfig.POWER_TAP_MAX_EXTRACT);
            for (BlockPos tapPos : s.powerTaps()) {
                if (controller.energyStorage.getEnergyStored() <= 0) break;
                if (level.getBlockEntity(tapPos) instanceof ReactorPowerTapBlockEntity tap) {
                    tap.pushEnergyToNeighbors(controller.energyStorage, maxPerTap);
                }
            }
        }

        // 5. Push Steam out through all linked Outlet Coolant Ports (Active mode)
        if (!controller.steamTank.isEmpty()) {
            for (BlockPos portPos : s.coolantPorts()) {
                if (controller.steamTank.isEmpty()) break;
                if (level.getBlockEntity(portPos) instanceof ReactorCoolantPortBlockEntity port && port.isOutlet()) {
                    port.pushSteamToNeighbors(controller.steamTank, STEAM_EJECT_RATE_MB);
                }
            }
        }

        // 6. Auto-eject waste ingots into linked Access Ports
        if (controller.simulation.getWasteMb() >= ReactorSimulation.MB_PER_INGOT) {
            controller.pushWasteToPorts(level, s, false);
        }

        // 7. Update output Redstone Ports
        for (BlockPos rpPos : s.redstonePorts()) {
            if (level.getBlockEntity(rpPos) instanceof ReactorRedstonePortBlockEntity rp) {
                rp.tickOutput(controller);
            }
        }

        boolean visuallyActive = controller.simulation.isActive() && controller.simulation.getFuelMb() > 0.0001;
        controller.updateBlockState(level, pos, state, true, visuallyActive);
        controller.syncClientRenderData(level, pos, level.getBlockState(pos));

        if (output.generatedFe() > 0 || output.steamProducedMb() > 0 || output.fuelBurnedMb() > 0.0) {
            controller.setChanged();
        }
    }

    private void syncClientRenderData(Level level, BlockPos pos, BlockState state) {
        boolean assembled = this.structure != null;
        boolean active = assembled && this.simulation.isActive() && this.simulation.getFuelMb() > 0.0001;
        int maxFuel = Math.max(1, getTotalFuelCapacityMb());
        float fuelRatio = assembled ? (float) Math.max(0.0, Math.min(1.0, this.simulation.getFuelMb() / maxFuel)) : 0.0F;
        float wasteRatio = assembled ? (float) Math.max(0.0, Math.min(1.0, this.simulation.getWasteMb() / maxFuel)) : 0.0F;

        boolean stateChanged = assembled != lastSyncedAssembled || active != lastSyncedActive;
        boolean fillChanged = (tickCounter % 10 == 0)
                && (Math.abs(fuelRatio - lastSyncedFuelRatio) >= 0.01F
                || Math.abs(wasteRatio - lastSyncedWasteRatio) >= 0.01F);

        if (stateChanged || fillChanged) {
            lastSyncedAssembled = assembled;
            lastSyncedActive = active;
            lastSyncedFuelRatio = fuelRatio;
            lastSyncedWasteRatio = wasteRatio;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public boolean tryAssemble() {
        if (level == null || level.isClientSide()) return false;
        ReactorValidator.Result result = ReactorValidator.validate(level, worldPosition);
        if (result instanceof ReactorValidator.Result.Valid valid) {
            ReactorStructure s = valid.structure();
            this.structure = s;
            this.lastValidationError = null;
            this.simulation.setMode(s.coolantPorts().isEmpty() ? ReactorMode.PASSIVE : ReactorMode.ACTIVE);
            int tankCap = Math.max(10_000, s.fuelRodCount() * JustReactorsConfig.get(JustReactorsConfig.COOLANT_TANK_PER_ROD_MB));
            this.coolantTank.setCapacity(tankCap);
            this.steamTank.setCapacity(tankCap);
            linkParts(s);
            syncClientRenderData(level, worldPosition, getBlockState());
            return true;
        } else if (result instanceof ReactorValidator.Result.Invalid invalid) {
            unlinkParts();
            this.structure = null;
            this.lastValidationError = invalid.reason();
            syncClientRenderData(level, worldPosition, getBlockState());
            return false;
        }
        return false;
    }

    private void linkParts(ReactorStructure s) {
        if (level == null) return;
        for (BlockPos p : s.controlRods()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(worldPosition);
        }
        for (BlockPos p : s.powerTaps()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(worldPosition);
        }
        for (BlockPos p : s.accessPorts()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(worldPosition);
        }
        for (BlockPos p : s.redstonePorts()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(worldPosition);
        }
        for (BlockPos p : s.coolantPorts()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(worldPosition);
        }
    }

    private void unlinkParts() {
        if (level == null || structure == null) return;
        for (BlockPos p : structure.controlRods()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(null);
        }
        for (BlockPos p : structure.powerTaps()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(null);
        }
        for (BlockPos p : structure.accessPorts()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(null);
        }
        for (BlockPos p : structure.redstonePorts()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(null);
        }
        for (BlockPos p : structure.coolantPorts()) {
            if (level.getBlockEntity(p) instanceof ReactorPartBlockEntity part) part.setControllerPos(null);
        }
    }

    private void pullFuelFromPorts(Level level, ReactorStructure s, int fuelCapacityMb) {
        double spaceMb = fuelCapacityMb - (simulation.getFuelMb() + simulation.getWasteMb());
        if (spaceMb < ReactorSimulation.MB_PER_INGOT) return;
        int ingotsWanted = (int) Math.floor(spaceMb / ReactorSimulation.MB_PER_INGOT);
        for (BlockPos portPos : s.accessPorts()) {
            if (ingotsWanted <= 0) break;
            if (level.getBlockEntity(portPos) instanceof ReactorAccessPortBlockEntity port && !port.isOutlet()) {
                int pulled = port.consumeFuelIngots(ingotsWanted);
                if (pulled > 0) {
                    simulation.insertFuelMb(pulled * (double) ReactorSimulation.MB_PER_INGOT, fuelCapacityMb);
                    ingotsWanted -= pulled;
                    setChanged();
                }
            }
        }
    }

    public int pushWasteToPorts(Level level, ReactorStructure s, boolean allowAnyPort) {
        int availableIngots = (int) Math.floor(simulation.getWasteMb() / ReactorSimulation.MB_PER_INGOT);
        if (availableIngots <= 0) return 0;

        int totalEjected = 0;
        for (int pass = 0; pass < (allowAnyPort ? 2 : 1); pass++) {
            boolean wantOutlet = (pass == 0);
            for (BlockPos portPos : s.accessPorts()) {
                if (availableIngots <= 0) break;
                if (level.getBlockEntity(portPos) instanceof ReactorAccessPortBlockEntity port) {
                    if (wantOutlet && !port.isOutlet()) continue;
                    if (!wantOutlet && port.isOutlet()) continue;
                    int accepted = port.insertWasteIngots(availableIngots);
                    if (accepted > 0) {
                        simulation.extractWasteIngots(accepted);
                        availableIngots -= accepted;
                        totalEjected += accepted;
                        setChanged();
                    }
                }
            }
        }
        return totalEjected;
    }

    public double getAverageControlRodInsertion(Level level, ReactorStructure s) {
        if (s.controlRods().isEmpty()) return 0.0;
        int sum = 0;
        int count = 0;
        for (BlockPos rodPos : s.controlRods()) {
            if (level.getBlockEntity(rodPos) instanceof ReactorControlRodBlockEntity rod) {
                sum += rod.getInsertion();
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) sum / count;
    }

    public void setAllControlRods(int insertionPercent) {
        if (level == null || structure == null) return;
        int clamped = Math.max(0, Math.min(100, insertionPercent));
        for (BlockPos rodPos : structure.controlRods()) {
            if (level.getBlockEntity(rodPos) instanceof ReactorControlRodBlockEntity rod) {
                rod.setInsertion(clamped);
            }
        }
        setChanged();
    }

    public void ejectWaste() {
        ejectWasteAndCount();
    }

    public int ejectWasteAndCount() {
        if (level != null && structure != null) {
            return pushWasteToPorts(level, structure, true);
        }
        return 0;
    }

    public void toggleActive() {
        setActive(!simulation.isActive());
    }

    public void setActive(boolean active) {
        if (simulation.isActive() != active) {
            simulation.setActive(active);
            setChanged();
        }
    }

    private void updateBlockState(Level level, BlockPos pos, BlockState state, boolean assembled, boolean active) {
        if (state.getValue(ReactorControllerBlock.ASSEMBLED) != assembled
                || state.getValue(ReactorControllerBlock.ACTIVE) != active) {
            level.setBlock(pos, state
                    .setValue(ReactorControllerBlock.ASSEMBLED, assembled)
                    .setValue(ReactorControllerBlock.ACTIVE, active), 3);
        }
    }

    public boolean isAssembled() {
        return structure != null;
    }

    @Nullable
    public ReactorStructure getStructure() {
        return structure;
    }

    @Nullable
    public Component getLastValidationError() {
        return lastValidationError;
    }

    public ReactorSimulation getSimulation() {
        return simulation;
    }

    public ReactorEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public ReactorFluidTank getCoolantTank() {
        return coolantTank;
    }

    public ReactorFluidTank getSteamTank() {
        return steamTank;
    }

    public int getTotalFuelCapacityMb() {
        if (structure == null) return ReactorSimulation.MB_PER_INGOT;
        return simulation.getTotalFuelCapacityMb(structure, JustReactorsConfig.get(JustReactorsConfig.FUEL_CAPACITY_PER_ROD_MB));
    }

    public int getAverageControlRodInsertionInt() {
        if (level == null || structure == null) return 0;
        return (int) Math.round(getAverageControlRodInsertion(level, structure));
    }

    public boolean isClientAssembled() {
        return clientAssembled;
    }

    public boolean isClientActive() {
        return clientActive;
    }

    @Nullable
    public BlockPos getClientMin() {
        return clientMin;
    }

    @Nullable
    public BlockPos getClientMax() {
        return clientMax;
    }

    public float getClientFuelRatio() {
        return clientFuelRatio;
    }

    public float getClientWasteRatio() {
        return clientWasteRatio;
    }

    public int[] getClientFuelRodOffsets() {
        return clientFuelRodOffsets;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.reactor_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReactorControllerMenu(containerId, playerInventory, this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = new ValueOutput(tag, registries);
        energyStorage.serialize(output);
        coolantTank.save(output, "CoolantTank");
        steamTank.save(output, "SteamTank");
        output.putBoolean("Active", simulation.isActive());
        output.putDouble("FuelMb", simulation.getFuelMb());
        output.putDouble("WasteMb", simulation.getWasteMb());
        output.putDouble("FuelHeat", simulation.getFuelHeat());
        output.putDouble("CasingHeat", simulation.getCasingHeat());
        output.putDouble("Fertility", simulation.getFuelFertility());

        boolean assembled = structure != null;
        output.putBoolean("SyncAssembled", assembled);
        output.putBoolean("SyncActive", assembled && simulation.isActive() && simulation.getFuelMb() > 0.0001);
        int maxFuel = Math.max(1, getTotalFuelCapacityMb());
        output.putDouble("SyncFuelRatio", assembled ? Math.max(0.0, Math.min(1.0, simulation.getFuelMb() / maxFuel)) : 0.0);
        output.putDouble("SyncWasteRatio", assembled ? Math.max(0.0, Math.min(1.0, simulation.getWasteMb() / maxFuel)) : 0.0);
        if (structure != null) {
            output.putLong("SyncMin", structure.min().asLong());
            output.putLong("SyncMax", structure.max().asLong());
            List<BlockPos> rods = structure.fuelRodPositions();
            int[] offsets = new int[rods.size() * 3];
            for (int i = 0; i < rods.size(); i++) {
                BlockPos rp = rods.get(i);
                offsets[i * 3] = rp.getX() - worldPosition.getX();
                offsets[i * 3 + 1] = rp.getY() - worldPosition.getY();
                offsets[i * 3 + 2] = rp.getZ() - worldPosition.getZ();
            }
            output.putIntArray("SyncFuelRods", offsets);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        energyStorage.deserialize(input);
        coolantTank.load(input, "CoolantTank");
        steamTank.load(input, "SteamTank");
        simulation.setActive(input.getBooleanOr("Active", false));
        simulation.setFuelMb(input.getDoubleOr("FuelMb", 0.0));
        simulation.setWasteMb(input.getDoubleOr("WasteMb", 0.0));
        simulation.setFuelHeat(input.getDoubleOr("FuelHeat", ReactorSimulation.AMBIENT_TEMP));
        simulation.setCasingHeat(input.getDoubleOr("CasingHeat", ReactorSimulation.AMBIENT_TEMP));
        simulation.setFuelFertility(input.getDoubleOr("Fertility", 1.0));

        this.clientAssembled = input.getBooleanOr("SyncAssembled", false);
        this.clientActive = input.getBooleanOr("SyncActive", false);
        this.clientMin = input.getLong("SyncMin").map(BlockPos::of).orElse(null);
        this.clientMax = input.getLong("SyncMax").map(BlockPos::of).orElse(null);
        this.clientFuelRatio = (float) input.getDoubleOr("SyncFuelRatio", 0.0);
        this.clientWasteRatio = (float) input.getDoubleOr("SyncWasteRatio", 0.0);
        this.clientFuelRodOffsets = input.getIntArray("SyncFuelRods").orElse(new int[0]);
    }
}
