package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class FluidLoaderStandard extends FluidLoadingHandler {
    @Override
    public boolean fillItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in, out) || tank == null) {
            return false;
        }
        return HbmFluidItemTransfer.unloadTankToSlot(wrap(slots), in, out, tank);
    }

    @Override
    public boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in, out) || tank == null) {
            return false;
        }
        return HbmFluidItemTransfer.loadTankFromSlot(wrap(slots), in, out, tank);
    }
}
