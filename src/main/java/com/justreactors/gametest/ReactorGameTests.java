package com.justreactors.gametest;

import com.justreactors.JustReactors;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.demo.DemoSetup;
import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.blockentity.ReactorAccessPortBlockEntity;
import com.justreactors.blockentity.ReactorControlRodBlockEntity;
import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.blockentity.ReactorRedstonePortBlockEntity;
import com.justreactors.datagen.ModTags;
import com.justreactors.reactor.ReactorMode;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.reactor.RedstonePortMode;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.registry.ModItems;
import com.justreactors.turbine.TurbineCoil;
import com.justreactors.turbine.TurbineRedstonePortMode;
import com.justreactors.turbine.TurbineSimulation;
import com.justreactors.turbine.TurbineVentMode;
import com.justreactors.turbine.block.TurbineFluidPortBlock;
import com.justreactors.turbine.block.TurbineRotorBladeBlock;
import com.justreactors.turbine.block.TurbineRotorShaftBlock;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;
import com.justreactors.turbine.blockentity.TurbineRedstonePortBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(JustReactors.MOD_ID)
public final class ReactorGameTests {
    private static final ResourceLocation STRUCTURE = JustReactors.id("empty");

    private record NamedTest(String name, Consumer<GameTestHelper> body, int timeoutTicks) {
    }

    private static final List<NamedTest> TESTS = List.of(
            new NamedTest("assemble_minimal_3x3x3", ReactorGameTests::assembleMinimal3x3x3, 40),
            new NamedTest("reject_missing_control_rod", ReactorGameTests::rejectMissingControlRod, 40),
            new NamedTest("generate_energy_from_uranium", ReactorGameTests::generateEnergyFromUranium, 80),
            new NamedTest("control_rod_100_stops_burn", ReactorGameTests::controlRod100StopsBurn, 60),
            new NamedTest("moderators_boost_output", ReactorGameTests::moderatorsBoostOutput, 100),
            new NamedTest("eject_waste_to_access_port", ReactorGameTests::ejectWasteToAccessPort, 40),
            new NamedTest("redstone_port_toggles_reactor", ReactorGameTests::redstonePortTogglesReactor, 40),
            new NamedTest("assemble_5x5x5_checkerboard_reactor", ReactorGameTests::assemble5x5x5CheckerboardReactor, 100),
            new NamedTest("active_reactor_boils_water_to_steam", ReactorGameTests::activeReactorBoilsWaterToSteam, 100),
            new NamedTest("turbine_assembles_and_generates_fe", ReactorGameTests::turbineAssemblesAndGeneratesFe, 100),
            new NamedTest("cyanite_reprocessor_makes_blutonium", ReactorGameTests::cyaniteReprocessorMakesBlutonium, 140),
            new NamedTest("demo_setup_closed_loop_turbine", ReactorGameTests::demoSetupClosedLoopTurbine, 120),
            new NamedTest("redstone_port_output_energy_threshold", ReactorGameTests::redstonePortOutputEnergyThreshold, 40),
            new NamedTest("redstone_port_analog_control_rods", ReactorGameTests::redstonePortAnalogControlRods, 40),
            new NamedTest("datapack_moderators_and_coils_loaded", ReactorGameTests::datapackModeratorsAndCoilsLoaded, 40),
            new NamedTest("turbine_shutdown_spins_down", ReactorGameTests::turbineShutdownSpinsDown, 80),
            new NamedTest("turbine_redstone_port_controls_and_outputs", ReactorGameTests::turbineRedstonePortControlsAndOutputs, 40),
            new NamedTest("turbine_vent_modes", ReactorGameTests::turbineVentModes, 40),
            new NamedTest("jei_display_entries_populated", ReactorGameTests::jeiDisplayEntriesPopulated, 40),
            new NamedTest("reactor_coolant_makes_hot_coolant", MultiblockGameTests::reactorCoolantMakesHotCoolant, 100),
            new NamedTest("reactor_outlet_rejects_insertion", MultiblockGameTests::reactorOutletRejectsInsertion, 40),
            new NamedTest("heat_exchanger_cools_and_boils", MultiblockGameTests::heatExchangerCoolsAndBoils, 60),
            new NamedTest("heat_exchanger_needs_tubes", MultiblockGameTests::heatExchangerNeedsTubes, 40),
            new NamedTest("heat_exchanger_stops_without_water", MultiblockGameTests::heatExchangerStopsWithoutWater, 60),
            new NamedTest("battery_stores_and_releases_energy", MultiblockGameTests::batteryStoresAndReleasesEnergy, 40),
            new NamedTest("battery_sensor_follows_charge", MultiblockGameTests::batterySensorFollowsCharge, 60),
            new NamedTest("battery_keeps_energy_across_reassembly", MultiblockGameTests::batteryKeepsEnergyAcrossReassembly, 40)
    );

