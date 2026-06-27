package com.hbm.inventory;

import com.hbm.ntm.fluid.FluidType;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy package facade for the 1.7.10 fixed fluid container tuple.
 */
@Deprecated(forRemoval = false)
public class FluidContainer {
    public ItemStack fullContainer;
    public ItemStack emptyContainer;
    public FluidType type;
    public int content;

    public FluidContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type, int content) {
        this.fullContainer = fullContainer == null ? null : fullContainer.copy();
        this.emptyContainer = emptyContainer == null || emptyContainer.isEmpty() ? null : emptyContainer.copy();
        this.type = type;
        this.content = content;
    }
}
