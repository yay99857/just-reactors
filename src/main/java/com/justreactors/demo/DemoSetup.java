package com.justreactors.demo;

import com.justreactors.JustReactors;
import com.justreactors.block.ReactorAccessPortBlock;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.blockentity.ReactorAccessPortBlockEntity;
import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.registry.ModBlocks;
import com.justreactors.registry.ModFluids;
import com.justreactors.registry.ModItems;
import com.justreactors.turbine.block.TurbineFluidPortBlock;
import com.justreactors.turbine.block.TurbineRotorShaftBlock;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Builds a complete closed-loop demonstration:
 * Active 5x5x5 Reactor -> Steam Fluid Pipe -> 5x7x5 Steam Turbine -> Voltaic Cable -> Accumulator,
 * plus condensed Water return pipe back to the Reactor.
 *
 * Launch via {@code ./gradlew runDemo} or in any world with {@code /justreactors demo}.
 */
public final class DemoSetup {
    private static final int PLATFORM_W = 16;
    private static final int PLATFORM_D = 9;
    private static final int CLEAR_H = 9;

    private static final Set<MinecraftServer> AUTO_DEMO_SERVERS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public record DemoResult(BlockPos reactorControllerPos, BlockPos turbineControllerPos, BlockPos accumulatorPos) {
    }

    private DemoSetup() {
    }

