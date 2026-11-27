package com.justreactors.transfer;

import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Single-fluid tank on NeoForge 1.21.1's {@link FluidTank}.
 */
public class ReactorFluidTank extends FluidTank {
    @Nullable
    private final Runnable onChanged;

    public ReactorFluidTank(int capacity, Predicate<FluidStack> validator, @Nullable Runnable onChanged) {
        super(capacity, validator);
        this.onChanged = onChanged;
    }

    @Override
    public FluidTank setCapacity(int capacity) {
        this.capacity = Math.max(1_000, capacity);
        if (!fluid.isEmpty() && fluid.getAmount() > this.capacity) {
            fluid = fluid.copyWithAmount(this.capacity);
        }
        return this;
    }

    public int getSpace() {
        return Math.max(0, capacity - getFluidAmount());
    }

    @Override
    public void setFluid(FluidStack stack) {
        this.fluid = stack.isEmpty() ? FluidStack.EMPTY : stack.copyWithAmount(Math.min(capacity, stack.getAmount()));
    }

    public int fillInternal(FluidStack stack) {
        if (stack.isEmpty() || !validator.test(stack)) return 0;
        if (!fluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, stack)) return 0;
        int added = Math.min(getSpace(), stack.getAmount());
        if (added > 0) {
            setFluid(stack.copyWithAmount(fluid.getAmount() + added));
            onContentsChanged();
        }
        return added;
    }

    public FluidStack drainInternal(int maxDrain) {
        if (maxDrain <= 0 || isEmpty()) return FluidStack.EMPTY;
        int taken = Math.min(maxDrain, fluid.getAmount());
        if (taken <= 0) return FluidStack.EMPTY;
        FluidStack drained = fluid.copyWithAmount(taken);
        setFluid(taken == fluid.getAmount() ? FluidStack.EMPTY : fluid.copyWithAmount(fluid.getAmount() - taken));
        onContentsChanged();
        return drained;
    }

    @Override
    protected void onContentsChanged() {
        if (onChanged != null) onChanged.run();
    }

    public void save(ValueOutput output, String key) {
        if (!isEmpty()) {
            output.store(key, FluidStack.CODEC, getFluid());
        }
    }

    public void load(ValueInput input, String key) {
        FluidStack loaded = input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY);
        fluid = loaded.isEmpty() ? FluidStack.EMPTY : loaded.copyWithAmount(Math.min(loaded.getAmount(), capacity));
    }

    public static int pushFluid(@Nullable IFluidHandler target, FluidStack stack) {
        if (target == null || stack.isEmpty()) return 0;
        return target.fill(stack.copy(), IFluidHandler.FluidAction.EXECUTE);
    }
}
