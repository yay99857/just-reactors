package com.justreactors.turbine.blockentity;

import com.justreactors.JustReactorsConfig;
import com.justreactors.client.ClientMachineSounds;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.transfer.ReactorEnergyStorage;
import com.justreactors.transfer.ReactorFluidTank;
import com.justreactors.turbine.TurbineSimulation;
import com.justreactors.turbine.TurbineStructure;
import com.justreactors.turbine.TurbineValidator;
import com.justreactors.turbine.TurbineVentMode;
import com.justreactors.turbine.block.TurbineControllerBlock;
import com.justreactors.turbine.block.TurbineRotorBladeBlock;
import com.justreactors.turbine.block.TurbineRotorShaftBlock;
import com.justreactors.turbine.menu.TurbineControllerMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
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
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TurbineControllerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int VALIDATION_INTERVAL_TICKS = 20;
    private static final int FLUID_TANK_CAPACITY_MB = 32_000;
    private static final int WATER_EJECT_RATE_MB = 4_000;
    private static final double SPINNING_RPM_THRESHOLD = 0.5;

    private final TurbineSimulation simulation = new TurbineSimulation();
    private final ReactorEnergyStorage energyStorage;
    private final ReactorFluidTank steamTank;
    private final ReactorFluidTank waterTank;
    private TurbineVentMode ventMode = TurbineVentMode.OVERFLOW;

    @Nullable
    private TurbineStructure structure;
    @Nullable
    private Component lastValidationError;
    private int tickCounter = 0;

    // Server sync tracking
    private boolean rotorMarkedSpinning = false;
    private boolean lastSyncedAssembled = false;
    private boolean lastSyncedSpinning = false;
    private boolean lastSyncedActive = false;
    private double lastSyncedRpm = -1.0;

    // Client render snapshot & animation state
    private boolean clientAssembled = false;
    private boolean clientSpinning = false;
    private boolean clientActive = false;
    private double clientRpm = 0.0;
    @Nullable
    private BlockPos clientMin;
    @Nullable
    private BlockPos clientMax;
    @Nullable
    private BlockPos clientBearingPos;
    private Direction clientInwardDir = Direction.UP;
    private int clientShaftLength = 0;
    private int[] clientBladeOffsets = new int[0];

    private float prevRotorAngle = 0.0F;
    private float rotorAngle = 0.0F;

    public TurbineControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURBINE_CONTROLLER.get(), pos, state);
        this.energyStorage = new ReactorEnergyStorage(
                JustReactorsConfig.get(JustReactorsConfig.TURBINE_ENERGY_CAPACITY),
                JustReactorsConfig.get(JustReactorsConfig.POWER_TAP_MAX_EXTRACT),
                this::setChanged
        );
        this.steamTank = new ReactorFluidTank(FLUID_TANK_CAPACITY_MB, stack -> true, this::setChanged);
        this.waterTank = new ReactorFluidTank(FLUID_TANK_CAPACITY_MB, stack -> stack.is(Fluids.WATER), this::setChanged);
        this.simulation.setMaxSteamFlowMb(JustReactorsConfig.get(JustReactorsConfig.TURBINE_MAX_STEAM_FLOW));
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TurbineControllerBlockEntity be) {
        be.prevRotorAngle = be.rotorAngle;
        ClientMachineSounds.tickTurbineSound(be);
        if (!be.clientAssembled || !be.clientSpinning || be.clientRpm <= SPINNING_RPM_THRESHOLD) {
            return;
        }

        // Keep visual angular step smooth (1.5..32 deg/tick) so high RPM reads cleanly without wagon-wheel aliasing.
        float degPerTick = (float) Math.min(32.0, Math.max(1.5, be.clientRpm * 0.025));
        be.rotorAngle += degPerTick;
        if (be.rotorAngle >= 360.0F) {
            be.rotorAngle -= 360.0F;
            be.prevRotorAngle -= 360.0F;
        }

        // Subtle steam swirl inside the turbine chamber while actively receiving steam
        if (be.clientActive && be.clientMin != null && be.clientMax != null) {
            RandomSource rand = level.getRandom();
            if (rand.nextFloat() < 0.35F) {
                int spanX = Math.max(1, be.clientMax.getX() - be.clientMin.getX() - 1);
                int spanY = Math.max(1, be.clientMax.getY() - be.clientMin.getY() - 1);
                int spanZ = Math.max(1, be.clientMax.getZ() - be.clientMin.getZ() - 1);
                double px = be.clientMin.getX() + 1.15 + rand.nextDouble() * Math.max(0.1, spanX - 0.3);
                double py = be.clientMin.getY() + 1.15 + rand.nextDouble() * Math.max(0.1, spanY - 0.3);
                double pz = be.clientMin.getZ() + 1.15 + rand.nextDouble() * Math.max(0.1, spanZ - 0.3);
                double vx = be.clientInwardDir.getStepX() * 0.035 + (rand.nextDouble() - 0.5) * 0.015;
                double vy = be.clientInwardDir.getStepY() * 0.035 + (rand.nextDouble() - 0.5) * 0.015;
                double vz = be.clientInwardDir.getStepZ() * 0.035 + (rand.nextDouble() - 0.5) * 0.015;
                level.addParticle(ParticleTypes.CLOUD, px, py, pz, vx, vy, vz);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TurbineControllerBlockEntity controller) {
        controller.tickCounter++;

        if (controller.structure == null || controller.tickCounter % VALIDATION_INTERVAL_TICKS == 0) {
            controller.tryAssemble();
        }

        if (controller.structure == null) {
            controller.updateBlockState(level, pos, state, false, false);
            controller.syncClientRenderData(level, pos, level.getBlockState(pos));
            return;
        }

        TurbineStructure s = controller.structure;
        int availableSteam = controller.ventMode == TurbineVentMode.NONE
                ? Math.min(controller.steamTank.getFluidAmount(), controller.waterTank.getSpace())
                : controller.steamTank.getFluidAmount();
        TurbineSimulation.TickOutput output = controller.simulation.tick(
                s,
                availableSteam,
                controller.energyStorage.getSpace(),
                JustReactorsConfig.get(JustReactorsConfig.TURBINE_POWER_MULTIPLIER),
                JustReactorsConfig.get(JustReactorsConfig.TURBINE_MAX_STEAM_FLOW)
        );

        if (output.steamConsumedMb() > 0) {
            controller.steamTank.drainInternal(output.steamConsumedMb());
        }
        if (controller.ventMode == TurbineVentMode.ALL) {
            if (!controller.waterTank.isEmpty()) {
                controller.waterTank.drainInternal(controller.waterTank.getFluidAmount());
            }
        } else if (output.waterCondensedMb() > 0) {
            controller.waterTank.fillInternal(new FluidStack(Fluids.WATER, output.waterCondensedMb()));
        }
        if (output.generatedFe() > 0) {
            controller.energyStorage.generateEnergy(output.generatedFe());
        }

        // Push FE out through all linked Power Taps
        if (controller.energyStorage.getEnergyStored() > 0) {
            int maxPerTap = JustReactorsConfig.get(JustReactorsConfig.POWER_TAP_MAX_EXTRACT);
            for (BlockPos tapPos : s.powerTaps()) {
                if (controller.energyStorage.getEnergyStored() <= 0) break;
                if (level.getBlockEntity(tapPos) instanceof TurbinePowerTapBlockEntity tap) {
                    tap.pushEnergyToNeighbors(controller.energyStorage, maxPerTap);
                }
            }
        }

        // Push condensed Water out through Outlet Fluid Ports
        if (!controller.waterTank.isEmpty()) {
            for (BlockPos portPos : s.fluidPorts()) {
                if (controller.waterTank.isEmpty()) break;
                if (level.getBlockEntity(portPos) instanceof TurbineFluidPortBlockEntity port && port.isOutlet()) {
                    port.pushWaterToNeighbors(controller.waterTank, WATER_EJECT_RATE_MB);
                }
            }
        }

        // Update output Redstone Ports
        for (BlockPos rpPos : s.redstonePorts()) {
            if (level.getBlockEntity(rpPos) instanceof TurbineRedstonePortBlockEntity rp) {
                rp.tickOutput(controller);
            }
        }

        double rpm = controller.simulation.getRpm();
        boolean shouldSpin = rpm > SPINNING_RPM_THRESHOLD;
        if (shouldSpin != controller.rotorMarkedSpinning) {
            setRotorBlocksSpinning(level, s, shouldSpin);
            controller.rotorMarkedSpinning = shouldSpin;
        }

        boolean visuallyActive = controller.simulation.isActive() && rpm > 1.0;
        controller.updateBlockState(level, pos, state, true, visuallyActive);
        controller.syncClientRenderData(level, pos, level.getBlockState(pos));

        if (output.generatedFe() > 0 || output.steamConsumedMb() > 0) {
            controller.setChanged();
        }
    }

    private void syncClientRenderData(Level level, BlockPos pos, BlockState state) {
        boolean assembled = this.structure != null;
        boolean spinning = assembled && this.simulation.getRpm() > SPINNING_RPM_THRESHOLD;
        boolean active = assembled && this.simulation.isActive() && this.simulation.getLastSteamConsumedMb() > 0;
        double rpm = assembled ? this.simulation.getRpm() : 0.0;

        boolean stateChanged = assembled != lastSyncedAssembled
                || spinning != lastSyncedSpinning
                || active != lastSyncedActive;
        boolean rpmChanged = (tickCounter % 10 == 0) && Math.abs(rpm - lastSyncedRpm) >= 2.0;

        if (stateChanged || rpmChanged) {
            lastSyncedAssembled = assembled;
            lastSyncedSpinning = spinning;
            lastSyncedActive = active;
            lastSyncedRpm = rpm;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void setRotorBlocksSpinning(Level level, TurbineStructure s, boolean spinning) {
        for (BlockPos pos : s.shaftPositions()) {
            BlockState st = level.getBlockState(pos);
            if (st.getBlock() == ModBlocks.TURBINE_ROTOR_SHAFT.get()
                    && st.hasProperty(TurbineRotorShaftBlock.SPINNING)
                    && st.getValue(TurbineRotorShaftBlock.SPINNING) != spinning) {
                level.setBlock(pos, st.setValue(TurbineRotorShaftBlock.SPINNING, spinning), Block.UPDATE_CLIENTS);
            }
        }
        for (BlockPos pos : s.bladePositions()) {
            BlockState st = level.getBlockState(pos);
            if (st.getBlock() == ModBlocks.TURBINE_ROTOR_BLADE.get()
                    && st.hasProperty(TurbineRotorBladeBlock.SPINNING)
                    && st.getValue(TurbineRotorBladeBlock.SPINNING) != spinning) {
                level.setBlock(pos, st.setValue(TurbineRotorBladeBlock.SPINNING, spinning), Block.UPDATE_CLIENTS);
            }
        }
    }

    public boolean tryAssemble() {
        if (level == null || level.isClientSide()) return false;
        TurbineValidator.Result result = TurbineValidator.validate(level, worldPosition);
        if (result instanceof TurbineValidator.Result.Valid valid) {
            boolean wasUnassembled = this.structure == null;
            this.structure = valid.structure();
            this.lastValidationError = null;
            linkParts(valid.structure());
            com.justreactors.multiblock.AssembledMultiblocks.put(level, worldPosition, valid.structure().min(), valid.structure().max());
            boolean shouldSpin = this.simulation.getRpm() > SPINNING_RPM_THRESHOLD;
            if (wasUnassembled || shouldSpin != this.rotorMarkedSpinning) {
                setRotorBlocksSpinning(level, valid.structure(), shouldSpin);
                this.rotorMarkedSpinning = shouldSpin;
            }
            if (wasUnassembled) {
                syncClientRenderData(level, worldPosition, getBlockState());
            }
            return true;
        } else if (result instanceof TurbineValidator.Result.Invalid invalid) {
            unlinkParts();
            this.structure = null;
            com.justreactors.multiblock.AssembledMultiblocks.remove(level, worldPosition);
            this.lastValidationError = invalid.reason();
            syncClientRenderData(level, worldPosition, getBlockState());
            return false;
        }
        return false;
    }

    private void linkParts(TurbineStructure s) {
        if (level == null) return;
        for (BlockPos p : s.powerTaps()) {
            if (level.getBlockEntity(p) instanceof TurbinePartBlockEntity part) part.setControllerPos(worldPosition);
        }
        for (BlockPos p : s.fluidPorts()) {
            if (level.getBlockEntity(p) instanceof TurbinePartBlockEntity part) part.setControllerPos(worldPosition);
        }
        for (BlockPos p : s.redstonePorts()) {
            if (level.getBlockEntity(p) instanceof TurbinePartBlockEntity part) part.setControllerPos(worldPosition);
        }
    }

    private void unlinkParts() {
        if (level == null || structure == null) return;
        if (rotorMarkedSpinning && !level.isClientSide()) {
            setRotorBlocksSpinning(level, structure, false);
            rotorMarkedSpinning = false;
        }
        for (BlockPos p : structure.powerTaps()) {
            if (level.getBlockEntity(p) instanceof TurbinePartBlockEntity part) part.setControllerPos(null);
        }
        for (BlockPos p : structure.fluidPorts()) {
            if (level.getBlockEntity(p) instanceof TurbinePartBlockEntity part) part.setControllerPos(null);
        }
        for (BlockPos p : structure.redstonePorts()) {
            if (level.getBlockEntity(p) instanceof TurbinePartBlockEntity part) part.setControllerPos(null);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide() && rotorMarkedSpinning && structure != null) {
            setRotorBlocksSpinning(level, structure, false);
            rotorMarkedSpinning = false;
        }
        if (level != null) com.justreactors.multiblock.AssembledMultiblocks.remove(level, worldPosition);
        super.setRemoved();
    }

    private void updateBlockState(Level level, BlockPos pos, BlockState state, boolean assembled, boolean active) {
        if (state.getValue(TurbineControllerBlock.ASSEMBLED) != assembled
                || state.getValue(TurbineControllerBlock.ACTIVE) != active) {
            level.setBlock(pos, state
                    .setValue(TurbineControllerBlock.ASSEMBLED, assembled)
                    .setValue(TurbineControllerBlock.ACTIVE, active), 3);
        }
    }

    public void toggleActive() {
        simulation.setActive(!simulation.isActive());
        setChanged();
    }

    public void setActive(boolean active) {
        if (simulation.isActive() != active) {
            simulation.setActive(active);
            setChanged();
        }
    }

    public void toggleInductor() {
        setInductorEngaged(!simulation.isInductorEngaged());
    }

    public void setInductorEngaged(boolean engaged) {
        if (simulation.isInductorEngaged() != engaged) {
            simulation.setInductorEngaged(engaged);
            setChanged();
        }
    }

    public void setMaxSteamFlowMb(int rate) {
        int maxAllowed = JustReactorsConfig.get(JustReactorsConfig.TURBINE_MAX_STEAM_FLOW);
        int clamped = Math.max(0, Math.min(maxAllowed, rate));
        if (simulation.getMaxSteamFlowMb() != clamped) {
            simulation.setMaxSteamFlowMb(clamped);
            setChanged();
        }
    }

    public void adjustMaxSteamFlow(int delta) {
        setMaxSteamFlowMb(simulation.getMaxSteamFlowMb() + delta);
    }

    public TurbineVentMode getVentMode() {
        return ventMode;
    }

    public void setVentMode(TurbineVentMode mode) {
        if (this.ventMode != mode) {
            this.ventMode = mode;
            setChanged();
        }
    }

    public void cycleVentMode() {
        setVentMode(this.ventMode.next());
    }

    public boolean isAssembled() {
        return structure != null;
    }

    @Nullable
    public TurbineStructure getStructure() {
        return structure;
    }

    @Nullable
    public Component getLastValidationError() {
        return lastValidationError;
    }

    public TurbineSimulation getSimulation() {
        return simulation;
    }

    public ReactorEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public ReactorFluidTank getSteamTank() {
        return steamTank;
    }

    public ReactorFluidTank getWaterTank() {
        return waterTank;
    }

    // Client render getters
    public boolean isClientAssembled() {
        return clientAssembled;
    }

    public boolean isClientSpinning() {
        return clientSpinning;
    }

    public double getClientRpm() {
        return clientRpm;
    }

    @Nullable
    public BlockPos getClientMin() {
        return clientMin;
    }

    @Nullable
    public BlockPos getClientMax() {
        return clientMax;
    }

    @Nullable
    public BlockPos getClientBearingPos() {
        return clientBearingPos;
    }

    public Direction getClientInwardDir() {
        return clientInwardDir;
    }

    public int getClientShaftLength() {
        return clientShaftLength;
    }

    public int[] getClientBladeOffsets() {
        return clientBladeOffsets;
    }

    public float getInterpolatedRotorAngle(float partialTicks) {
        return Mth.lerp(partialTicks, prevRotorAngle, rotorAngle);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.turbine_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TurbineControllerMenu(containerId, playerInventory, this);
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
        steamTank.save(output, "SteamTank");
        waterTank.save(output, "WaterTank");
        output.putBoolean("Active", simulation.isActive());
        output.putBoolean("Inductor", simulation.isInductorEngaged());
        output.putInt("MaxSteamFlow", simulation.getMaxSteamFlowMb());
        output.putDouble("RotorEnergy", simulation.getRotorEnergy());
        output.putInt("VentMode", ventMode.ordinal());

        boolean assembled = structure != null;
        output.putBoolean("SyncAssembled", assembled);
        output.putBoolean("SyncSpinning", assembled && simulation.getRpm() > SPINNING_RPM_THRESHOLD);
        output.putBoolean("SyncActive", assembled && simulation.isActive() && simulation.getLastSteamConsumedMb() > 0);
        output.putDouble("SyncRpm", assembled ? simulation.getRpm() : 0.0);
        if (structure != null) {
            output.putLong("SyncMin", structure.min().asLong());
            output.putLong("SyncMax", structure.max().asLong());
            BlockPos bearing = structure.bearingPos();
            output.putLong("SyncBearing", bearing.asLong());
            output.putInt("SyncInward", structure.inwardDir().ordinal());
            output.putInt("SyncShaftLen", structure.shaftLength());

            List<BlockPos> blades = structure.bladePositions();
            int[] offsets = new int[blades.size() * 3];
            for (int i = 0; i < blades.size(); i++) {
                BlockPos bp = blades.get(i);
                offsets[i * 3] = bp.getX() - bearing.getX();
                offsets[i * 3 + 1] = bp.getY() - bearing.getY();
                offsets[i * 3 + 2] = bp.getZ() - bearing.getZ();
            }
            output.putIntArray("SyncBlades", offsets);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = new ValueInput(tag, registries);
        energyStorage.deserialize(input);
        steamTank.load(input, "SteamTank");
        waterTank.load(input, "WaterTank");
        simulation.setActive(input.getBooleanOr("Active", false));
        simulation.setInductorEngaged(input.getBooleanOr("Inductor", true));
        simulation.setMaxSteamFlowMb(input.getIntOr("MaxSteamFlow",
                JustReactorsConfig.get(JustReactorsConfig.TURBINE_MAX_STEAM_FLOW)));
        simulation.setRotorEnergy(input.getDoubleOr("RotorEnergy", 0.0));
        this.ventMode = TurbineVentMode.byOrdinal(input.getIntOr("VentMode", 0));

        this.clientAssembled = input.getBooleanOr("SyncAssembled", false);
        this.clientSpinning = input.getBooleanOr("SyncSpinning", false);
        this.clientActive = input.getBooleanOr("SyncActive", false);
        this.clientRpm = input.getDoubleOr("SyncRpm", 0.0);
        this.clientMin = input.getLong("SyncMin").map(BlockPos::of).orElse(null);
        this.clientMax = input.getLong("SyncMax").map(BlockPos::of).orElse(null);
        this.clientBearingPos = input.getLong("SyncBearing").map(BlockPos::of).orElse(null);
        int inwardOrd = input.getIntOr("SyncInward", Direction.UP.ordinal());
        Direction[] dirs = Direction.values();
        this.clientInwardDir = (inwardOrd >= 0 && inwardOrd < dirs.length) ? dirs[inwardOrd] : Direction.UP;
        this.clientShaftLength = input.getIntOr("SyncShaftLen", 0);
        this.clientBladeOffsets = input.getIntArray("SyncBlades").orElse(new int[0]);
    }
}
