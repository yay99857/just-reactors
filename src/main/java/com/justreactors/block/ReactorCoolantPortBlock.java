package com.justreactors.block;

import com.justreactors.transfer.Caps;
import com.justreactors.blockentity.ReactorCoolantPortBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid I/O port for Active-Cooled reactors:
 * Inlet (outlet=false) accepts Water; Outlet (outlet=true) outputs Steam.
 */
public class ReactorCoolantPortBlock extends ReactorPartBlock implements EntityBlock {
    public static final BooleanProperty OUTLET = BooleanProperty.create("outlet");

    public ReactorCoolantPortBlock(Properties properties) {
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
        return new ReactorCoolantPortBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        InteractionResult held = jrUseItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
        if (held != InteractionResult.PASS) return held;
        return hand == InteractionHand.MAIN_HAND ? jrUseWithoutItem(state, level, pos, player, hit) : InteractionResult.PASS;
    }

    private InteractionResult jrUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!stack.isEmpty() && Caps.item(stack, ForgeCapabilities.FLUID_HANDLER_ITEM) != null) {
            if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    private InteractionResult jrUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        boolean nextOutlet = !state.getValue(OUTLET);
        level.setBlock(pos, state.setValue(OUTLET, nextOutlet), 3);
        player.displayClientMessage(Component.translatable(
                nextOutlet ? "message.justreactors.coolant_port.outlet" : "message.justreactors.coolant_port.inlet")
                .withStyle(nextOutlet ? ChatFormatting.AQUA : ChatFormatting.BLUE), true);
        return InteractionResult.CONSUME;
    }
}
