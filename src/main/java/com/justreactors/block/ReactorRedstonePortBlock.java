package com.justreactors.block;

import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.InteractionHand;
import com.justreactors.blockentity.ReactorRedstonePortBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ReactorRedstonePortBlock extends ReactorPartBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ReactorRedstonePortBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorRedstonePortBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return hand == InteractionHand.MAIN_HAND ? jrUseWithoutItem(state, level, pos, player, hit) : InteractionResult.PASS;
    }

    private InteractionResult jrUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ReactorRedstonePortBlockEntity port && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, port, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ReactorRedstonePortBlockEntity port && port.getMode().isOutput()) {
            return port.getEmittedSignal();
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos) instanceof ReactorRedstonePortBlockEntity port)) return;
        if (port.getMode().isOutput()) return;

        int signal = level.getBestNeighborSignal(pos);
        boolean powered = signal > 0;
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
        port.onNeighborSignal(signal);
    }
}
