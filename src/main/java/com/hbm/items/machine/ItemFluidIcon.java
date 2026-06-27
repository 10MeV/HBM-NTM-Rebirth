package com.hbm.items.machine;

import com.hbm.inventory.FluidStack;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.item.FluidIconItem;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy package facade for 1.7.10 fluid icon item helpers.
 */
@Deprecated(forRemoval = false)
public class ItemFluidIcon extends FluidIconItem {
    private static final String TAG_AMOUNT = "hbm_fluid_amount";
    private static final String TAG_PRESSURE = "hbm_fluid_pressure";
    private static final String LEGACY_TAG_AMOUNT = "fill";
    private static final String LEGACY_TAG_PRESSURE = "pressure";

    public ItemFluidIcon() {
        super(new Properties());
    }

    public static ItemStack addQuantity(ItemStack stack, int amount) {
        if (amount <= 0 || stack == null || stack.isEmpty()) {
            return stack;
        }
        stack.getOrCreateTag().putInt(TAG_AMOUNT, amount);
        stack.getOrCreateTag().putInt(LEGACY_TAG_AMOUNT, amount);
        return stack;
    }

    public static ItemStack addPressure(ItemStack stack, int pressure) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        int clamped = Math.max(0, pressure);
        stack.getOrCreateTag().putInt(TAG_PRESSURE, clamped);
        stack.getOrCreateTag().putInt(LEGACY_TAG_PRESSURE, clamped);
        return stack;
    }

    public static ItemStack make(FluidStack stack) {
        return stack == null ? ItemStack.EMPTY : make(stack.type, stack.fill, stack.pressure);
    }

    public static ItemStack make(FluidType fluid, int amount) {
        return make(fluid, amount, 0);
    }

    public static ItemStack make(FluidType fluid, int amount, int pressure) {
        return FluidIconItem.make(fluid, amount, pressure);
    }

    public static int getQuantity(ItemStack stack) {
        return FluidIconItem.getAmount(stack);
    }

    public static int getPressure(ItemStack stack) {
        return FluidIconItem.getPressure(stack);
    }
}
