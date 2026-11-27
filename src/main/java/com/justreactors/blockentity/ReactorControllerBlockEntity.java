package com.justreactors.blockentity;

import net.minecraft.world.phys.AABB;
import com.justreactors.JustReactorsConfig;
import com.justreactors.multiblock.AssembledMultiblocks;
import com.justreactors.multiblock.MultiblockTrim;
import com.justreactors.block.ReactorControllerBlock;
import com.justreactors.block.ReactorFuelRodBlock;
import com.justreactors.client.ClientMachineSounds;
import com.justreactors.menu.ReactorControllerMenu;
import com.justreactors.reactor.ReactorMode;
import com.justreactors.reactor.ReactorSimulation;
import com.justreactors.reactor.ReactorStructure;
import com.justreactors.reactor.ReactorValidator;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorEnergyStorage;
import com.justreactors.transfer.ReactorFluidTank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.fluids.FluidStack;
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
    /** Heat (in mB of steam) not yet turned into a whole mB of Hot Coolant. */
    private int coolantRemainder = 0;
    private int lastHotCoolantMb = 0;

    // Server sync tracking
    private boolean lastSyncedAssembled = false;
    private boolean lastSyncedActive = false;
    private float lastSyncedFuelRatio = -1.0F;
    private float lastSyncedWasteRatio = -1.0F;
    private float lastSyncedHeat = -1.0F;
    private float lastSyncedActivity = -1.0F;
    private boolean visualsApplied = false;
    private boolean lastAppliedActive = false;

    // Client render & sound state
    private boolean clientAssembled = false;
    private boolean clientActive = false;
    @Nullable
    private BlockPos clientMin;
    @Nullable
    private BlockPos clientMax;
    private float clientFuelRatio = 0.0F;
    private float clientWasteRatio = 0.0F;
    private float clientFuelHeat = (float) ReactorSimulation.AMBIENT_TEMP;
    private float clientActivity = 0.0F;
    private int[] clientFuelRodOffsets = new int[0];
    private int[] clientControlRodOffsets = new int[0];

    public ReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROLLER.get(), pos, state);
        this.energyStorage = new ReactorEnergyStorage(
                JustReactorsConfig.get(JustReactorsConfig.ENERGY_BUFFER_CAPACITY),
                JustReactorsConfig.get(JustReactorsConfig.POWER_TAP_MAX_EXTRACT),
                this::setChanged
        );
        int defaultTank = Math.max(10_000, JustReactorsConfig.get(JustReactorsConfig.COOLANT_TANK_PER_ROD_MB));
        // Coolant side holds Water (boiled to Steam) or reusable Coolant (heated to Hot Coolant).
        this.coolantTank = new ReactorFluidTank(defaultTank,
                stack -> stack.getFluid().isSame(Fluids.WATER) || ModFluids.isCoolant(stack.getFluid()), this::setChanged);
        this.steamTank = new ReactorFluidTank(defaultTank,
                stack -> stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get()) || ModFluids.isHotCoolant(stack.getFluid()), this::setChanged);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ReactorControllerBlockEntity be) {
        ClientMachineSounds.tickReactorSound(be);
        if (!be.clientAssembled || !be.clientActive) {
            return;
        }
        RandomSource rand = level.getRandom();
        if (be.clientFuelHeat >= 100.0F && be.clientControlRodOffsets.length >= 3) {
            float heatFrac = Math.min(1.0F, (be.clientFuelHeat - 100.0F) / 1400.0F);
            float smokeChance = 0.055F + 0.045F * heatFrac;
            if (rand.nextFloat() < smokeChance) {
                int idx = rand.nextInt(be.clientControlRodOffsets.length / 3) * 3;
                double px = pos.getX() + be.clientControlRodOffsets[idx] + 0.5 + (rand.nextDouble() - 0.5) * 0.22;
                double py = pos.getY() + be.clientControlRodOffsets[idx + 1] + 1.12;
                double pz = pos.getZ() + be.clientControlRodOffsets[idx + 2] + 0.5 + (rand.nextDouble() - 0.5) * 0.22;
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz,
                        (rand.nextDouble() - 0.5) * 0.006, 0.045, (rand.nextDouble() - 0.5) * 0.006);
            }
        }
        if (be.clientFuelHeat >= 220.0F && be.clientFuelRodOffsets.length >= 3 && rand.nextInt(20) == 0) {
            int idx = rand.nextInt(be.clientFuelRodOffsets.length / 3) * 3;
            double px = pos.getX() + be.clientFuelRodOffsets[idx] + 0.25 + rand.nextDouble() * 0.5;
            double py = pos.getY() + be.clientFuelRodOffsets[idx + 1] + 0.25 + rand.nextDouble() * 0.5;
            double pz = pos.getZ() + be.clientFuelRodOffsets[idx + 2] + 0.25 + rand.nextDouble() * 0.5;
            level.addParticle(ParticleTypes.GLOW, px, py, pz, 0.0, 0.01, 0.0);
        }
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

        // 3. Run ER2-style fission simulation. With Coolant, each mB carries `ratio` mB of steam worth of heat.
        boolean usingCoolant = controller.isUsingCoolant();
        int ratio = usingCoolant ? JustReactorsConfig.get(JustReactorsConfig.COOLANT_HEAT_RATIO) : 1;
        FluidStack hotOut = controller.steamTank.getFluid();
        boolean outputMatches = hotOut.isEmpty() || (usingCoolant
                ? ModFluids.isHotCoolant(hotOut.getFluid()) : hotOut.getFluid().isSame(ModFluids.STEAM_SOURCE.get()));
        int outputSpace = outputMatches ? controller.steamTank.getSpace() : 0;
        ReactorSimulation.TickOutput output = controller.simulation.tick(
                s,
                avgInsertion,
                controller.energyStorage.getSpace(),
                (int) Math.min(Integer.MAX_VALUE, (long) controller.coolantTank.getFluidAmount() * ratio),
                (int) Math.max(0, Math.min(Integer.MAX_VALUE,
                        (long) outputSpace * ratio - (usingCoolant ? controller.coolantRemainder : 0))),
                JustReactorsConfig.get(JustReactorsConfig.POWER_PRODUCTION_MULTIPLIER),
                JustReactorsConfig.get(JustReactorsConfig.STEAM_PRODUCTION_MULTIPLIER),
                JustReactorsConfig.get(JustReactorsConfig.FUEL_USAGE_MULTIPLIER)
        );
        if (output.generatedFe() > 0) {
            controller.energyStorage.generateEnergy(output.generatedFe());
        }
        controller.lastHotCoolantMb = 0;
        if (output.steamProducedMb() > 0) {
            if (usingCoolant) {
                // Heat is carried away in whole mB of Hot Coolant; the fraction is kept for the next tick.
                int heated = output.steamProducedMb() + controller.coolantRemainder;
                int mb = heated / ratio;
                controller.coolantRemainder = heated % ratio;
                controller.lastHotCoolantMb = mb;
                if (mb > 0) {
                    controller.coolantTank.drainInternal(mb);
                    controller.steamTank.fillInternal(new FluidStack(ModFluids.HOT_COOLANT_SOURCE.get(), mb));
                }
            } else {
                controller.coolantTank.drainInternal(output.steamProducedMb());
                controller.steamTank.fillInternal(new FluidStack(ModFluids.STEAM_SOURCE.get(), output.steamProducedMb()));
            }
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
        if (!controller.visualsApplied || visuallyActive != controller.lastAppliedActive) {
            controller.applyStructureVisuals(level, s, visuallyActive);
        }
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
        float fuelHeat = assembled ? (float) this.simulation.getFuelHeat() : (float) ReactorSimulation.AMBIENT_TEMP;
        float activity = active && this.structure != null
                ? (float) Math.max(0.0, 1.0 - getAverageControlRodInsertion(level, this.structure) / 100.0)
                : 0.0F;

        boolean stateChanged = assembled != lastSyncedAssembled || active != lastSyncedActive;
        boolean fillChanged = (tickCounter % 10 == 0)
                && (Math.abs(fuelRatio - lastSyncedFuelRatio) >= 0.01F
                || Math.abs(wasteRatio - lastSyncedWasteRatio) >= 0.01F
                || Math.abs(fuelHeat - lastSyncedHeat) >= 25.0F
                || Math.abs(activity - lastSyncedActivity) >= 0.05F);

        if (stateChanged || fillChanged) {
            lastSyncedAssembled = assembled;
            lastSyncedActive = active;
            lastSyncedFuelRatio = fuelRatio;
            lastSyncedWasteRatio = wasteRatio;
            lastSyncedHeat = fuelHeat;
            lastSyncedActivity = activity;
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
            boolean visuallyActive = this.simulation.isActive() && this.simulation.getFuelMb() > 0.0001;
            applyStructureVisuals(level, s, visuallyActive);
            AssembledMultiblocks.put(level, worldPosition, s.min(), s.max());
            syncClientRenderData(level, worldPosition, getBlockState());
            return true;
        } else if (result instanceof ReactorValidator.Result.Invalid invalid) {
            unlinkParts();
            if (this.structure != null) {
                resetStructureVisuals(level, this.structure);
            }
            this.structure = null;
            AssembledMultiblocks.remove(level, worldPosition);
            this.lastValidationError = invalid.reason();
            syncClientRenderData(level, worldPosition, getBlockState());
            return false;
        }
        return false;
    }

    private void applyStructureVisuals(Level level, ReactorStructure s, boolean active) {
        MultiblockTrim.apply(level, s.min(), s.max(), ModBlocks.REACTOR_CASING.get());
        for (BlockPos rp : s.fuelRodPositions()) {
            BlockState st = level.getBlockState(rp);
            if (st.getBlock() instanceof ReactorFuelRodBlock && st.getValue(ReactorFuelRodBlock.ACTIVE) != active) {
                level.setBlock(rp, st.setValue(ReactorFuelRodBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
            }
        }
        for (BlockPos crPos : s.controlRods()) {
            if (level.getBlockEntity(crPos) instanceof ReactorControlRodBlockEntity rod) {
                rod.syncBlockLevel();
            }
        }
        this.visualsApplied = true;
        this.lastAppliedActive = active;
    }

    private void resetStructureVisuals(Level level, ReactorStructure s) {
        MultiblockTrim.reset(level, s.min(), s.max(), ModBlocks.REACTOR_CASING.get());
        for (BlockPos rp : s.fuelRodPositions()) {
            BlockState st = level.getBlockState(rp);
            if (st.getBlock() instanceof ReactorFuelRodBlock && st.getValue(ReactorFuelRodBlock.ACTIVE)) {
                level.setBlock(rp, st.setValue(ReactorFuelRodBlock.ACTIVE, false), Block.UPDATE_CLIENTS);
            }
        }
        this.visualsApplied = false;
        this.lastAppliedActive = false;
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

    public int getLastHotCoolantMb() {
        return lastHotCoolantMb;
    }

    /** True while the reactor runs on the reusable Coolant loop (either tank holds coolant). */
    public boolean isCoolantLoop() {
        return isUsingCoolant() || isOutputHotCoolant();
    }

    /** True when the coolant tank holds reusable Coolant instead of Water. */
    public boolean isUsingCoolant() {
        return !coolantTank.isEmpty() && ModFluids.isCoolant(coolantTank.getFluid().getFluid());
    }

    /** True when the output tank holds Hot Coolant instead of Steam. */
    public boolean isOutputHotCoolant() {
        return !steamTank.isEmpty() && ModFluids.isHotCoolant(steamTank.getFluid().getFluid());
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

    public float getClientFuelHeat() {
        return clientFuelHeat;
    }

    public float getClientActivity() {
        return clientActivity;
    }

    public int[] getClientControlRodOffsets() {
        return clientControlRodOffsets;
    }

    /** Called from the block's onRemove (1.21.1 has no BlockEntity#preRemoveSideEffects). */
    public void onControllerRemoved() {
        if (level != null && !level.isClientSide() && structure != null) {
            resetStructureVisuals(level, structure);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null) AssembledMultiblocks.remove(level, worldPosition);
        super.setRemoved();
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
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        energyStorage.serialize(output);
        output.putInt("TankCapacity", coolantTank.getCapacity());
        coolantTank.save(output, "CoolantTank");
        output.putInt("CoolantRemainder", coolantRemainder);
        steamTank.save(output, "SteamTank");
        output.putBoolean("Active", simulation.isActive());
        output.putDouble("FuelMb", simulation.getFuelMb());
        output.putDouble("WasteMb", simulation.getWasteMb());
        output.putDouble("FuelHeat", simulation.getFuelHeat());
        output.putDouble("CasingHeat", simulation.getCasingHeat());
        output.putDouble("Fertility", simulation.getFuelFertility());

        boolean assembled = structure != null;
        boolean active = assembled && simulation.isActive() && simulation.getFuelMb() > 0.0001;
        output.putBoolean("SyncAssembled", assembled);
        output.putBoolean("SyncActive", active);
        int maxFuel = Math.max(1, getTotalFuelCapacityMb());
        output.putDouble("SyncFuelRatio", assembled ? Math.max(0.0, Math.min(1.0, simulation.getFuelMb() / maxFuel)) : 0.0);
        output.putDouble("SyncWasteRatio", assembled ? Math.max(0.0, Math.min(1.0, simulation.getWasteMb() / maxFuel)) : 0.0);
        output.putDouble("SyncFuelHeat", assembled ? simulation.getFuelHeat() : ReactorSimulation.AMBIENT_TEMP);
        double syncActivity = (active && level != null && structure != null)
                ? Math.max(0.0, 1.0 - getAverageControlRodInsertion(level, structure) / 100.0)
                : 0.0;
        output.putDouble("SyncActivity", syncActivity);
        if (structure != null) {
            output.putLong("SyncMin", structure.min().asLong());
            output.putLong("SyncMax", structure.max().asLong());
            output.putIntArray("SyncFuelRods", encodeRelativeOffsets(structure.fuelRodPositions()));
            output.putIntArray("SyncControlRods", encodeRelativeOffsets(structure.controlRods()));
        }
    }

    private int[] encodeRelativeOffsets(List<BlockPos> positions) {
        int[] offsets = new int[positions.size() * 3];
        for (int i = 0; i < positions.size(); i++) {
            BlockPos rp = positions.get(i);
            offsets[i * 3] = rp.getX() - worldPosition.getX();
            offsets[i * 3 + 1] = rp.getY() - worldPosition.getY();
            offsets[i * 3 + 2] = rp.getZ() - worldPosition.getZ();
        }
        return offsets;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);
        energyStorage.deserialize(input);
        // Restore the structure-sized capacity before the amounts, or large tanks would be clamped on reload.
        int savedCap = input.getIntOr("TankCapacity", coolantTank.getCapacity());
        coolantTank.setCapacity(savedCap);
        steamTank.setCapacity(savedCap);
        coolantTank.load(input, "CoolantTank");
        coolantRemainder = Math.max(0, input.getIntOr("CoolantRemainder", 0));
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
        this.clientFuelHeat = (float) input.getDoubleOr("SyncFuelHeat", ReactorSimulation.AMBIENT_TEMP);
        this.clientActivity = (float) input.getDoubleOr("SyncActivity", 0.0);
        this.clientFuelRodOffsets = input.getIntArray("SyncFuelRods").orElse(new int[0]);
        this.clientControlRodOffsets = input.getIntArray("SyncControlRods").orElse(new int[0]);
    }

    @Override
    public AABB getRenderBoundingBox() {
        // the renderer draws the whole core (fuel rods / rotor), not just the controller block
        BlockPos min = getClientMin();
        BlockPos max = getClientMax();
        if (isClientAssembled() && min != null && max != null) {
            return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
        }
        return super.getRenderBoundingBox();
    }
}
