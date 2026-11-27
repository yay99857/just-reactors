package com.justreactors.gametest;

import com.justreactors.transfer.Caps;
import com.justreactors.JustReactorsConfig;
import com.justreactors.battery.BatteryControllerBlockEntity;
import com.justreactors.battery.BatteryPowerPortBlock;
import com.justreactors.battery.BatteryRedstoneSensorBlock;
import com.justreactors.battery.BatteryRedstoneSensorBlockEntity;
import com.justreactors.battery.BatterySensorMode;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.exchanger.ExchangerPortMode;
import com.justreactors.exchanger.HeatExchangerControllerBlockEntity;
import com.justreactors.exchanger.HeatExchangerFluidPortBlock;
import com.justreactors.multiblock.AssembledMultiblocks;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.Map;

/** GameTests for the coolant loop, the Heat Exchanger and the Battery. */
final class MultiblockGameTests {
    private MultiblockGameTests() {
    }

    /**
     * Builds a size^3 box at (ox, oy, oz): casing everywhere on the shell, {@code core} inside, then the overrides
     * (relative to the box origin). Returns the absolute-in-test position of the box origin.
     */
    private static void buildBox(GameTestHelper helper, int ox, int oy, int oz, int size, Block casing, Block core,
                                 Map<BlockPos, BlockState> overrides) {
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                for (int dz = 0; dz < size; dz++) {
                    boolean shell = dx == 0 || dy == 0 || dz == 0 || dx == size - 1 || dy == size - 1 || dz == size - 1;
                    helper.setBlock(new BlockPos(ox + dx, oy + dy, oz + dz), shell ? casing : core);
                }
            }
        }
        overrides.forEach((rel, state) -> helper.setBlock(new BlockPos(ox + rel.getX(), oy + rel.getY(), oz + rel.getZ()), state));
    }

    // ---------------------------------------------------------------- coolant reactor

    static void reactorCoolantMakesHotCoolant(GameTestHelper helper) {
        BlockPos ctrlPos = ReactorGameTests.build3x3x3Public(helper, 1, 1, 1);
        helper.setBlock(new BlockPos(3, 2, 2), ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                .setValue(ReactorCoolantPortBlock.OUTLET, false));
        helper.setBlock(new BlockPos(2, 2, 3), ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                .setValue(ReactorCoolantPortBlock.OUTLET, true));
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        if (!ctrl.tryAssemble()) {
            helper.fail(String.valueOf("Expected reactor with coolant ports to assemble"));
            return;
        }
        // Coolant enters through the inlet port's fluid capability, like a pipe would.
        IFluidHandler inlet = Caps.block(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 2)), ForgeCapabilities.FLUID_HANDLER, null);
        int inserted = inlet == null ? 0 : inlet.fill(new FluidStack(ModFluids.COOLANT_SOURCE.get(), 4_000), FluidAction.EXECUTE);
        if (inserted != 4_000) {
            helper.fail(String.valueOf("Inlet port should accept Coolant, accepted " + inserted));
            return;
        }
        ctrl.getSimulation().setFuelMb(4_000.0);
        ctrl.setActive(true);
        helper.runAfterDelay(60, () -> {
            FluidStack out = ctrl.getSteamTank().getFluid();
            if (out.isEmpty() || !ModFluids.isHotCoolant(out.getFluid())) {
                helper.fail(String.valueOf("Expected Hot Coolant in the output tank, got " + out));
                return;
            }
            int used = 4_000 - ctrl.getCoolantTank().getFluidAmount();
            if (used != out.getAmount()) {
                helper.fail(String.valueOf("Coolant used (" + used + ") should equal Hot Coolant made (" + out.getAmount() + ")"));
                return;
            }
            helper.succeed();
        });
    }

    static void reactorOutletRejectsInsertion(GameTestHelper helper) {
        BlockPos ctrlPos = ReactorGameTests.build3x3x3Public(helper, 1, 1, 1);
        helper.setBlock(new BlockPos(2, 2, 3), ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                .setValue(ReactorCoolantPortBlock.OUTLET, true));
        ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos)).tryAssemble();
        IFluidHandler outlet = Caps.block(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 3)), ForgeCapabilities.FLUID_HANDLER, null);
        int in = outlet == null ? -1 : outlet.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), 1_000), FluidAction.EXECUTE);
        if (in != 0) {
            helper.fail(String.valueOf("Outlet must not accept fluid, accepted " + in));
            return;
        }
        helper.succeed();
    }

    // ---------------------------------------------------------------- heat exchanger

    private static final BlockPos EX_CTRL = new BlockPos(1, 1, 0);
    private static final BlockPos EX_PORT_IN = new BlockPos(1, 1, 2);
    private static final BlockPos EX_PORT_COOL = new BlockPos(0, 1, 1);
    private static final BlockPos EX_PORT_STEAM = new BlockPos(2, 1, 1);

    private static HeatExchangerControllerBlockEntity buildExchanger(GameTestHelper helper, Block core) {
        buildBox(helper, 1, 1, 1, 3, ModBlocks.HEAT_EXCHANGER_CASING.get(), core, Map.of(
                EX_CTRL, ModBlocks.HEAT_EXCHANGER_CONTROLLER.get().defaultBlockState(),
                EX_PORT_IN, port(ExchangerPortMode.INPUT),
                EX_PORT_COOL, port(ExchangerPortMode.OUTPUT_COOLANT),
                EX_PORT_STEAM, port(ExchangerPortMode.OUTPUT_STEAM)));
        return ((HeatExchangerControllerBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 1)));
    }

    private static BlockState port(ExchangerPortMode mode) {
        return ModBlocks.HEAT_EXCHANGER_FLUID_PORT.get().defaultBlockState().setValue(HeatExchangerFluidPortBlock.MODE, mode);
    }

    static void heatExchangerCoolsAndBoils(GameTestHelper helper) {
        HeatExchangerControllerBlockEntity ex = buildExchanger(helper, ModBlocks.HEAT_EXCHANGER_TUBE.get());
        if (!ex.tryAssemble() || ex.getTubes() != 1) {
            helper.fail(String.valueOf("Expected 3x3x3 exchanger with 1 tube to assemble: " + ex.getLastValidationError()));
            return;
        }
        IFluidHandler in = Caps.block(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 3)), ForgeCapabilities.FLUID_HANDLER, null);
        int hot = in.fill(new FluidStack(ModFluids.HOT_COOLANT_SOURCE.get(), 1_000), FluidAction.EXECUTE);
        int water = in.fill(new FluidStack(Fluids.WATER, 10_000), FluidAction.EXECUTE);
        int steamIn = in.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), 1_000), FluidAction.EXECUTE);
        if (hot != 1_000 || water != 10_000 || steamIn != 0) {
            helper.fail(String.valueOf("Input port routing wrong: hot=" + hot + " water=" + water + " steam=" + steamIn));
            return;
        }
        helper.runAfterDelay(10, () -> {
            int cooled = ex.getCoolantTank().getFluidAmount();
            int steam = ex.getSteamTank().getFluidAmount();
            int k = HeatExchangerControllerBlockEntity.steamPerHotCoolant();
            if (cooled <= 0 || steam != cooled * k) {
                helper.fail(String.valueOf("Expected Coolant and " + k + "x Steam, got coolant=" + cooled + " steam=" + steam));
                return;
            }
            int perTick = JustReactorsConfig.get(JustReactorsConfig.EXCHANGER_HOT_COOLANT_PER_TUBE);
            if (ex.getLastCooledMb() > perTick) {
                helper.fail(String.valueOf("One tube must not cool more than " + perTick + " mB/t"));
                return;
            }
            if (ex.getHotTank().getFluidAmount() + cooled != 1_000 || ex.getWaterTank().getFluidAmount() + steam != 10_000) {
                helper.fail(String.valueOf("Fluid was created or lost in the exchanger"));
                return;
            }
            helper.succeed();
        });
    }

    static void heatExchangerNeedsTubes(GameTestHelper helper) {
        HeatExchangerControllerBlockEntity ex = buildExchanger(helper, Blocks.AIR);
        if (ex.tryAssemble()) {
            helper.fail(String.valueOf("An exchanger without tubes must not assemble"));
            return;
        }
        helper.succeed();
    }

    static void heatExchangerStopsWithoutWater(GameTestHelper helper) {
        HeatExchangerControllerBlockEntity ex = buildExchanger(helper, ModBlocks.HEAT_EXCHANGER_TUBE.get());
        ex.tryAssemble();
        ex.getHotTank().fillInternal(new FluidStack(ModFluids.HOT_COOLANT_SOURCE.get(), 1_000));
        helper.runAfterDelay(10, () -> {
            if (ex.getHotTank().getFluidAmount() != 1_000 || !ex.getCoolantTank().isEmpty()) {
                helper.fail(String.valueOf("Without Water nothing should be cooled"));
                return;
            }
            helper.succeed();
        });
    }

    // ---------------------------------------------------------------- battery

    private static final BlockPos BAT_CTRL = new BlockPos(1, 1, 0);
    private static final BlockPos BAT_IN = new BlockPos(1, 1, 2);
    private static final BlockPos BAT_OUT = new BlockPos(0, 1, 1);
    private static final BlockPos BAT_SENSOR = new BlockPos(2, 1, 1);

    private static BatteryControllerBlockEntity buildBattery(GameTestHelper helper) {
        buildBox(helper, 1, 1, 1, 3, ModBlocks.BATTERY_CASING.get(), ModBlocks.BATTERY_CELL.get(), Map.of(
                BAT_CTRL, ModBlocks.BATTERY_CONTROLLER.get().defaultBlockState(),
                BAT_IN, ModBlocks.BATTERY_POWER_PORT.get().defaultBlockState(),
                BAT_OUT, ModBlocks.BATTERY_POWER_PORT.get().defaultBlockState().setValue(BatteryPowerPortBlock.OUTLET, true),
                BAT_SENSOR, ModBlocks.BATTERY_REDSTONE_SENSOR.get().defaultBlockState()));
        return ((BatteryControllerBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 1)));
    }

    private static IEnergyStorage energyAt(GameTestHelper helper, BlockPos rel) {
        return Caps.block(helper.getLevel(), helper.absolutePos(new BlockPos(1 + rel.getX(), 1 + rel.getY(), 1 + rel.getZ())), ForgeCapabilities.ENERGY, null);
    }

    static void batteryStoresAndReleasesEnergy(GameTestHelper helper) {
        BatteryControllerBlockEntity bat = buildBattery(helper);
        if (!bat.tryAssemble() || bat.getCells() != 1) {
            helper.fail(String.valueOf("Expected 3x3x3 battery with 1 cell: " + bat.getLastValidationError()));
            return;
        }
        int capacity = BatteryControllerBlockEntity.capacityFor(1);
        if (bat.getEnergyStorage().getMaxEnergyStored() != capacity) {
            helper.fail(String.valueOf("Capacity should be one cell's worth"));
            return;
        }
        IEnergyStorage in = energyAt(helper, BAT_IN);
        IEnergyStorage out = energyAt(helper, BAT_OUT);
        int max = bat.getMaxTransfer();
        int accepted = in.receiveEnergy(max * 3, false);
        int rejected = out.receiveEnergy(1_000, false);
        if (accepted != max || rejected != 0) {
            helper.fail(String.valueOf("Input should accept " + max + " (got " + accepted + "), output none (got " + rejected + ")"));
            return;
        }
        int fromInput = in.extractEnergy(1_000, false);
        int fromOutput = out.extractEnergy(1_000, false);
        if (fromInput != 0 || fromOutput != 1_000) {
            helper.fail(String.valueOf("Only the output port may be drained (in=" + fromInput + ", out=" + fromOutput + ")"));
            return;
        }
        if (bat.getEnergyStorage().getEnergyStored() != max - 1_000) {
            helper.fail(String.valueOf("Stored energy mismatch: " + bat.getEnergyStorage().getEnergyStored()));
            return;
        }
        helper.succeed();
    }

    static void batterySensorFollowsCharge(GameTestHelper helper) {
        BatteryControllerBlockEntity bat = buildBattery(helper);
        bat.tryAssemble();
        BatteryRedstoneSensorBlockEntity sensor = ((BatteryRedstoneSensorBlockEntity) helper.getBlockEntity(new BlockPos(3, 2, 2)));
        sensor.setMode(BatterySensorMode.CHARGE_ABOVE);
        int cap = bat.getEnergyStorage().getMaxEnergyStored();
        bat.getEnergyStorage().generateEnergy((int) (cap * 0.95));
        helper.runAfterDelay(3, () -> {
            if (!helper.getBlockState(new BlockPos(3, 2, 2)).getValue(BatteryRedstoneSensorBlock.POWERED)) {
                helper.fail(String.valueOf("Sensor should be powered at 95% with a 90% threshold"));
                return;
            }
            if (bat.getComparatorSignal() < 14) {
                helper.fail(String.valueOf("Controller comparator should read near full, got " + bat.getComparatorSignal()));
                return;
            }
            bat.getEnergyStorage().extractInternal((int) (cap * 0.5));
            helper.runAfterDelay(3, () -> {
                if (helper.getBlockState(new BlockPos(3, 2, 2)).getValue(BatteryRedstoneSensorBlock.POWERED)) {
                    helper.fail(String.valueOf("Sensor should switch off at 45%"));
                    return;
                }
                sensor.setMode(BatterySensorMode.CHARGE_BELOW);
                helper.runAfterDelay(2, () -> {
                    if (!helper.getBlockState(new BlockPos(3, 2, 2)).getValue(BatteryRedstoneSensorBlock.POWERED)) {
                        helper.fail(String.valueOf("CHARGE_BELOW 90% should be powered at 45%"));
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    static void batteryKeepsEnergyAcrossReassembly(GameTestHelper helper) {
        BatteryControllerBlockEntity bat = buildBattery(helper);
        bat.tryAssemble();
        bat.getEnergyStorage().generateEnergy(123_456);
        // break a casing, then repair it
        BlockPos casing = new BlockPos(1, 1, 1);
        helper.setBlock(casing, Blocks.AIR);
        if (bat.tryAssemble()) {
            helper.fail(String.valueOf("Battery with a hole must not assemble"));
            return;
        }
        if (AssembledMultiblocks.isInside(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)))) {
            helper.fail(String.valueOf("Broken battery must leave the assembled index"));
            return;
        }
        helper.setBlock(casing, ModBlocks.BATTERY_CASING.get());
        if (!bat.tryAssemble() || bat.getEnergyStorage().getEnergyStored() != 123_456) {
            helper.fail(String.valueOf("Energy should survive a repair, got " + bat.getEnergyStorage().getEnergyStored()));
            return;
        }
        if (!AssembledMultiblocks.isInside(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)))) {
            helper.fail(String.valueOf("Repaired battery should be indexed as assembled"));
            return;
        }
        helper.succeed();
    }
}
