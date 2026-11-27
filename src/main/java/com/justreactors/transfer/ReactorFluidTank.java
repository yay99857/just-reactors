package com.justreactors.transfer;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Single-fluid tank on NeoForge 26.3's transactional fluid handler.
 */
public class ReactorFluidTank extends FluidStacksResourceHandler {
    private final Predicate<FluidStack> validator;
    @Nullable
    private final Runnable onChanged;

    public ReactorFluidTank(int capacity, Predicate<FluidStack> validator, @Nullable Runnable onChanged) {
        super(1, capacity);
        this.validator = validator;
        this.onChanged = onChanged;
    }

    public FluidStack getFluid() {
        return stacks.get(0);
    }

    public int getFluidAmount() {
        return stacks.get(0).getAmount();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1_000, capacity);
        FluidStack current = getFluid();
        if (!current.isEmpty() && current.getAmount() > this.capacity) {
            stacks.set(0, current.copyWithAmount(this.capacity));
        }
    }

    public boolean isEmpty() {
        return stacks.get(0).isEmpty();
    }

    public int getSpace() {
        return Math.max(0, capacity - getFluidAmount());
    }

    public void setFluid(FluidStack stack) {
        set(0, stack.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stack),
                stack.isEmpty() ? 0 : Math.min(capacity, stack.getAmount()));
    }

    public int fillInternal(FluidStack stack) {
        if (stack.isEmpty() || !validator.test(stack)) return 0;
        FluidStack current = getFluid();
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, stack)) return 0;
        int added = Math.min(getSpace(), stack.getAmount());
        if (added > 0) {
            int nextAmount = current.getAmount() + added;
            setFluid(stack.copyWithAmount(nextAmount));
            if (onChanged != null) onChanged.run();
        }
        return added;
    }

    public FluidStack drainInternal(int maxDrain) {
        if (maxDrain <= 0 || isEmpty()) return FluidStack.EMPTY;
        FluidStack current = getFluid();
        int taken = Math.min(maxDrain, current.getAmount());
        if (taken <= 0) return FluidStack.EMPTY;
        FluidStack drained = current.copyWithAmount(taken);
        if (taken == current.getAmount()) {
            setFluid(FluidStack.EMPTY);
        } else {
            setFluid(current.copyWithAmount(current.getAmount() - taken));
        }
        if (onChanged != null) onChanged.run();
        return drained;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return validator.test(resource.toStack(1));
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previousContents) {
        if (onChanged != null) onChanged.run();
    }

    public void save(ValueOutput output, String key) {
        if (!isEmpty()) {
            output.store(key, FluidStack.CODEC, getFluid());
        }
    }

    public void load(ValueInput input, String key) {
        FluidStack loaded = input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY);
        stacks.set(0, loaded.isEmpty() ? FluidStack.EMPTY : loaded.copyWithAmount(Math.min(loaded.getAmount(), capacity)));
    }

    @SuppressWarnings("deprecation")
    public static int pushFluid(@Nullable ResourceHandler<FluidResource> target, FluidStack stack) {
        if (target == null || stack.isEmpty()) return 0;
        try (Transaction tx = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
            int inserted = target.insert(FluidResource.of(stack), stack.getAmount(), tx);
            if (inserted > 0) tx.commit();
            return inserted;
        }
    }
}
