package com.justreactors.blockentity;

import com.justreactors.block.ReactorAccessPortBlock;
import com.justreactors.datagen.ModTags;
import com.justreactors.menu.ReactorAccessPortMenu;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModItems;
import com.justreactors.transfer.ReactorItemHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Item I/O port for inserting reactor fuel (slot 0) and extracting Cyanite waste (slot 1).
 */
public class ReactorAccessPortBlockEntity extends ReactorPartBlockEntity implements MenuProvider {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_WASTE = 1;

    private final ReactorItemHandler inventory = new ReactorItemHandler(2) {
        @Override
        protected void onSlotChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SLOT_FUEL && stack.is(ModTags.REACTOR_FUEL);
        }
    };

    /** Pipes/hoppers can insert fuel into slot 0 and extract waste from slot 1. */
    private final ResourceHandler<ItemResource> automationHandler = new DelegatingResourceHandler<>(inventory) {
        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (slot != SLOT_FUEL) return 0;
            return super.insert(slot, resource, amount, transaction);
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (slot != SLOT_WASTE) return 0;
            return super.extract(slot, resource, amount, transaction);
        }
    };

    public ReactorAccessPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_ACCESS_PORT.get(), pos, state);
    }

    public boolean isOutlet() {
        return getBlockState().getValue(ReactorAccessPortBlock.OUTLET);
    }

    public void setOutlet(boolean outlet) {
        if (level != null && !level.isClientSide() && isOutlet() != outlet) {
            level.setBlock(worldPosition, getBlockState().setValue(ReactorAccessPortBlock.OUTLET, outlet), 3);
            setChanged();
        }
    }

    public void toggleOutlet() {
        setOutlet(!isOutlet());
    }

    /** Consumes up to maxIngots from the fuel input slot; returns how many were consumed. */
    public int consumeFuelIngots(int maxIngots) {
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);
        if (fuel.isEmpty() || !fuel.is(ModTags.REACTOR_FUEL) || maxIngots <= 0) return 0;
        int taken = Math.min(maxIngots, fuel.getCount());
        if (taken == fuel.getCount()) {
            inventory.setStackInSlot(SLOT_FUEL, ItemStack.EMPTY);
        } else {
            inventory.setStackInSlot(SLOT_FUEL, fuel.copyWithCount(fuel.getCount() - taken));
        }
        return taken;
    }

    /** Inserts up to maxIngots of Cyanite into the waste output slot; returns how many fit. */
    public int insertWasteIngots(int maxIngots) {
        if (maxIngots <= 0) return 0;
        ItemStack existing = inventory.getStackInSlot(SLOT_WASTE);
        ItemStack cyanite = new ItemStack(ModItems.CYANITE_INGOT.get());
        if (existing.isEmpty()) {
            int toPut = Math.min(maxIngots, cyanite.getMaxStackSize());
            inventory.setStackInSlot(SLOT_WASTE, new ItemStack(ModItems.CYANITE_INGOT.get(), toPut));
            return toPut;
        }
        if (!existing.is(ModItems.CYANITE_INGOT.get())) return 0;
        int space = existing.getMaxStackSize() - existing.getCount();
        int toPut = Math.min(maxIngots, space);
        if (toPut > 0) {
            inventory.setStackInSlot(SLOT_WASTE, existing.copyWithCount(existing.getCount() + toPut));
        }
        return toPut;
    }

    public ReactorItemHandler getInventory() {
        return inventory;
    }

    @Nullable
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return automationHandler;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            SimpleContainer drops = new SimpleContainer(inventory.getSlots());
            for (int i = 0; i < inventory.getSlots(); i++) {
                drops.setItem(i, inventory.getStackInSlot(i));
            }
            Containers.dropContents(level, worldPosition, drops);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.justreactors.reactor_access_port");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReactorAccessPortMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Inventory").ifPresent(inventory::deserialize);
    }
}
