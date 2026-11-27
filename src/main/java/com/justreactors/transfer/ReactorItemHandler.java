package com.justreactors.transfer;

import com.justreactors.compat.nbt.ValueInput;
import com.justreactors.compat.nbt.ValueOutput;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Simple slot-based item handler on Forge 1.20.1's {@link ItemStackHandler}.
 */
public class ReactorItemHandler extends ItemStackHandler {
    public ReactorItemHandler(int size) {
        super(size);
    }

    public int size() {
        return getSlots();
    }

    @Override
    protected void onContentsChanged(int slot) {
        onSlotChanged(slot);
    }

    protected void onSlotChanged(int slot) {
    }

    public void markChanged(int slot) {
        onSlotChanged(slot);
    }

    public void serialize(ValueOutput output) {
        output.tag().merge(serializeNBT());
    }

    public void deserialize(ValueInput input) {
        deserializeNBT(input.tag());
    }
}
