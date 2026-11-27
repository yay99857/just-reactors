package com.justreactors.blockentity;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;
import com.justreactors.transfer.Caps;
import com.justreactors.transfer.Handlers;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.justreactors.JustReactorsConfig;
import com.justreactors.block.CyaniteReprocessorBlock;
import com.justreactors.registry.ModTags;
import com.justreactors.menu.CyaniteReprocessorMenu;
import com.justreactors.registry.ModBlockEntities;
import com.justreactors.registry.ModItems;
import com.justreactors.transfer.ReactorEnergyStorage;
import com.justreactors.transfer.ReactorFluidTank;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Reprocesses 2 Cyanite Ingots + 1,000 mB Water + 20,000 FE -> 1 Blutonium Ingot.
 */
public class CyaniteReprocessorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int CYANITE_PER_OP = 2;

    private final ReactorItemHandler inventory = new ReactorItemHandler(2) {
        @Override
        protected void onSlotChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SLOT_INPUT && (stack.is(ModItems.CYANITE_INGOT.get()) || stack.is(ModTags.INGOTS_CYANITE));
        }
    };

    private final IItemHandler automationHandler = Handlers.slots(inventory,
            slot -> slot == SLOT_INPUT, slot -> slot == SLOT_OUTPUT);

    private final ReactorFluidTank waterTank = new ReactorFluidTank(8_000, stack -> stack.getFluid().isSame(Fluids.WATER), this::setChanged);
    private final ReactorEnergyStorage energyStorage = new ReactorEnergyStorage(100_000, 2_000, 0, this::setChanged);

    private int progress = 0;

    public CyaniteReprocessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CYANITE_REPROCESSOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CyaniteReprocessorBlockEntity reprocessor) {
        int totalTicks = JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_TICKS);
        int energyCost = JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_ENERGY_COST);
        int waterCost = JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_WATER_COST);
        int fePerTick = Math.max(1, energyCost / Math.max(1, totalTicks));

        boolean active = false;
        if (reprocessor.canProcess(waterCost)) {
            if (reprocessor.energyStorage.consumeEnergy(fePerTick)) {
                active = true;
                reprocessor.progress++;
                if (reprocessor.progress >= totalTicks) {
                    reprocessor.progress = 0;
                    reprocessor.completeProcess(waterCost);
                }
                reprocessor.setChanged();
            }
        } else if (reprocessor.progress > 0) {
            reprocessor.progress = 0;
            reprocessor.setChanged();
        }

        if (state.getValue(CyaniteReprocessorBlock.LIT) != active) {
            level.setBlock(pos, state.setValue(CyaniteReprocessorBlock.LIT, active), 3);
        }
    }

    private boolean canProcess(int waterCost) {
        ItemStack in = inventory.getStackInSlot(SLOT_INPUT);
        if (in.isEmpty() || in.getCount() < CYANITE_PER_OP) return false;
        if (!(in.is(ModItems.CYANITE_INGOT.get()) || in.is(ModTags.INGOTS_CYANITE))) return false;
        if (waterTank.getFluidAmount() < waterCost) return false;
        ItemStack out = inventory.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) return true;
        return out.is(ModItems.BLUTONIUM_INGOT.get()) && out.getCount() < out.getMaxStackSize();
    }

    private void completeProcess(int waterCost) {
        ItemStack in = inventory.getStackInSlot(SLOT_INPUT);
        if (in.getCount() == CYANITE_PER_OP) {
            inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
        } else {
            inventory.setStackInSlot(SLOT_INPUT, in.copyWithCount(in.getCount() - CYANITE_PER_OP));
        }

        waterTank.drainInternal(waterCost);

        ItemStack out = inventory.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(ModItems.BLUTONIUM_INGOT.get(), 1));
        } else {
            inventory.setStackInSlot(SLOT_OUTPUT, out.copyWithCount(out.getCount() + 1));
        }
    }

    public ReactorItemHandler getInventory() {
        return inventory;
    }

    public ReactorFluidTank getWaterTank() {
        return waterTank;
    }

    public ReactorEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return JustReactorsConfig.get(JustReactorsConfig.REPROCESSOR_TICKS);
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return automationHandler;
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return waterTank;
    }

    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyStorage;
    }

    /** Called from the block's onRemove (1.21.1 has no BlockEntity#preRemoveSideEffects). */
    public void dropContents() {
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
        return Component.translatable("block.justreactors.cyanite_reprocessor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CyaniteReprocessorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = new ValueOutput(tag);
        inventory.serialize(output.child("Inventory"));
        waterTank.save(output, "WaterTank");
        energyStorage.serialize(output);
        output.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ValueInput input = new ValueInput(tag);
        input.child("Inventory").ifPresent(inventory::deserialize);
        waterTank.load(input, "WaterTank");
        energyStorage.deserialize(input);
        progress = input.getIntOr("Progress", 0);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return Caps.of(getItemHandler(side)).cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return Caps.of(getFluidHandler(side)).cast();
        if (cap == ForgeCapabilities.ENERGY) return Caps.of(getEnergyHandler(side)).cast();
        return super.getCapability(cap, side);
    }
}
