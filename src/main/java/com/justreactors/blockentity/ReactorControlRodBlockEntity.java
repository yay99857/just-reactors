package com.justreactors.blockentity;

import com.justreactors.menu.ReactorControlRodMenu;
import com.justreactors.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class ReactorControlRodBlockEntity extends ReactorPartBlockEntity implements MenuProvider {
    private int insertion = 0; // 0% = fully retracted (max power), 100% = fully inserted (off)

    public ReactorControlRodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROL_ROD.get(), pos, state);
    }

    public int getInsertion() {
        return insertion;
    }

    public void setInsertion(int insertion) {
        int clamped = Math.max(0, Math.min(100, insertion));
        if (this.insertion != clamped) {
            this.insertion = clamped;
            setChanged();
        }
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Insertion", insertion);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        insertion = Math.max(0, Math.min(100, input.getIntOr("Insertion", 0)));
    }
}
