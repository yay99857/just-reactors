package com.justreactors.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Restricted views over internal handlers, exposed through capabilities (1.21.1 replacement for 26.x's
 * {@code DelegatingResourceHandler} subclasses).
 */
public final class Handlers {
    private Handlers() {
    }

    /** Fluid view that only accepts fluids matching {@code filter} and never drains. */
    public static IFluidHandler insertOnly(IFluidHandler delegate, Predicate<FluidStack> filter) {
        return new FluidView(delegate, filter, false);
    }

    /** Fluid view that can only be drained. */
    public static IFluidHandler extractOnly(IFluidHandler delegate) {
        return new FluidView(delegate, stack -> false, true);
    }

    /** Item view where insertion and extraction are limited to the given slots. */
    public static IItemHandler slots(IItemHandler delegate, IntPredicate canInsert, IntPredicate canExtract) {
        return new ItemView(delegate, canInsert, canExtract);
    }

    private record FluidView(IFluidHandler delegate, Predicate<FluidStack> fillFilter, boolean canDrain)
            implements IFluidHandler {
        @Override
        public int getTanks() {
            return delegate.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return delegate.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return delegate.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return fillFilter.test(stack) && delegate.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !fillFilter.test(resource)) return 0;
            return delegate.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return canDrain ? delegate.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return canDrain ? delegate.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    }

    private record ItemView(IItemHandler delegate, IntPredicate canInsert, IntPredicate canExtract)
            implements IItemHandler {
        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return canInsert.test(slot) ? delegate.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return canExtract.test(slot) ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return canInsert.test(slot) && delegate.isItemValid(slot, stack);
        }
    }
}
