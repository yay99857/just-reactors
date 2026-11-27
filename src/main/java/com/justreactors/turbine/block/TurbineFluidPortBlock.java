package com.justreactors.turbine.block;

import net.minecraft.world.InteractionHand;
import com.justreactors.turbine.blockentity.TurbineFluidPortBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

/**
 * Fluid I/O port for Steam Turbines:
 * Inlet (outlet=false) accepts Steam; Outlet (outlet=true) outputs condensed Water.
 */
public class TurbineFluidPortBlock extends TurbinePartBlock implements EntityBlock {
    public static final BooleanProperty OUTLET = BooleanProperty.create("outlet");

    public TurbineFluidPortBlock(Properties properties) {
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
        return new TurbineFluidPortBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return hand == InteractionHand.MAIN_HAND ? jrUseWithoutItem(state, level, pos, player, hit) : InteractionResult.PASS;
    }

    private InteractionResult jrUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        boolean nextOutlet = !state.getValue(OUTLET);
        level.setBlock(pos, state.setValue(OUTLET, nextOutlet), 3);
        player.displayClientMessage(Component.translatable(
                nextOutlet ? "message.justreactors.turbine_port.outlet" : "message.justreactors.turbine_port.inlet")
                .withStyle(nextOutlet ? ChatFormatting.BLUE : ChatFormatting.AQUA), true);
        return InteractionResult.CONSUME;
    }
}
