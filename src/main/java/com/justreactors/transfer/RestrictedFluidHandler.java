package com.justreactors.transfer;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

/**
 * View of a tank that only allows insertion (of matching fluids) or only extraction.
 * Both the indexed and the slotless insert/extract overloads are restricted.
 */
public class RestrictedFluidHandler extends DelegatingResourceHandler<FluidResource> {
    private final boolean canInsert;
    private final boolean canExtract;
    private final Predicate<FluidResource> filter;

    private RestrictedFluidHandler(ResourceHandler<FluidResource> tank, boolean canInsert, boolean canExtract,
                                   Predicate<FluidResource> filter) {
        super(tank);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
        this.filter = filter;
    }

    public static RestrictedFluidHandler insertOnly(ResourceHandler<FluidResource> tank, Predicate<FluidResource> filter) {
        return new RestrictedFluidHandler(tank, true, false, filter);
    }

    public static RestrictedFluidHandler extractOnly(ResourceHandler<FluidResource> tank) {
        return new RestrictedFluidHandler(tank, false, true, r -> true);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return canInsert && filter.test(resource) && super.isValid(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (!canInsert || !filter.test(resource)) return 0;
        return super.insert(index, resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (!canInsert || !filter.test(resource)) return 0;
        return super.insert(resource, amount, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return canExtract ? super.extract(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        return canExtract ? super.extract(resource, amount, transaction) : 0;
    }
}