    public static void register(IEventBus forgeBus) {
        forgeBus.addListener(DemoSetup::onRegisterCommands);
        forgeBus.addListener(DemoSetup::onPlayerLoggedIn);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal(JustReactors.MOD_ID)
                        .then(Commands.literal("demo")
                                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                        ctx.getSource().sendFailure(Component.literal("Use este comando como jogador."));
                                        return 0;
                                    }
                                    ServerLevel level = player.serverLevel();
                                    BlockPos p = player.blockPosition();
                                    BlockPos origin = new BlockPos(p.getX() - 8, p.getY() - 1, p.getZ() + 4);
                                    DemoResult result = build(level, origin);
                                    teleportToViewerSpot(player, level, origin);
                                    sendGuideMessages(player, result);
                                    return 1;
                                }))
        );
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Boolean.getBoolean("justreactors.autoDemo")) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        MinecraftServer server = level.getServer();
        if (!AUTO_DEMO_SERVERS.add(server)) return;

        int baseX = 128;
        int baseZ = 128;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, baseX + 8, baseZ + 4) - 1;
        int groundY = Math.max(level.getMinBuildHeight(), surfaceY);
        BlockPos origin = new BlockPos(baseX, groundY, baseZ);

        DemoResult result = build(level, origin);
        teleportToViewerSpot(player, level, origin);
        sendGuideMessages(player, result);
    }

    private static void teleportToViewerSpot(ServerPlayer player, ServerLevel level, BlockPos origin) {
        double vx = origin.getX() + 8.0;
        double vy = origin.getY() + 1.5;
        double vz = origin.getZ() - 3.0;
        player.teleportTo(level, vx, vy, vz, Set.of(), 0.0f, 10.0f);
    }

    private static void sendGuideMessages(ServerPlayer player, DemoResult result) {
        player.sendSystemMessage(Component.literal("=== Just Reactors: Demonstracao Turbina + Ciclo Fechado ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("1. Esquerda: Reator 5x5x5 em Modo Ativo (Coolant Ports fervem Agua -> Vapor).")
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("2. Centro: Cano superior envia Vapor p/ Turbina; cano inferior devolve Agua condensada.")
                .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("3. Direita: Turbina 5x7x5 com Rotor Bearing na base, Shaft + 12 Pas + 16 Bobinas (Ouro/Diamante) no topo.")
                .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("4. Saida: Turbine Power Tap -> Cabo Voltaic -> Acumulador. Use '/justreactors demo' para recriar em qualquer lugar.")
                .withStyle(ChatFormatting.YELLOW));
    }

    public static DemoResult build(ServerLevel level, BlockPos origin) {
        // 1. Platform & air clearance
        for (int dx = 0; dx < PLATFORM_W; dx++) {
            for (int dz = 0; dz < PLATFORM_D; dz++) {
                boolean border = dx == 0 || dx == PLATFORM_W - 1 || dz == 0 || dz == PLATFORM_D - 1;
                Block floor = border ? Blocks.POLISHED_DEEPSLATE : Blocks.SMOOTH_STONE;
                level.setBlock(origin.offset(dx, 0, dz), floor.defaultBlockState(), Block.UPDATE_ALL);
                for (int dy = 1; dy <= CLEAR_H; dy++) {
                    level.setBlock(origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // 2. Active 5x5x5 Reactor at dx=1..5, dy=1..5, dz=2..6
        BlockPos reactorCtrlPos = origin.offset(3, 2, 2);
        BlockPos reactorAccessPos = origin.offset(2, 2, 2);
        BlockPos reactorSteamOutPos = origin.offset(5, 3, 3);
        BlockPos reactorWaterInPos = origin.offset(5, 2, 5);

        for (int dx = 1; dx <= 5; dx++) {
            for (int dy = 1; dy <= 5; dy++) {
                for (int dz = 2; dz <= 6; dz++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    boolean onX = dx == 1 || dx == 5;
                    boolean onY = dy == 1 || dy == 5;
                    boolean onZ = dz == 2 || dz == 6;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);
                    boolean rodCol = (dx == 2 || dx == 4) && (dz == 3 || dz == 5);

                    if (axes >= 2) {
                        level.setBlock(p, ModBlocks.REACTOR_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                    } else if (axes == 1) {
                        if (p.equals(reactorCtrlPos)) {
                            level.setBlock(p, ModBlocks.REACTOR_CONTROLLER.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (p.equals(reactorAccessPos)) {
                            level.setBlock(p, ModBlocks.REACTOR_ACCESS_PORT.get().defaultBlockState()
                                    .setValue(ReactorAccessPortBlock.OUTLET, false), Block.UPDATE_ALL);
                        } else if (p.equals(reactorSteamOutPos)) {
                            level.setBlock(p, ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                                    .setValue(ReactorCoolantPortBlock.OUTLET, true), Block.UPDATE_ALL);
                        } else if (p.equals(reactorWaterInPos)) {
                            level.setBlock(p, ModBlocks.REACTOR_COOLANT_PORT.get().defaultBlockState()
                                    .setValue(ReactorCoolantPortBlock.OUTLET, false), Block.UPDATE_ALL);
                        } else if (dy == 5 && rodCol) {
                            level.setBlock(p, ModBlocks.REACTOR_CONTROL_ROD.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (dz == 2 || dz == 6 || dx == 1) {
                            level.setBlock(p, ModBlocks.REACTOR_GLASS.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            level.setBlock(p, ModBlocks.REACTOR_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                        }
                    } else {
                        if (rodCol) {
                            level.setBlock(p, ModBlocks.REACTOR_FUEL_ROD.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (dx == 3 && dz == 4) {
                            level.setBlock(p, ModBlocks.GRAPHITE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            level.setBlock(p, Blocks.DIAMOND_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }

        if (level.getBlockEntity(reactorAccessPos) instanceof ReactorAccessPortBlockEntity accessPort) {
            accessPort.getInventory().setStackInSlot(ReactorAccessPortBlockEntity.SLOT_FUEL,
                    new ItemStack(ModItems.URANIUM_INGOT.get(), 64));
        }
        if (level.getBlockEntity(reactorCtrlPos) instanceof ReactorControllerBlockEntity reactorCtrl) {
            reactorCtrl.tryAssemble();
            reactorCtrl.getSimulation().setFuelMb(16_000.0);
            reactorCtrl.getSimulation().setFuelHeat(350.0);
            reactorCtrl.getSimulation().setCasingHeat(220.0);
            reactorCtrl.getCoolantTank().fillInternal(new FluidStack(Fluids.WATER, 24_000));
            reactorCtrl.setActive(true);
        }

        // 3. Steam Turbine 5x7x5 at dx=8..12, dy=1..7, dz=2..6
        BlockPos turbineCtrlPos = origin.offset(10, 2, 2);
        BlockPos turbineBearingPos = origin.offset(10, 1, 4);
        BlockPos turbineSteamInPos = origin.offset(8, 3, 3);
        BlockPos turbineWaterOutPos = origin.offset(8, 2, 5);
        BlockPos turbinePowerTapPos = origin.offset(12, 2, 4);

        for (int dx = 8; dx <= 12; dx++) {
            for (int dy = 1; dy <= 7; dy++) {
                for (int dz = 2; dz <= 6; dz++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    boolean onX = dx == 8 || dx == 12;
                    boolean onY = dy == 1 || dy == 7;
                    boolean onZ = dz == 2 || dz == 6;
                    int axes = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);

                    if (axes >= 2) {
                        level.setBlock(p, ModBlocks.TURBINE_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                    } else if (axes == 1) {
                        if (p.equals(turbineCtrlPos)) {
                            level.setBlock(p, ModBlocks.TURBINE_CONTROLLER.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (p.equals(turbineBearingPos)) {
                            level.setBlock(p, ModBlocks.TURBINE_ROTOR_BEARING.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (p.equals(turbineSteamInPos)) {
                            level.setBlock(p, ModBlocks.TURBINE_FLUID_PORT.get().defaultBlockState()
                                    .setValue(TurbineFluidPortBlock.OUTLET, false), Block.UPDATE_ALL);
                        } else if (p.equals(turbineWaterOutPos)) {
                            level.setBlock(p, ModBlocks.TURBINE_FLUID_PORT.get().defaultBlockState()
                                    .setValue(TurbineFluidPortBlock.OUTLET, true), Block.UPDATE_ALL);
                        } else if (p.equals(turbinePowerTapPos)) {
                            level.setBlock(p, ModBlocks.TURBINE_POWER_TAP.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else if (dy == 1 || dy == 7) {
                            level.setBlock(p, ModBlocks.TURBINE_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            level.setBlock(p, ModBlocks.TURBINE_GLASS.get().defaultBlockState(), Block.UPDATE_ALL);
                        }
                    } else {
                        if (dx == 10 && dz == 4) {
                            level.setBlock(p, ModBlocks.TURBINE_ROTOR_SHAFT.get().defaultBlockState()
                                    .setValue(TurbineRotorShaftBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
                        } else if (dy >= 2 && dy <= 4) {
                            boolean crossArm = (dx == 10 && (dz == 3 || dz == 5)) || (dz == 4 && (dx == 9 || dx == 11));
                            if (crossArm) {
                                level.setBlock(p, ModBlocks.TURBINE_ROTOR_BLADE.get().defaultBlockState(), Block.UPDATE_ALL);
                            }
                        } else if (dy == 5) {
                            level.setBlock(p, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        } else if (dy == 6) {
                            level.setBlock(p, Blocks.DIAMOND_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }

        if (level.getBlockEntity(turbineCtrlPos) instanceof TurbineControllerBlockEntity turbineCtrl) {
            turbineCtrl.tryAssemble();
            turbineCtrl.getSteamTank().fillInternal(new FluidStack(ModFluids.STEAM_SOURCE.get(), 8_000));
            turbineCtrl.getSimulation().setRotorEnergy(120_000.0);
            turbineCtrl.setActive(true);
        }

        // 4. Voltaic Dynamics closed-loop pipes & energy output
        BlockPos accumulatorPos = origin.offset(14, 2, 4);
        List<BlockPos> placedCables = new ArrayList<>();

        Optional<Block> fluidCable = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation("voltaicdynamics", "fluid_cable"));
        Optional<Block> voltaicCable = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation("voltaicdynamics", "voltaic_cable"));
        Optional<Block> accumulator = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation("voltaicdynamics", "elite_accumulator"))
                .or(() -> BuiltInRegistries.BLOCK.getOptional(new ResourceLocation("voltaicdynamics", "basic_accumulator")));

        accumulator.ifPresent(b -> {
            // Put a pedestal block under the accumulator so it sits flush at power-tap height
            level.setBlock(origin.offset(14, 1, 4), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(accumulatorPos, b.defaultBlockState(), Block.UPDATE_ALL);
        });

        if (fluidCable.isPresent()) {
            BlockState fc = fluidCable.get().defaultBlockState();
            for (BlockPos p : List.of(
                    origin.offset(6, 3, 3), origin.offset(7, 3, 3),
                    origin.offset(6, 2, 5), origin.offset(7, 2, 5))) {
                level.setBlock(p, fc, Block.UPDATE_ALL);
                placedCables.add(p);
            }
        }
        if (voltaicCable.isPresent()) {
            BlockPos vcPos = origin.offset(13, 2, 4);
            level.setBlock(vcPos, voltaicCable.get().defaultBlockState(), Block.UPDATE_ALL);
            placedCables.add(vcPos);
        }

        refreshCableConnections(level, placedCables);

        // 5. Explanatory signs along the front border facing North (-Z) toward viewer
        placeSign(level, origin.offset(3, 1, 1),
                "1. Reator Ativo", "5x5x5 + Coolant", "Ferve Agua ->", "Envia Vapor");
        placeSign(level, origin.offset(6, 1, 1),
                "Ciclo Fechado", "Cima: Vapor ->", "Baixo: <- Agua", "(2 Canos!)");
        placeSign(level, origin.offset(10, 1, 1),
                "2. Turbina 5x7x5", "Base: Bearing", "Meio: Shaft+Pas", "Topo: Bobinas");
        placeSign(level, origin.offset(14, 1, 1),
                "3. Energia (FE)", "Power Tap ->", "Voltaic Cable", "-> Acumulador");

        return new DemoResult(reactorCtrlPos, turbineCtrlPos, accumulatorPos);
    }

    private static void refreshCableConnections(ServerLevel level, List<BlockPos> cablePositions) {
        for (BlockPos pos : cablePositions) {
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                state = state.updateShape(dir, level.getBlockState(neighborPos), level, pos, neighborPos);
            }
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, state.getBlock());
        }
    }

    private static void placeSign(ServerLevel level, BlockPos pos, String l1, String l2, String l3, String l4) {
        level.setBlock(pos, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 8), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            Component[] lines = {
                    Component.literal(l1),
                    Component.literal(l2),
                    Component.literal(l3),
                    Component.literal(l4)
            };
            sign.setText(new SignText(lines, lines, DyeColor.WHITE, true), true);
        }
    }
}
