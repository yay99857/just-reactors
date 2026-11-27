package com.justreactors.block;

import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.InteractionHand;
import com.justreactors.blockentity.ReactorControlRodBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ReactorControlRodBlock extends ReactorPartBlock implements EntityBlock {
    public ReactorControlRodBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorControlRodBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return hand == InteractionHand.MAIN_HAND ? jrUseWithoutItem(state, level, pos, player, hit) : InteractionResult.PASS;
    }

    private InteractionResult jrUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ReactorControlRodBlockEntity rod && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, rod, pos);
        }
        return InteractionResult.CONSUME;
    }
}
