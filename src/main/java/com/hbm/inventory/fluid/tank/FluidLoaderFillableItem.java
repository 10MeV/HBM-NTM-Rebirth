package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class FluidLoaderFillableItem extends FluidLoadingHandler {
    @Override
    public boolean fillItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in) || tank == null) {
            return false;
        }
        HbmFluidItemTransfer.TransferResult result = fillResult(slots[in], tank);
        if (result.moved()) {
            slots[in] = result.stack();
        }
        return result.moved();
    }

    public boolean fill(ItemStack stack, FluidTank tank) {
        HbmFluidItemTransfer.TransferResult result = fillResult(stack, tank);
        copyResultIntoOriginal(stack, result.stack());
        return result.moved();
    }

    private HbmFluidItemTransfer.TransferResult fillResult(ItemStack stack, FluidTank tank) {
        if (stack == null || stack.isEmpty() || tank == null) {
            return new HbmFluidItemTransfer.TransferResult(stack == null ? ItemStack.EMPTY : stack, 0);
        }
        return HbmFluidItemTransfer.fillItemFromTank(stack, tank, Integer.MAX_VALUE, false);
    }

    @Override
    public boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in) || tank == null) {
            return false;
        }
        HbmFluidItemTransfer.TransferResult result = emptyResult(slots[in], tank);
        if (result.moved()) {
            slots[in] = result.stack();
        }
        return result.moved();
    }

    public boolean empty(ItemStack stack, FluidTank tank) {
        HbmFluidItemTransfer.TransferResult result = emptyResult(stack, tank);
        copyResultIntoOriginal(stack, result.stack());
        return result.moved();
    }

    private HbmFluidItemTransfer.TransferResult emptyResult(ItemStack stack, FluidTank tank) {
        if (stack == null || stack.isEmpty() || tank == null) {
            return new HbmFluidItemTransfer.TransferResult(stack == null ? ItemStack.EMPTY : stack, 0);
        }
        return HbmFluidItemTransfer.drainItemToTank(stack, tank, Integer.MAX_VALUE, false);
    }

    private static void copyResultIntoOriginal(ItemStack original, ItemStack result) {
        if (original == null || original.isEmpty() || result == null || result.isEmpty()
                || !ItemStack.isSameItem(original, result)) {
            return;
        }
        original.setCount(result.getCount());
        original.setTag(result.getTag() == null ? null : result.getTag().copy());
    }
}
