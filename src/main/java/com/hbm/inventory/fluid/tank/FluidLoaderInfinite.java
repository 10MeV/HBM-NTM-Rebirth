package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class FluidLoaderInfinite extends FluidLoadingHandler {
    @Override
    public boolean fillItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in, out) || tank == null) {
            return false;
        }
        ItemStack stack = slots[in];
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof HbmInfiniteFluidItem infinite)) {
            return false;
        }
        if (!infinite.allowPressure(tank.getPressure())) {
            return false;
        }
        if (infinite.getType() != null && tank.getTankType() != infinite.getType()) {
            return false;
        }
        HbmFluidItemTransfer.unloadTankToInfiniteItemSlot(wrap(slots), in, tank);
        return true;
    }

    @Override
    public boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in, out) || tank == null) {
            return false;
        }
        ItemStack stack = slots[in];
        if (stack == null || stack.isEmpty()
                || !(stack.getItem() instanceof HbmInfiniteFluidItem infinite)
                || tank.getTankType() == HbmFluids.NONE) {
            return false;
        }
        if (infinite.getType() != null && tank.getTankType() != infinite.getType()) {
            return false;
        }
        HbmFluidItemTransfer.loadTankFromInfiniteItemSlot(wrap(slots), in, tank, false, false);
        return true;
    }
}
