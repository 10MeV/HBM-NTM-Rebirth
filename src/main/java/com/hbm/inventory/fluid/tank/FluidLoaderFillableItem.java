package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.api.fluid.IFillableItem;
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
        return slots[in] != null && !slots[in].isEmpty() && slots[in].getItem() instanceof IFillableItem
                && tank.getPressure() == 0;
    }

    public boolean fill(ItemStack stack, FluidTank tank) {
        boolean fillable = stack != null && !stack.isEmpty() && stack.getItem() instanceof IFillableItem
                && tank != null && tank.getPressure() == 0;
        HbmFluidItemTransfer.TransferResult result = fillResult(stack, tank);
        copyResultIntoOriginal(stack, result.stack());
        return fillable;
    }

    private HbmFluidItemTransfer.TransferResult fillResult(ItemStack stack, FluidTank tank) {
        if (stack == null || stack.isEmpty() || tank == null) {
            return new HbmFluidItemTransfer.TransferResult(stack == null ? ItemStack.EMPTY : stack, 0);
        }
        return HbmFluidItemTransfer.fillFillableItemFromTank(stack, tank, Integer.MAX_VALUE, false);
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
        return slots[in] != null && !slots[in].isEmpty() && slots[in].getItem() instanceof IFillableItem
                && tank.getFill() == tank.getMaxFill();
    }

    public boolean empty(ItemStack stack, FluidTank tank) {
        boolean fillable = stack != null && !stack.isEmpty() && stack.getItem() instanceof IFillableItem;
        HbmFluidItemTransfer.TransferResult result = emptyResult(stack, tank);
        copyResultIntoOriginal(stack, result.stack());
        return fillable && tank != null && tank.getFill() == tank.getMaxFill();
    }

    private HbmFluidItemTransfer.TransferResult emptyResult(ItemStack stack, FluidTank tank) {
        if (stack == null || stack.isEmpty() || tank == null) {
            return new HbmFluidItemTransfer.TransferResult(stack == null ? ItemStack.EMPTY : stack, 0);
        }
        return HbmFluidItemTransfer.drainFillableItemToTank(stack, tank, Integer.MAX_VALUE, false);
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
