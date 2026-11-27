package com.justreactors.client;

import com.justreactors.JustReactors;
import com.justreactors.demo.DemoSetup;
import com.justreactors.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * DEV ONLY ({@code ./gradlew runShots}): builds the closed-loop demo on a flat plain and saves
 * screenshots to {@code run/screenshots}, then closes the game.
 */
@EventBusSubscriber(modid = JustReactors.MOD_ID, value = Dist.CLIENT)
public final class ShotDirector {
    private static final org.slf4j.Logger LOG = com.mojang.logging.LogUtils.getLogger();
    private static final boolean ON = Boolean.getBoolean("justreactors.shots");
    /** {@code -PuiShots=true}: only the GUI screenshots, skipping the scenery and the GIF. */
    private static final boolean UI_ONLY = Boolean.getBoolean("justreactors.shots.ui");

    private record Step(int delay, Runnable action) {
    }

    private static final List<Step> steps = new ArrayList<>();
    private static int index = -1, timer;
    private static int cx = 3000, cz = 3000, g;
    private static DemoSetup.DemoResult demo;

    private ShotDirector() {
    }

    private static void server(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> action.accept(server.getPlayerList().getPlayer(mc.player.getUUID())));
    }

    private static void cmd(ServerPlayer p, String c) {
        p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack()
                .withPermission(net.minecraft.server.permissions.LevelBasedPermissionSet.OWNER).withSuppressedOutput(), c);
    }

    /** Puts the camera at (x, y, z) relative to the demo origin, looking at (tx, ty, tz). */
    private static void look(ServerPlayer p, double x, double y, double z, double tx, double ty, double tz) {
        x += cx; y += g; z += cz; tx += cx; ty += g; tz += cz;
        double dx = tx - x, dy = ty - (y + p.getEyeHeight()), dz = tz - z;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        p.getAbilities().flying = true;
        p.onUpdateAbilities();
        p.teleportTo(p.level(), x, y, z, Set.of(), yaw, pitch, true);
        p.setDeltaMovement(0, 0, 0);
    }

    private static void shot(String name) {
        Minecraft mc = Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory, "justreactors_" + name + ".png", mc.gameRenderer.mainRenderTarget(), 1,
                c -> LOG.info("shot {}", name));
    }

    private static void hideHud(Minecraft mc, boolean hidden) {
        if (mc.gui.hud.isHidden() != hidden) mc.gui.hud.toggle();
    }

    private static void setup(ServerPlayer p) {
        for (String c : List.of("gamerule doDaylightCycle false", "gamerule doWeatherCycle false",
                "gamerule doMobSpawning false", "time set 6000", "weather clear", "gamemode creative"))
            cmd(p, c);
        var found = p.level().findClosestBiome3d(b -> b.is(net.minecraft.world.level.biome.Biomes.PLAINS),
                new BlockPos(6000, 64, 6000), 6400, 32, 64);
        if (found != null) {
            cx = found.getFirst().getX();
            cz = found.getFirst().getZ();
        }
        cmd(p, "forceload add " + (cx - 48) + " " + (cz - 48) + " " + (cx + 48) + " " + (cz + 48));
        p.teleportTo(p.level(), cx, 200, cz, Set.of(), 0, 0, true);
    }

    private static void flattenAndBuild(ServerPlayer p) {
        ServerLevel level = p.level();
        g = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = -40; x <= 56; x++) {
            for (int z = -40; z <= 48; z++) {
                for (int y = g - 8; y <= g + 30; y++) {
                    m.set(cx + x, y, cz + z);
                    Block b = y >= g ? Blocks.AIR : y == g - 1 ? Blocks.GRASS_BLOCK : y > g - 4 ? Blocks.DIRT : Blocks.STONE;
                    level.setBlock(m, b.defaultBlockState(), 2);
                }
            }
        }
        for (Entity e : level.getEntities((Entity) null, new AABB(cx - 50, g - 10, cz - 50, cx + 60, g + 40, cz + 60),
                e -> !(e instanceof Player))) e.discard();

        // the demo platform sits one block into the ground, flush with the grass
        BlockPos origin = new BlockPos(cx, g - 1, cz);
        demo = DemoSetup.build(level, origin);
        // the demo's guide signs are in Portuguese; the CurseForge page is in English
        for (int dx : new int[]{3, 6, 10, 14}) level.setBlock(origin.offset(dx, 1, 1), Blocks.AIR.defaultBlockState(), 3);
        g = g - 1; // coordinates below are relative to the platform
    }

    private static void openMenu(ServerPlayer p, BlockPos pos) {
        if (p.level().getBlockEntity(pos) instanceof MenuProvider provider) p.openMenu(provider, pos);
    }

    private static BlockPos find(ServerPlayer p, Block block) {
        BlockPos o = new BlockPos(cx, g, cz);
        for (BlockPos q : BlockPos.betweenClosed(o.offset(-2, 0, -2), o.offset(16, 10, 10))) {
            if (p.level().getBlockState(q).is(block)) return q.immutable();
        }
        return null;
    }

    private static void openFound(ServerPlayer p, Block block) {
        BlockPos q = find(p, block);
        if (q != null) openMenu(p, q);
        else LOG.warn("shots: no {} in the demo", block);
    }

    /** A size^3 box of {@code casing} filled with {@code core}; the north face gets glass with the controller in the middle. */
    private static void box(ServerLevel level, BlockPos o, int size, Block casing, Block glass, Block core, Block controller) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    int axes = (x == 0 || x == size - 1 ? 1 : 0) + (y == 0 || y == size - 1 ? 1 : 0) + (z == 0 || z == size - 1 ? 1 : 0);
                    Block b = axes == 0 ? core : axes >= 2 ? casing : z == 0 ? glass : casing;
                    level.setBlock(o.offset(x, y, z), b.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        level.setBlock(o.offset(size / 2, 1, 0), controller.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static BlockPos exchangerOrigin() {
        return new BlockPos(cx + 16, g + 1, cz + 2);
    }

    private static BlockPos batteryOrigin() {
        return new BlockPos(cx + 23, g + 1, cz + 2);
    }

    /** Builds a 5x5x5 Heat Exchanger and a 5x5x5 Battery east of the turbine, already running. */
    private static void buildNewMachines(ServerPlayer p) {
        ServerLevel level = p.level();
        BlockPos ex = exchangerOrigin();
        box(level, ex, 5, ModBlocks.HEAT_EXCHANGER_CASING.get(), ModBlocks.HEAT_EXCHANGER_GLASS.get(),
                ModBlocks.HEAT_EXCHANGER_TUBE.get(), ModBlocks.HEAT_EXCHANGER_CONTROLLER.get());
        var port = ModBlocks.HEAT_EXCHANGER_FLUID_PORT.get().defaultBlockState();
        level.setBlock(ex.offset(4, 1, 2), port, Block.UPDATE_ALL);
        level.setBlock(ex.offset(4, 2, 2), port.setValue(com.justreactors.exchanger.HeatExchangerFluidPortBlock.MODE,
                com.justreactors.exchanger.ExchangerPortMode.OUTPUT_COOLANT), Block.UPDATE_ALL);
        level.setBlock(ex.offset(4, 3, 2), port.setValue(com.justreactors.exchanger.HeatExchangerFluidPortBlock.MODE,
                com.justreactors.exchanger.ExchangerPortMode.OUTPUT_STEAM), Block.UPDATE_ALL);

        BlockPos bat = batteryOrigin();
        box(level, bat, 5, ModBlocks.BATTERY_CASING.get(), ModBlocks.BATTERY_GLASS.get(),
                ModBlocks.BATTERY_CELL.get(), ModBlocks.BATTERY_CONTROLLER.get());
        level.setBlock(bat.offset(4, 2, 1), ModBlocks.BATTERY_POWER_PORT.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(bat.offset(4, 2, 3), ModBlocks.BATTERY_POWER_PORT.get().defaultBlockState()
                .setValue(com.justreactors.battery.BatteryPowerPortBlock.OUTLET, true), Block.UPDATE_ALL);
        level.setBlock(bat.offset(4, 3, 2), ModBlocks.BATTERY_REDSTONE_SENSOR.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void fillNewMachines(ServerPlayer p) {
        ServerLevel level = p.level();
        if (level.getBlockEntity(exchangerOrigin().offset(2, 1, 0)) instanceof com.justreactors.exchanger.HeatExchangerControllerBlockEntity ex
                && ex.tryAssemble()) {
            ex.getHotTank().fillInternal(new net.neoforged.neoforge.fluids.FluidStack(com.justreactors.registry.ModFluids.HOT_COOLANT_SOURCE.get(), 40_000));
            ex.getWaterTank().fillInternal(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 70_000));
        } else {
            LOG.warn("shots: heat exchanger did not assemble");
        }
        if (level.getBlockEntity(batteryOrigin().offset(2, 1, 0)) instanceof com.justreactors.battery.BatteryControllerBlockEntity bat
                && bat.tryAssemble()) {
            bat.getEnergyStorage().generateEnergy((int) (bat.getEnergyStorage().getMaxEnergyStored() * 0.62));
        } else {
            LOG.warn("shots: battery did not assemble");
        }
    }

    private static void reactorOnCoolant(ServerPlayer p) {
        ServerLevel level = p.level();
        if (level.getBlockEntity(demo.reactorControllerPos()) instanceof com.justreactors.blockentity.ReactorControllerBlockEntity r) {
            r.getCoolantTank().setFluid(new net.neoforged.neoforge.fluids.FluidStack(com.justreactors.registry.ModFluids.COOLANT_SOURCE.get(), 30_000));
            r.getSteamTank().setFluid(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
        }
    }

    /** Loose machines next to the camera, for the screens the demo has no block for. */
    private static BlockPos loose(int i) {
        return new BlockPos(cx + 1 + i * 2, g + 1, cz - 3);
    }

    /** Moves the cursor to a point given in GUI coordinates relative to a centred w x h screen. */
    private static void hover(Minecraft mc, int w, int h, int gx, int gy) {
        var win = mc.getWindow();
        double scale = win.getGuiScale();
        int left = (win.getGuiScaledWidth() - w) / 2, top = (win.getGuiScaledHeight() - h) / 2;
        mc.mouseHandler.onMove(win.handle(), (left + gx) * scale, (top + gy) * scale, 0, 0);
    }

    private static void ui(Minecraft mc, String name, Consumer<ServerPlayer> open) {
        add(5, () -> mc.gui.setScreen(null));
        add(5, () -> server(open));
        add(20, () -> parkMouse(mc));
        add(20, () -> shot(name));
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        if (!ON) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        if (index < 0) {
            mc.options.pauseOnLostFocus = false; // keep running while another window is in front
            build(mc);
            index = 0;
            timer = 100;
        }
        if (index >= steps.size()) return;
        if (--timer > 0) return;
        steps.get(index).action().run();
        index++;
        if (index < steps.size()) timer = steps.get(index).delay();
    }

    private static void add(int wait, Runnable r) {
        steps.add(new Step(wait, r));
    }

    /** Parks the cursor in a corner so no tooltip shows up over a screen. */
    private static void parkMouse(Minecraft mc) {
        long handle = mc.getWindow().handle();
        mc.mouseHandler.onMove(handle, 2, 2, 0, 0);
    }

    private static final int GIF_FRAMES = 48;
    private static final int REACTOR_GIF_FRAMES = 36;

    private static void build(Minecraft mc) {
        add(1, () -> server(ShotDirector::setup));
        add(200, () -> server(ShotDirector::flattenAndBuild));
        add(20, () -> hideHud(mc, true));
        add(5, () -> server(ShotDirector::buildNewMachines));
        add(40, () -> server(ShotDirector::fillNewMachines));
        // Layout (relative to the platform): reactor x1..5 z2..6 y1..5, turbine x8..12 z2..6 y1..7.
        // The camera looks south (+Z), so +X (the turbine) is on the left of the picture.
        if (!UI_ONLY) {
            // overview from the front, high, close enough to fill the frame
            add(20, () -> server(p -> look(p, 1, 8.5, -6.5, 7, 2.5, 4)));
            add(200, () -> shot("overview"));
            // the reactor's west glass with the fuel rods
            add(5, () -> server(p -> look(p, -3, 2.2, 4.5, 3.5, 3, 4.5)));
            add(60, () -> shot("reactor_glass"));
            for (int i = 0; i < REACTOR_GIF_FRAMES; i++) {
                final int frame = i;
                add(1, () -> shot(String.format("reactor_gif_%02d", frame)));
            }
            // the reactor from the corner
            add(5, () -> server(p -> look(p, -3.5, 5.5, -3.5, 3.5, 3, 4.5)));
            add(60, () -> shot("reactor"));
            // the turbine from the east corner
            add(5, () -> server(p -> look(p, 17.5, 6, -3.5, 10.5, 3.5, 4.5)));
            add(60, () -> shot("turbine"));
            // the closed loop between them
            add(5, () -> server(p -> look(p, 6.5, 5, -3.5, 6.5, 2.5, 4)));
            add(60, () -> shot("loop"));
        } else {
            add(400, () -> LOG.info("shots: letting the reactor warm up"));
        }
        // controller screens
        add(5, () -> hideHud(mc, false));
        add(5, () -> server(p -> {
            look(p, 3.5, 1, -1, 3.5, 2.5, 2.5);
            openMenu(p, demo.reactorControllerPos());
        }));
        add(20, () -> parkMouse(mc));
        add(20, () -> shot("reactor_controller"));
        add(5, () -> mc.gui.setScreen(null));
        add(5, () -> server(p -> {
            look(p, 10.5, 1, -1, 10.5, 2.5, 2.5);
            openMenu(p, demo.turbineControllerPos());
        }));
        add(20, () -> parkMouse(mc));
        add(20, () -> shot("turbine_controller"));
        add(5, () -> mc.gui.setScreen(null));
        // the other screens and two tooltips
        add(5, () -> server(p -> {
            look(p, 3.5, 1, -1, 3.5, 2.5, 2.5);
            openMenu(p, demo.reactorControllerPos());
        }));
        add(20, () -> hover(mc, 244, 178, 22, 80));
        add(20, () -> shot("ui_reactor_tooltip"));
        add(5, () -> mc.gui.setScreen(null));
        add(5, () -> server(p -> openMenu(p, demo.turbineControllerPos())));
        add(20, () -> hover(mc, 244, 196, 132, 80));
        add(20, () -> shot("ui_turbine_tooltip"));
        if (net.neoforged.fml.ModList.get().isLoaded("carryon")) {
            add(5, () -> server(p -> {
                BlockPos loose = new BlockPos(cx + 22, g + 1, cz - 2);
                p.level().setBlock(loose, ModBlocks.BATTERY_POWER_PORT.get().defaultBlockState(), Block.UPDATE_ALL);
                LOG.info("shots carryon: assembled port protected={}, loose port protected={}",
                        com.justreactors.compat.carryon.CarryOnCompat.isProtected(p.level(), batteryOrigin().offset(4, 2, 1)),
                        com.justreactors.compat.carryon.CarryOnCompat.isProtected(p.level(), loose));
            }));
        }
        // the heat exchanger and the battery
        add(5, () -> {
            mc.gui.setScreen(null);
            hideHud(mc, true);
            server(p -> look(p, 21.5, 6.5, -5.5, 24, 3, 4.5));
        });
        add(60, () -> shot("exchanger_battery"));
        add(5, () -> hideHud(mc, false));
        ui(mc, "ui_heat_exchanger", p -> {
            look(p, 18.5, 1, -1.5, 18.5, 2, 2);
            openMenu(p, exchangerOrigin().offset(2, 1, 0));
        });
        add(10, () -> hover(mc, 244, 176, 200, 36));
        add(20, () -> shot("ui_heat_exchanger_status"));
        ui(mc, "ui_battery", p -> {
            look(p, 25.5, 1, -1.5, 25.5, 2, 2);
            openMenu(p, batteryOrigin().offset(2, 1, 0));
        });
        ui(mc, "ui_battery_sensor", p -> {
            look(p, 29.5, 2, 4.5, 27, 4, 4.5);
            openMenu(p, batteryOrigin().offset(4, 3, 2));
        });
        add(5, () -> server(ShotDirector::reactorOnCoolant));
        add(80, () -> LOG.info("shots: reactor heating coolant"));
        ui(mc, "ui_reactor_coolant", p -> {
            look(p, 3.5, 1, -1, 3.5, 2.5, 2.5);
            openMenu(p, demo.reactorControllerPos());
        });
        ui(mc, "ui_control_rod", p -> openFound(p, ModBlocks.REACTOR_CONTROL_ROD.get()));
        if (net.neoforged.fml.ModList.get().isLoaded("roughlyenoughitems")) {
            for (String cat : List.of("heat_exchanging", "reprocessing", "moderators")) {
                add(5, () -> mc.gui.setScreen(null));
                add(5, () -> com.justreactors.compat.rei.ReiShots.open(cat));
                add(30, () -> parkMouse(mc));
                add(10, () -> shot("rei_" + cat));
            }
        }
        ui(mc, "ui_access_port", p -> openFound(p, ModBlocks.REACTOR_ACCESS_PORT.get()));
        add(5, () -> server(p -> {
            Block[] loose = {ModBlocks.REACTOR_REDSTONE_PORT.get(), ModBlocks.TURBINE_REDSTONE_PORT.get(), ModBlocks.CYANITE_REPROCESSOR.get()};
            for (int i = 0; i < loose.length; i++) p.level().setBlock(loose(i), loose[i].defaultBlockState(), Block.UPDATE_ALL);
        }));
        ui(mc, "ui_reactor_redstone", p -> openMenu(p, loose(0)));
        ui(mc, "ui_turbine_redstone", p -> openMenu(p, loose(1)));
        ui(mc, "ui_reprocessor", p -> openMenu(p, loose(2)));
        add(5, () -> {
            mc.gui.setScreen(null);
            server(p -> {
                for (int i = 0; i < 3; i++) p.level().setBlock(loose(i), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            });
        });
        if (!UI_ONLY) {
            // GIF: the rotor spinning, straight through the turbine's front glass, one frame per tick
            add(5, () -> {
                hideHud(mc, true);
                server(p -> {
                    look(p, 10.5, 2.0, -1.8, 10.5, 3.8, 4.5);
                    if (p.level().getBlockEntity(demo.turbineControllerPos()) instanceof com.justreactors.turbine.blockentity.TurbineControllerBlockEntity turbine) {
                        turbine.setMaxSteamFlowMb(400);
                        turbine.getSteamTank().setFluid(new net.neoforged.neoforge.fluids.FluidStack(com.justreactors.registry.ModFluids.STEAM_SOURCE.get(), 32_000));
                        turbine.getSimulation().setRotorEnergy(180_000.0);
                        turbine.setActive(true);
                    }
                });
            });
            for (int i = 0; i < GIF_FRAMES; i++) {
                final int frame = i;
                add(i == 0 ? 15 : 1, () -> shot(String.format("gif_%02d", frame)));
            }
            // night, closer, so the glass glow reads
            add(10, () -> server(p -> {
                cmd(p, "time set 18000");
                if (p.level().getBlockEntity(demo.turbineControllerPos()) instanceof com.justreactors.turbine.blockentity.TurbineControllerBlockEntity turbine) {
                    turbine.getSteamTank().setFluid(new net.neoforged.neoforge.fluids.FluidStack(com.justreactors.registry.ModFluids.STEAM_SOURCE.get(), 32_000));
                    turbine.getSimulation().setRotorEnergy(180_000.0);
                    turbine.setActive(true);
                }
            }));
            add(10, () -> server(p -> look(p, 1, 8.5, -6.5, 7, 2.5, 4)));
            add(30, () -> shot("overview_night"));
        }
        add(20, () -> LOG.info("shots done"));
        add(20, () -> Minecraft.getInstance().stop());
    }
}