    private ReactorGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ReactorGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        event.register(ReactorGameTests.class);
    }

    /** 1.21.1 registers GameTests through a generator instead of 26.x's test-function registry. */
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        return TESTS.stream()
                .map(test -> new TestFunction(JustReactors.MOD_ID, JustReactors.MOD_ID + "." + test.name(),
                        STRUCTURE.toString(), test.timeoutTicks(), 0, true, test.body()))
                .toList();
    }

    static BlockPos build3x3x3Public(GameTestHelper helper, int ox, int oy, int oz) {
        return build3x3x3(helper, ox, oy, oz);
    }

    /** Builds a valid 3x3x3 reactor with origin at (ox, oy, oz). Returns the relative Controller pos. */
    private static BlockPos build3x3x3(GameTestHelper helper, int ox, int oy, int oz) {
        for (int dx = 0; dx < 3; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                for (int dz = 0; dz < 3; dz++) {
                    BlockPos p = new BlockPos(ox + dx, oy + dy, oz + dz);
                    if (dx == 1 && dy == 1 && dz == 1) {
                        helper.setBlock(p, ModBlocks.REACTOR_FUEL_ROD.get());
                    } else if (dx == 1 && dy == 2 && dz == 1) {
                        helper.setBlock(p, ModBlocks.REACTOR_CONTROL_ROD.get());
                    } else if (dx == 1 && dy == 1 && dz == 0) {
                        helper.setBlock(p, ModBlocks.REACTOR_CONTROLLER.get());
                    } else if (dx == 2 && dy == 1 && dz == 1) {
                        helper.setBlock(p, ModBlocks.REACTOR_POWER_TAP.get());
                    } else if (dx == 0 && dy == 1 && dz == 1) {
                        helper.setBlock(p, ModBlocks.REACTOR_ACCESS_PORT.get());
                    } else if (dx == 1 && dy == 1 && dz == 2) {
                        helper.setBlock(p, ModBlocks.REACTOR_REDSTONE_PORT.get());
                    } else {
                        helper.setBlock(p, ModBlocks.REACTOR_CASING.get());
                    }
                }
            }
        }
        return new BlockPos(ox + 1, oy + 1, oz);
    }

    /** Builds a 5x3x5 reactor with 1 center fuel rod and a given moderator block filling the interior. */
    private static BlockPos build5x3x5(GameTestHelper helper, int ox, int oy, int oz, Block moderator) {
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos p = new BlockPos(ox + dx, oy + dy, oz + dz);
                    boolean onX = dx == 0 || dx == 4;
                    boolean onY = dy == 0 || dy == 2;
                    boolean onZ = dz == 0 || dz == 4;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);

                    if (axes >= 2) {
                        helper.setBlock(p, ModBlocks.REACTOR_CASING.get());
                    } else if (axes == 1) {
                        if (dx == 2 && dy == 1 && dz == 0) {
                            helper.setBlock(p, ModBlocks.REACTOR_CONTROLLER.get());
                        } else if (dx == 2 && dy == 2 && dz == 2) {
                            helper.setBlock(p, ModBlocks.REACTOR_CONTROL_ROD.get());
                        } else if (dx == 4 && dy == 1 && dz == 2) {
                            helper.setBlock(p, ModBlocks.REACTOR_POWER_TAP.get());
                        } else if (dx == 0 && dy == 1 && dz == 2) {
                            helper.setBlock(p, ModBlocks.REACTOR_ACCESS_PORT.get());
                        } else {
                            helper.setBlock(p, ModBlocks.REACTOR_CASING.get());
                        }
                    } else {
                        if (dx == 2 && dz == 2) {
                            helper.setBlock(p, ModBlocks.REACTOR_FUEL_ROD.get());
                        } else {
                            helper.setBlock(p, moderator);
                        }
                    }
                }
            }
        }
        return new BlockPos(ox + 2, oy + 1, oz);
    }

    private static void assembleMinimal3x3x3(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        if (!ctrl.tryAssemble() || !ctrl.isAssembled()) {
            helper.fail(String.valueOf("Expected 3x3x3 reactor to assemble, got: " + ctrl.getLastValidationError()));
            return;
        }
        if (ctrl.getStructure().fuelRodCount() != 1 || ctrl.getStructure().powerTaps().size() != 1) {
            helper.fail(String.valueOf("Unexpected structure counts"));
            return;
        }
        helper.succeed();
    }

    private static void rejectMissingControlRod(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        helper.setBlock(new BlockPos(2, 3, 2), ModBlocks.REACTOR_CASING.get());
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        if (ctrl.tryAssemble() || ctrl.isAssembled()) {
            helper.fail(String.valueOf("Reactor without Control Rod should NOT assemble"));
            return;
        }
        helper.succeed();
    }

    private static void generateEnergyFromUranium(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        ReactorAccessPortBlockEntity port = ((ReactorAccessPortBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 2)));
        port.getInventory().setStackInSlot(ReactorAccessPortBlockEntity.SLOT_FUEL, new ItemStack(ModItems.URANIUM_INGOT.get(), 4));

        ctrl.tryAssemble();
        ctrl.setActive(true);

        helper.runAfterDelay(30, () -> {
            if (ctrl.getEnergyStorage().getEnergyStored() <= 0) {
                helper.fail(String.valueOf("Expected reactor to generate FE after 30 ticks"));
                return;
            }
            if (ctrl.getSimulation().getFuelHeat() <= 20.0) {
                helper.fail(String.valueOf("Expected core temperature to rise"));
                return;
            }
            BlockPos tapAbs = helper.absolutePos(new BlockPos(3, 2, 2));
            IEnergyStorage cap = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, tapAbs, null);
            if (cap == null || cap.getEnergyStored() <= 0) {
                helper.fail(String.valueOf("Expected Power Tap to expose stored FE"));
                return;
            }
            helper.succeed();
        });
    }

    private static void controlRod100StopsBurn(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        ReactorControlRodBlockEntity rod = ((ReactorControlRodBlockEntity) helper.getBlockEntity(new BlockPos(2, 3, 2)));
        ctrl.tryAssemble();
        ctrl.getSimulation().setFuelMb(4_000.0);
        rod.setInsertion(100);
        ctrl.setActive(true);

        helper.runAfterDelay(20, () -> {
            if (ctrl.getSimulation().getFuelMb() < 3_999.9) {
                helper.fail(String.valueOf("Expected 100% control rod insertion to stop fuel burn"));
                return;
            }
            helper.succeed();
        });
    }

    private static void moderatorsBoostOutput(GameTestHelper helper) {
        BlockPos airCtrlPos = build5x3x5(helper, 1, 1, 1, Blocks.AIR);
        BlockPos graphiteCtrlPos = build5x3x5(helper, 1, 5, 1, ModBlocks.GRAPHITE_BLOCK.get());

        ReactorControllerBlockEntity airCtrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(airCtrlPos));
        ReactorControllerBlockEntity graphiteCtrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(graphiteCtrlPos));

        airCtrl.tryAssemble();
        graphiteCtrl.tryAssemble();
        airCtrl.getSimulation().setFuelMb(4_000.0);
        graphiteCtrl.getSimulation().setFuelMb(4_000.0);
        airCtrl.setActive(true);
        graphiteCtrl.setActive(true);

        helper.runAfterDelay(60, () -> {
            int airFe = airCtrl.getEnergyStorage().getEnergyStored();
            int graphiteFe = graphiteCtrl.getEnergyStorage().getEnergyStored();
            if (graphiteFe <= airFe) {
                helper.fail(String.valueOf("Expected Graphite moderated reactor (" + graphiteFe
                        + " FE) to outperform Air reactor (" + airFe + " FE)"));
                return;
            }
            helper.succeed();
        });
    }

    private static void ejectWasteToAccessPort(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        ReactorAccessPortBlockEntity port = ((ReactorAccessPortBlockEntity) helper.getBlockEntity(new BlockPos(1, 2, 2)));
        port.setOutlet(true);

        ctrl.tryAssemble();
        ctrl.getSimulation().setWasteMb(2_500.0);
        ctrl.ejectWaste();

        ItemStack waste = port.getInventory().getStackInSlot(ReactorAccessPortBlockEntity.SLOT_WASTE);
        if (!waste.is(ModItems.CYANITE_INGOT.get()) || waste.getCount() != 2) {
            helper.fail(String.valueOf("Expected 2 Cyanite Ingots ejected, got: " + waste));
            return;
        }
        helper.succeed();
    }

    private static void redstonePortTogglesReactor(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        ReactorRedstonePortBlockEntity rsPort = ((ReactorRedstonePortBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 3)));
        ctrl.tryAssemble();

        rsPort.onRedstoneChanged(true);
        if (!ctrl.getSimulation().isActive()) {
            helper.fail(String.valueOf("Expected redstone signal ON to activate reactor"));
            return;
        }
        rsPort.onRedstoneChanged(false);
        if (ctrl.getSimulation().isActive()) {
            helper.fail(String.valueOf("Expected redstone signal OFF to deactivate reactor"));
            return;
        }
        helper.succeed();
    }

    private static void assemble5x5x5CheckerboardReactor(GameTestHelper helper) {
        BlockPos smallCtrlPos = build3x3x3(helper, 1, 1, 1);
        // 5x5x5 at (1, 5, 1) with 4 checkerboard fuel columns (12 fuel rods) + Graphite moderators
        int ox = 1, oy = 5, oz = 1;
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos p = new BlockPos(ox + dx, oy + dy, oz + dz);
                    boolean onX = dx == 0 || dx == 4;
                    boolean onY = dy == 0 || dy == 4;
                    boolean onZ = dz == 0 || dz == 4;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);
                    boolean rodCol = (dx == 1 && dz == 1) || (dx == 3 && dz == 1) || (dx == 1 && dz == 3) || (dx == 3 && dz == 3);
                    if (axes >= 2) {
                        helper.setBlock(p, ModBlocks.REACTOR_CASING.get());
                    } else if (axes == 1) {
                        if (dx == 2 && dy == 1 && dz == 0) {
                            helper.setBlock(p, ModBlocks.REACTOR_CONTROLLER.get());
                        } else if (dy == 4 && rodCol) {
                            helper.setBlock(p, ModBlocks.REACTOR_CONTROL_ROD.get());
                        } else {
                            helper.setBlock(p, ModBlocks.REACTOR_CASING.get());
                        }
                    } else {
                        helper.setBlock(p, rodCol ? ModBlocks.REACTOR_FUEL_ROD.get() : ModBlocks.GRAPHITE_BLOCK.get());
                    }
                }
            }
        }
        ReactorControllerBlockEntity smallCtrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(smallCtrlPos));
        ReactorControllerBlockEntity bigCtrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(new BlockPos(ox + 2, oy + 1, oz)));
        if (!bigCtrl.tryAssemble() || bigCtrl.getStructure().fuelRodCount() != 12) {
            helper.fail(String.valueOf("Expected 5x5x5 checkerboard reactor with 12 fuel rods, error: " + bigCtrl.getLastValidationError()));
            return;
        }
        smallCtrl.tryAssemble();
        smallCtrl.getSimulation().setFuelMb(4_000.0);
        bigCtrl.getSimulation().setFuelMb(16_000.0);
        smallCtrl.setActive(true);
        bigCtrl.setActive(true);

        helper.runAfterDelay(60, () -> {
            int smallFe = smallCtrl.getEnergyStorage().getEnergyStored();
            int bigFe = bigCtrl.getEnergyStorage().getEnergyStored();
            if (bigFe <= smallFe * 3) {
                helper.fail(String.valueOf("Expected 5x5x5 (12 rods + Graphite: " + bigFe + " FE) >> 3x3x3 (" + smallFe + " FE)"));
                return;
            }
            helper.succeed();
        });
    }

    private static void activeReactorBoilsWaterToSteam(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        // Replace Power Tap (3, 2, 2) with Coolant Port (Inlet) and Redstone Port (2, 2, 3) with Coolant Port (Outlet)
        helper.setBlock(new BlockPos(3, 2, 2), ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                .setValue(ReactorCoolantPortBlock.OUTLET, false));
        helper.setBlock(new BlockPos(2, 2, 3), ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                .setValue(ReactorCoolantPortBlock.OUTLET, true));

        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        if (!ctrl.tryAssemble() || ctrl.getSimulation().getMode() != ReactorMode.ACTIVE) {
            helper.fail(String.valueOf("Expected reactor with Coolant Ports to assemble in ACTIVE mode"));
            return;
        }
        ctrl.getSimulation().setFuelMb(4_000.0);
        ctrl.getCoolantTank().fillInternal(new FluidStack(Fluids.WATER, 4_000));
        ctrl.setActive(true);

        helper.runAfterDelay(50, () -> {
            if (ctrl.getCoolantTank().getFluidAmount() >= 4_000) {
                helper.fail(String.valueOf("Expected active reactor to consume Water coolant"));
                return;
            }
            if (ctrl.getSteamTank().isEmpty()) {
                helper.fail(String.valueOf("Expected active reactor to produce Steam"));
                return;
            }
            if (ctrl.getEnergyStorage().getEnergyStored() != 0) {
                helper.fail(String.valueOf("Active reactor should not produce passive FE directly"));
                return;
            }
            helper.succeed();
        });
    }

    private static void turbineAssemblesAndGeneratesFe(GameTestHelper helper) {
        // 5x5x5 Steam Turbine at (1, 1, 1)
        int ox = 1, oy = 1, oz = 1;
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos p = new BlockPos(ox + dx, oy + dy, oz + dz);
                    boolean onX = dx == 0 || dx == 4;
                    boolean onY = dy == 0 || dy == 4;
                    boolean onZ = dz == 0 || dz == 4;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);

                    if (axes >= 2) {
                        helper.setBlock(p, ModBlocks.TURBINE_CASING.get());
                    } else if (axes == 1) {
                        if (dx == 2 && dy == 1 && dz == 0) {
                            helper.setBlock(p, ModBlocks.TURBINE_CONTROLLER.get());
                        } else if (dx == 2 && dy == 2 && dz == 4) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_BEARING.get());
                        } else if (dx == 4 && dy == 2 && dz == 2) {
                            helper.setBlock(p, ModBlocks.TURBINE_POWER_TAP.get());
                        } else if (dx == 0 && dy == 2 && dz == 2) {
                            helper.setBlock(p, ModBlocks.TURBINE_FLUID_PORT.get().defaultBlockState()
                                    .setValue(TurbineFluidPortBlock.OUTLET, false));
                        } else {
                            helper.setBlock(p, ModBlocks.TURBINE_CASING.get());
                        }
                    } else {
                        if (dx == 2 && dy == 2) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_SHAFT.get());
                        } else if (dz == 1 && ((dx == 2 && (dy == 1 || dy == 3)) || (dy == 2 && (dx == 1 || dx == 3)))) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_BLADE.get());
                        } else if (dz == 3 && dx >= 1 && dx <= 3 && dy >= 1 && dy <= 3) {
                            helper.setBlock(p, Blocks.GOLD_BLOCK);
                        } else {
                            helper.setBlock(p, Blocks.AIR);
                        }
                    }
                }
            }
        }

        TurbineControllerBlockEntity turbine = ((TurbineControllerBlockEntity) helper.getBlockEntity(new BlockPos(ox + 2, oy + 1, oz)));
        if (!turbine.tryAssemble() || !turbine.isAssembled()) {
            helper.fail(String.valueOf("Expected 5x5x5 turbine to assemble, got: " + turbine.getLastValidationError()));
            return;
        }
        turbine.getSteamTank().fillInternal(new FluidStack(ModFluids.STEAM_SOURCE.get(), 32_000));
        turbine.setActive(true);

        helper.runAfterDelay(25, () -> {
            if (turbine.getSimulation().getRpm() <= 10.0) {
                helper.fail(String.valueOf("Expected turbine RPM to spin up, got: " + turbine.getSimulation().getRpm()));
                return;
            }
            if (turbine.getEnergyStorage().getEnergyStored() <= 0) {
                helper.fail(String.valueOf("Expected turbine to generate FE from Steam"));
                return;
            }
            if (turbine.getWaterTank().isEmpty()) {
                helper.fail(String.valueOf("Expected turbine to condense Steam back into Water"));
                return;
            }
            BlockPos tapAbs = helper.absolutePos(new BlockPos(ox + 4, oy + 2, oz + 2));
            IEnergyStorage cap = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, tapAbs, null);
            if (cap == null || cap.getEnergyStored() <= 0) {
                helper.fail(String.valueOf("Expected Turbine Power Tap to expose FE"));
                return;
            }
            helper.succeed();
        });
    }

    @SuppressWarnings("deprecation")
    private static void cyaniteReprocessorMakesBlutonium(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, ModBlocks.CYANITE_REPROCESSOR.get());
        CyaniteReprocessorBlockEntity rep = ((CyaniteReprocessorBlockEntity) helper.getBlockEntity(pos));

        rep.getInventory().setStackInSlot(CyaniteReprocessorBlockEntity.SLOT_INPUT, new ItemStack(ModItems.CYANITE_INGOT.get(), 2));
        rep.getWaterTank().fillInternal(new FluidStack(Fluids.WATER, 1_000));

        IEnergyStorage energyCap = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(pos), null);
        if (energyCap == null) {
            helper.fail(String.valueOf("Expected Reprocessor Energy capability"));
            return;
        }
        for (int i = 0; i < 15; i++) {
            energyCap.receiveEnergy(2_000, false);
        }

        helper.runAfterDelay(110, () -> {
            ItemStack out = rep.getInventory().getStackInSlot(CyaniteReprocessorBlockEntity.SLOT_OUTPUT);
            if (!out.is(ModItems.BLUTONIUM_INGOT.get()) || out.getCount() != 1) {
                helper.fail(String.valueOf("Expected 1 Blutonium Ingot, got: " + out));
                return;
            }
            if (!out.is(ModTags.REACTOR_FUEL)) {
                helper.fail(String.valueOf("Expected Blutonium Ingot to be tagged as reactor_fuel"));
                return;
            }
            helper.succeed();
        });
    }

    private static void demoSetupClosedLoopTurbine(GameTestHelper helper) {
        DemoSetup.DemoResult result = DemoSetup.build(helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
        if (!(helper.getLevel().getBlockEntity(result.reactorControllerPos()) instanceof ReactorControllerBlockEntity reactorCtrl)
                || !reactorCtrl.isAssembled()
                || reactorCtrl.getSimulation().getMode() != ReactorMode.ACTIVE) {
            helper.fail(String.valueOf("Expected demo reactor to assemble in ACTIVE mode"));
            return;
        }
        if (!(helper.getLevel().getBlockEntity(result.turbineControllerPos()) instanceof TurbineControllerBlockEntity turbineCtrl)
                || !turbineCtrl.isAssembled()
                || turbineCtrl.getStructure().bladeCount() != 12
                || turbineCtrl.getStructure().coilCount() != 16) {
            helper.fail(String.valueOf("Expected demo turbine to assemble with 12 blades and 16 coils"));
            return;
        }

        // Without Voltaic Dynamics (no 1.21.1 build) there are no pipes: the turbine only burns its pre-filled
        // steam and spins down, so just check that the closed loop's turbine starts turning.
        if (!net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(
                ResourceLocation.fromNamespaceAndPath("voltaicdynamics", "fluid_cable"))) {
            helper.runAfterDelay(10, () -> {
                if (turbineCtrl.getSimulation().getRpm() <= 0.0) {
                    helper.fail(String.valueOf("Expected demo turbine to spin up from its pre-filled steam"));
                    return;
                }
                helper.succeed();
            });
            return;
        }

        helper.runAfterDelay(60, () -> {
            if (turbineCtrl.getSimulation().getRpm() <= 100.0) {
                helper.fail(String.valueOf("Expected demo turbine RPM > 100, got: " + turbineCtrl.getSimulation().getRpm()));
                return;
            }
            if (turbineCtrl.getSimulation().getLastGeneratedFe() <= 0) {
                helper.fail(String.valueOf("Expected demo turbine to generate FE"));
                return;
            }
            BlockPos shaftPos = turbineCtrl.getStructure().shaftPositions().getFirst();
            BlockPos bladePos = turbineCtrl.getStructure().bladePositions().getFirst();
            if (!helper.getLevel().getBlockState(shaftPos).getValue(TurbineRotorShaftBlock.SPINNING)
                    || !helper.getLevel().getBlockState(bladePos).getValue(TurbineRotorBladeBlock.SPINNING)) {
                helper.fail(String.valueOf("Expected turbine shaft and blade blocks to mark SPINNING=true while turning"));
                return;
            }
            IEnergyStorage accumCap = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, result.accumulatorPos(), null);
            if (accumCap != null && accumCap.getEnergyStored() <= 0) {
                helper.fail(String.valueOf("Expected Voltaic accumulator in demo setup to receive FE"));
                return;
            }
            helper.succeed();
        });
    }

    private static void redstonePortOutputEnergyThreshold(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        BlockPos rsRelPos = new BlockPos(2, 2, 3);
        ReactorRedstonePortBlockEntity rsPort = ((ReactorRedstonePortBlockEntity) helper.getBlockEntity(rsRelPos));
        ctrl.tryAssemble();

        rsPort.setThresholdPercent(50);
        rsPort.setMode(RedstonePortMode.OUTPUT_ENERGY_BELOW);
        rsPort.tickOutput(ctrl);
        if (rsPort.getEmittedSignal() != 15) {
            helper.fail(String.valueOf("Expected OUTPUT_ENERGY_BELOW to emit 15 when buffer is empty"));
            return;
        }

        // Fill buffer past 50%
        int targetFe = (int) (ctrl.getEnergyStorage().getMaxEnergyStored() * 0.75);
        ctrl.getEnergyStorage().generateEnergy(targetFe);
        rsPort.tickOutput(ctrl);
        if (rsPort.getEmittedSignal() != 0) {
            helper.fail(String.valueOf("Expected OUTPUT_ENERGY_BELOW to turn OFF above 50%, got: " + rsPort.getEmittedSignal()));
            return;
        }

        rsPort.setMode(RedstonePortMode.OUTPUT_ENERGY_ABOVE);
        rsPort.tickOutput(ctrl);
        if (rsPort.getEmittedSignal() != 15) {
            helper.fail(String.valueOf("Expected OUTPUT_ENERGY_ABOVE to emit 15 at 75% buffer"));
            return;
        }
        helper.succeed();
    }

    private static void redstonePortAnalogControlRods(GameTestHelper helper) {
        BlockPos ctrlPos = build3x3x3(helper, 1, 1, 1);
        ReactorControllerBlockEntity ctrl = ((ReactorControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        ReactorRedstonePortBlockEntity rsPort = ((ReactorRedstonePortBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 3)));
        ReactorControlRodBlockEntity rod = ((ReactorControlRodBlockEntity) helper.getBlockEntity(new BlockPos(2, 3, 2)));
        ctrl.tryAssemble();

        rsPort.setMode(RedstonePortMode.CONTROL_ROD_SIGNAL);
        rsPort.onRedstoneInput(8); // round(8 * 100 / 15) = 53%
        if (rod.getInsertion() != 53) {
            helper.fail(String.valueOf("Expected analog signal 8 to set rod insertion to 53%, got: " + rod.getInsertion()));
            return;
        }
        helper.succeed();
    }

    private static void datapackModeratorsAndCoilsLoaded(GameTestHelper helper) {
        if (ReactorModerator.getDatapackCount() < 11) {
            helper.fail(String.valueOf("Expected >= 11 datapack reactor moderators, got: " + ReactorModerator.getDatapackCount()));
            return;
        }
        if (TurbineCoil.getDatapackCount() < 8) {
            helper.fail(String.valueOf("Expected >= 8 datapack turbine coils, got: " + TurbineCoil.getDatapackCount()));
            return;
        }
        ReactorModerator copperMod = ReactorModerator.of(Blocks.COPPER_BLOCK.defaultBlockState());
        TurbineCoil copperCoil = TurbineCoil.of(Blocks.COPPER_BLOCK.defaultBlockState());
        if (Math.abs(copperMod.moderation() - 1.38) > 1e-4 || copperCoil == null || Math.abs(copperCoil.efficiency() - 1.1) > 1e-4) {
            helper.fail(String.valueOf("Expected tag-matched Copper datapack moderator & coil to resolve"));
            return;
        }
        helper.succeed();
    }

    private static void turbineShutdownSpinsDown(GameTestHelper helper) {
        int ox = 1, oy = 1, oz = 1;
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos p = new BlockPos(ox + dx, oy + dy, oz + dz);
                    boolean onX = dx == 0 || dx == 4;
                    boolean onY = dy == 0 || dy == 4;
                    boolean onZ = dz == 0 || dz == 4;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);
                    if (axes >= 2) {
                        helper.setBlock(p, ModBlocks.TURBINE_CASING.get());
                    } else if (axes == 1) {
                        if (dx == 2 && dy == 1 && dz == 0) helper.setBlock(p, ModBlocks.TURBINE_CONTROLLER.get());
                        else if (dx == 2 && dy == 2 && dz == 4) helper.setBlock(p, ModBlocks.TURBINE_ROTOR_BEARING.get());
                        else helper.setBlock(p, ModBlocks.TURBINE_CASING.get());
                    } else {
                        if (dx == 2 && dy == 2) helper.setBlock(p, ModBlocks.TURBINE_ROTOR_SHAFT.get());
                        else if (dz == 1 && ((dx == 2 && (dy == 1 || dy == 3)) || (dy == 2 && (dx == 1 || dx == 3)))) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_BLADE.get());
                        } else if (dz == 3 && dx >= 1 && dx <= 3 && dy >= 1 && dy <= 3) {
                            helper.setBlock(p, Blocks.GOLD_BLOCK);
                        } else {
                            helper.setBlock(p, Blocks.AIR);
                        }
                    }
                }
            }
        }

        TurbineControllerBlockEntity turbine = ((TurbineControllerBlockEntity) helper.getBlockEntity(new BlockPos(ox + 2, oy + 1, oz)));
        if (!turbine.tryAssemble() || !turbine.isAssembled()) {
            helper.fail(String.valueOf("Expected 5x5x5 turbine to assemble"));
            return;
        }

        // Verify pure simulation settles from over-capped energy to 0 RPM cleanly when offline
        TurbineSimulation sim = new TurbineSimulation();
        sim.setActive(false);
        sim.setRotorEnergy(50_000_000.0);
        for (int i = 0; i < 200; i++) {
            sim.tick(turbine.getStructure(), 0, 100_000, 1.0, 2_000);
        }
        if (sim.getRpm() != 0.0 || sim.getLastGeneratedFe() != 0) {
            helper.fail(String.valueOf("Expected turbine sim to spin down to 0 RPM, got RPM=" + sim.getRpm()));
            return;
        }

        // Verify live world BE clamps oversized rotorEnergy and drops visibly within 20 ticks
        turbine.getSimulation().setRotorEnergy(50_000_000.0);
        turbine.setActive(false);
        helper.runAfterDelay(20, () -> {
            if (turbine.getSimulation().getRpm() >= 2800.0) {
                helper.fail(String.valueOf("Expected turbine RPM to spin down below 2800 after shutdown, got: "
                        + turbine.getSimulation().getRpm()));
                return;
            }
            helper.succeed();
        });
    }

    private static BlockPos build5x5x5Turbine(GameTestHelper helper, int ox, int oy, int oz) {
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                for (int dz = 0; dz < 5; dz++) {
                    BlockPos p = new BlockPos(ox + dx, oy + dy, oz + dz);
                    boolean onX = dx == 0 || dx == 4;
                    boolean onY = dy == 0 || dy == 4;
                    boolean onZ = dz == 0 || dz == 4;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);

                    if (axes >= 2) {
                        helper.setBlock(p, ModBlocks.TURBINE_CASING.get());
                    } else if (axes == 1) {
                        if (dx == 2 && dy == 1 && dz == 0) {
                            helper.setBlock(p, ModBlocks.TURBINE_CONTROLLER.get());
                        } else if (dx == 1 && dy == 1 && dz == 0) {
                            helper.setBlock(p, ModBlocks.TURBINE_REDSTONE_PORT.get());
                        } else if (dx == 2 && dy == 2 && dz == 4) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_BEARING.get());
                        } else if (dx == 4 && dy == 2 && dz == 2) {
                            helper.setBlock(p, ModBlocks.TURBINE_POWER_TAP.get());
                        } else if (dx == 0 && dy == 2 && dz == 2) {
                            helper.setBlock(p, ModBlocks.TURBINE_FLUID_PORT.get().defaultBlockState()
                                    .setValue(TurbineFluidPortBlock.OUTLET, false));
                        } else {
                            helper.setBlock(p, ModBlocks.TURBINE_CASING.get());
                        }
                    } else {
                        if (dx == 2 && dy == 2) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_SHAFT.get());
                        } else if (dz == 1 && ((dx == 2 && (dy == 1 || dy == 3)) || (dy == 2 && (dx == 1 || dx == 3)))) {
                            helper.setBlock(p, ModBlocks.TURBINE_ROTOR_BLADE.get());
                        } else if (dz == 3 && dx >= 1 && dx <= 3 && dy >= 1 && dy <= 3) {
                            helper.setBlock(p, Blocks.GOLD_BLOCK);
                        } else {
                            helper.setBlock(p, Blocks.AIR);
                        }
                    }
                }
            }
        }
        return new BlockPos(ox + 2, oy + 1, oz);
    }

    private static void turbineRedstonePortControlsAndOutputs(GameTestHelper helper) {
        BlockPos ctrlPos = build5x5x5Turbine(helper, 1, 1, 1);
        TurbineControllerBlockEntity turbine = ((TurbineControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        TurbineRedstonePortBlockEntity rsPort = ((TurbineRedstonePortBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 1)));
        if (!turbine.tryAssemble() || turbine.getStructure().redstonePorts().size() != 1) {
            helper.fail(String.valueOf("Expected turbine with Redstone Port to assemble, got: " + turbine.getLastValidationError()));
            return;
        }

        // Input: TOGGLE_ON_SIGNAL
        rsPort.setMode(TurbineRedstonePortMode.TOGGLE_ON_SIGNAL);
        rsPort.onRedstoneChanged(true);
        if (!turbine.getSimulation().isActive()) {
            helper.fail(String.valueOf("Expected TOGGLE_ON_SIGNAL ON to activate turbine"));
            return;
        }
        rsPort.onRedstoneChanged(false);
        if (turbine.getSimulation().isActive()) {
            helper.fail(String.valueOf("Expected TOGGLE_ON_SIGNAL OFF to deactivate turbine"));
            return;
        }

        // Input: TOGGLE_COILS_SIGNAL
        rsPort.setMode(TurbineRedstonePortMode.TOGGLE_COILS_SIGNAL);
        rsPort.onRedstoneChanged(false);
        if (turbine.getSimulation().isInductorEngaged()) {
            helper.fail(String.valueOf("Expected TOGGLE_COILS_SIGNAL OFF to disengage coils"));
            return;
        }
        rsPort.onRedstoneChanged(true);
        if (!turbine.getSimulation().isInductorEngaged()) {
            helper.fail(String.valueOf("Expected TOGGLE_COILS_SIGNAL ON to engage coils"));
            return;
        }

        // Input: FLOW_RATE_SIGNAL (analog 15 -> max flow)
        rsPort.setMode(TurbineRedstonePortMode.FLOW_RATE_SIGNAL);
        rsPort.onRedstoneInput(0);
        if (turbine.getSimulation().getMaxSteamFlowMb() != 0) {
            helper.fail(String.valueOf("Expected FLOW_RATE_SIGNAL 0 to set flow to 0"));
            return;
        }
        rsPort.onRedstoneInput(15);
        if (turbine.getSimulation().getMaxSteamFlowMb() <= 0) {
            helper.fail(String.valueOf("Expected FLOW_RATE_SIGNAL 15 to set flow > 0"));
            return;
        }

        // Output: RPM Above / Below (threshold 50% -> 1,500 RPM)
        rsPort.setThresholdPercent(50);
        rsPort.setMode(TurbineRedstonePortMode.OUTPUT_RPM_BELOW);
        rsPort.tickOutput(turbine);
        if (rsPort.getEmittedSignal() != 15) {
            helper.fail(String.valueOf("Expected OUTPUT_RPM_BELOW to emit 15 at 0 RPM"));
            return;
        }
        rsPort.setMode(TurbineRedstonePortMode.OUTPUT_RPM_ABOVE);
        rsPort.tickOutput(turbine);
        if (rsPort.getEmittedSignal() != 0) {
            helper.fail(String.valueOf("Expected OUTPUT_RPM_ABOVE to emit 0 at 0 RPM"));
            return;
        }

        // Output: Energy Below / Above
        rsPort.setMode(TurbineRedstonePortMode.OUTPUT_ENERGY_BELOW);
        rsPort.tickOutput(turbine);
        if (rsPort.getEmittedSignal() != 15) {
            helper.fail(String.valueOf("Expected OUTPUT_ENERGY_BELOW to emit 15 when buffer empty"));
            return;
        }
        turbine.getEnergyStorage().generateEnergy((int) (turbine.getEnergyStorage().getMaxEnergyStored() * 0.75));
        rsPort.setMode(TurbineRedstonePortMode.OUTPUT_ENERGY_ABOVE);
        rsPort.tickOutput(turbine);
        if (rsPort.getEmittedSignal() != 15) {
            helper.fail(String.valueOf("Expected OUTPUT_ENERGY_ABOVE to emit 15 at 75% buffer"));
            return;
        }
        helper.succeed();
    }

    private static void turbineVentModes(GameTestHelper helper) {
        BlockPos ctrlPos = build5x5x5Turbine(helper, 1, 1, 1);
        TurbineControllerBlockEntity turbine = ((TurbineControllerBlockEntity) helper.getBlockEntity(ctrlPos));
        if (!turbine.tryAssemble()) {
            helper.fail(String.valueOf("Expected turbine to assemble"));
            return;
        }

        turbine.setActive(true);
        turbine.getSteamTank().fillInternal(new FluidStack(ModFluids.STEAM_SOURCE.get(), 10_000));
        turbine.getWaterTank().fillInternal(new FluidStack(Fluids.WATER, turbine.getWaterTank().getCapacity()));

        // Vent NONE: water tank is full -> must NOT consume steam
        turbine.setVentMode(TurbineVentMode.NONE);
        TurbineControllerBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(ctrlPos),
                helper.getBlockState(ctrlPos), turbine);
        if (turbine.getSteamTank().getFluidAmount() != 10_000) {
            helper.fail(String.valueOf("Expected Vent NONE with full water tank to stall steam intake, steam="
                    + turbine.getSteamTank().getFluidAmount()));
            return;
        }

        // Vent OVERFLOW: consumes steam even when water tank is full
        turbine.setVentMode(TurbineVentMode.OVERFLOW);
        TurbineControllerBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(ctrlPos),
                helper.getBlockState(ctrlPos), turbine);
        if (turbine.getSteamTank().getFluidAmount() >= 10_000) {
            helper.fail(String.valueOf("Expected Vent OVERFLOW to consume steam when water tank is full; active=" + turbine.getSimulation().isActive() + " flow=" + turbine.getSimulation().getMaxSteamFlowMb() + " steam=" + turbine.getSteamTank().getFluidAmount()));
            return;
        }

        // Vent ALL: drains water tank completely while consuming steam
        int beforeAll = turbine.getSteamTank().getFluidAmount();
        turbine.setVentMode(TurbineVentMode.ALL);
        TurbineControllerBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(ctrlPos),
                helper.getBlockState(ctrlPos), turbine);
        if (turbine.getSteamTank().getFluidAmount() >= beforeAll || !turbine.getWaterTank().isEmpty()) {
            helper.fail(String.valueOf("Expected Vent ALL to consume steam and empty water tank, water="
                    + turbine.getWaterTank().getFluidAmount()));
            return;
        }

        helper.succeed();
    }

    private static void jeiDisplayEntriesPopulated(GameTestHelper helper) {
        List<ReactorModerator.DisplayEntry> moderators = ReactorModerator.getDisplayEntries();
        if (moderators.size() < 11) {
            helper.fail(String.valueOf("Expected >= 11 moderator display entries, got: " + moderators.size()));
            return;
        }
        boolean hasWaterFluid = moderators.stream().anyMatch(e -> e.fluid() == Fluids.WATER);
        if (!hasWaterFluid) {
            helper.fail(String.valueOf("Expected Water fluid entry in moderator display list"));
            return;
        }

        List<TurbineCoil.DisplayEntry> coils = TurbineCoil.getDisplayEntries();
        if (coils.size() < 8) {
            helper.fail(String.valueOf("Expected >= 8 coil display entries, got: " + coils.size()));
            return;
        }
        double firstScore = coils.getFirst().coil().efficiency() * coils.getFirst().coil().bonus();
        double lastScore = coils.getLast().coil().efficiency() * coils.getLast().coil().bonus();
        if (firstScore <= lastScore) {
            helper.fail(String.valueOf("Expected coil display entries sorted descending"));
            return;
        }
        helper.succeed();
    }
}
