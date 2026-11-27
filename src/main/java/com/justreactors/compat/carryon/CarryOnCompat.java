package com.justreactors.compat.carryon;

import com.justreactors.JustReactors;
import com.justreactors.multiblock.AssembledMultiblocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Carry On picks blocks up with sneak + right-click on an empty hand. Doing that to a part of an assembled
 * reactor, turbine, heat exchanger or battery would tear the machine apart (and carry its controller's
 * contents away), so the click is cancelled before Carry On sees it. Loose parts can still be carried.
 * Uses no Carry On classes: only loaded when the mod is present.
 */
public final class CarryOnCompat {
    private CarryOnCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CarryOnCompat::onRightClickBlock);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide() || !player.isShiftKeyDown()) return;
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return;
        BlockPos pos = event.getPos();
        if (!isProtected(level, pos)) return;
        event.setCanceled(true);
        player.displayClientMessage(Component.translatable("message.justreactors.carryon.assembled").withStyle(ChatFormatting.RED), true);
    }

    public static boolean isProtected(Level level, BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        return JustReactors.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())
                && AssembledMultiblocks.isInside(level, pos);
    }
}
