package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import net.minecraft.world.item.ItemStack;

public interface IFillableItem {
    boolean acceptsFluid(FluidType type, ItemStack stack);

    int tryFill(FluidType type, int amount, ItemStack stack);

    boolean providesFluid(FluidType type, ItemStack stack);

    int tryEmpty(FluidType type, int amount, ItemStack stack);

    FluidType getFirstFluidType(ItemStack stack);

    int getFill(ItemStack stack);
}
