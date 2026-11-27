package com.justreactors.battery;

import net.minecraft.world.InteractionHand;
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

/** Battery energy port. Input (outlet=false) accepts FE; Output pushes FE to neighbours. Right-click toggles. */
public class BatteryPowerPortBlock extends Block implements EntityBlock {
    public static final BooleanProperty OUTLET = BooleanProperty.create("outlet");

    public BatteryPowerPortBlock(Properties properties) {
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
        return new BatteryPowerPortBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        return hand == InteractionHand.MAIN_HAND ? jrUseWithoutItem(state, level, pos, player, hit) : InteractionResult.PASS;
    }

    private InteractionResult jrUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        boolean outlet = !state.getValue(OUTLET);
        level.setBlock(pos, state.setValue(OUTLET, outlet), 3);
        player.displayClientMessage(Component.translatable(outlet
                        ? "message.justreactors.battery_port.output" : "message.justreactors.battery_port.input")
                .withStyle(outlet ? ChatFormatting.GOLD : ChatFormatting.GREEN), true);
        return InteractionResult.CONSUME;
    }
}
