package com.justreactors.block;

import com.justreactors.blockentity.ReactorAccessPortBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ReactorAccessPortBlock extends ReactorPartBlock implements EntityBlock {
    /** True = Outlet (waste out), False = Inlet (fuel in). */
    public static final BooleanProperty OUTLET = BooleanProperty.create("outlet");

    public ReactorAccessPortBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OUTLET, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OUTLET);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorAccessPortBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof ReactorAccessPortBlockEntity port && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(port, pos);
        }
        return InteractionResult.CONSUME;
    }
}
