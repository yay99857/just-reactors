package com.justreactors.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.justreactors.block.ReactorControlRodBlock;
import com.justreactors.menu.ReactorControlRodMenu;
import com.justreactors.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ReactorControlRodBlockEntity extends ReactorPartBlockEntity implements MenuProvider {
    private int insertion = 0; // 0% = fully retracted (max power), 100% = fully inserted (off)

    public ReactorControlRodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROL_ROD.get(), pos, state);
    }

    public static int levelForInsertion(int insertion) {
        if (insertion >= 75) return 3;
        if (insertion >= 50) return 2;
        if (insertion >= 25) return 1;
        return 0;
    }

    public int getInsertion() {
        return insertion;
    }

    public void setInsertion(int insertion) {
        int clamped = Math.max(0, Math.min(100, insertion));
        if (this.insertion != clamped) {
            this.insertion = clamped;
            setChanged();
            syncBlockLevel();
        }
    }

    public void syncBlockLevel() {
        if (level == null || level.isClientSide()) return;
        BlockState st = getBlockState();
        int target = levelForInsertion(insertion);
        if (st.hasProperty(ReactorControlRodBlock.LEVEL) && st.getValue(ReactorControlRodBlock.LEVEL) != target) {
            level.setBlock(worldPosition, st.setValue(ReactorControlRodBlock.LEVEL, target), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncBlockLevel();
    }

    public void applyToAllRods() {
        ReactorControllerBlockEntity controller = getController();
        if (controller != null) {
            controller.setAllControlRods(this.insertion);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.reactor_control_rod");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReactorControlRodMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        output.putInt("Insertion", insertion);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);
        insertion = Math.max(0, Math.min(100, input.getIntOr("Insertion", 0)));
    }
}
