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
        if (tank.getPressure() != 0) {
            return false;
        }
        if (slots[in] == null || slots[in].isEmpty()) {
            return true;
        }
        HbmFluidItemTransfer.unloadTankToStandardContainerSlot(wrap(slots), in, out, tank);
        return false;
    }

    @Override
    public boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank) {
        if (!valid(slots, in, out) || tank == null) {
            return false;
        }
        if (slots[in] == null || slots[in].isEmpty()) {
            return true;
        }
        return HbmFluidItemTransfer.loadTankFromStandardContainerSlot(wrap(slots), in, out, tank);
    }
}
