package com.justreactors.exchanger;

import com.justreactors.transfer.Caps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Fluid port of the Heat Exchanger. Right-click cycles Input / Coolant Output / Steam Output. */
public class HeatExchangerFluidPortBlock extends Block implements EntityBlock {
    public static final EnumProperty<ExchangerPortMode> MODE = EnumProperty.create("mode", ExchangerPortMode.class);

    public HeatExchangerFluidPortBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MODE, ExchangerPortMode.INPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatExchangerFluidPortBlockEntity(pos, state);
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
        // Buckets and tanks fill or empty the port like a pipe would.
        if (!stack.isEmpty() && Caps.item(stack, ForgeCapabilities.FLUID_HANDLER_ITEM) != null
                && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private InteractionResult jrUseWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ExchangerPortMode next = state.getValue(MODE).next();
        level.setBlock(pos, state.setValue(MODE, next), 3);
        player.displayClientMessage(Component.translatable(next.translationKey()).withStyle(switch (next) {
            case INPUT -> ChatFormatting.GOLD;
            case OUTPUT_COOLANT -> ChatFormatting.AQUA;
            case OUTPUT_STEAM -> ChatFormatting.WHITE;
        }), true);
        return InteractionResult.CONSUME;
    }
}
