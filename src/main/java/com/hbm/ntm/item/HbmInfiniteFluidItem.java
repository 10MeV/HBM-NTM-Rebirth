package com.hbm.ntm.item;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class HbmInfiniteFluidItem extends HbmFluidContainerItem {
    private final FluidType fluidType;
    private final int amount;
    private final int chance;
    private final String displayName;

    public HbmInfiniteFluidItem(Properties properties, FluidType fluidType, int amount, int chance, String displayName) {
        super(properties.stacksTo(1), com.hbm.ntm.fluid.HbmFluidContainerRules.ContainerKind.FLUID_BARREL, Integer.MAX_VALUE);
        this.fluidType = fluidType;
        this.amount = amount;
        this.chance = Math.max(1, chance);
        this.displayName = displayName;
    }

    public FluidType getType() {
        return fluidType;
    }

    public int getAmount() {
        return amount;
    }

    public int getChance() {
        return chance;
    }

    public boolean allowPressure(int pressure) {
        return fluidType == null || pressure == 0;
    }

    @Override
    public int getFill(ItemStack stack) {
        return amount;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return fluidType != null && fluidType != HbmFluids.NONE && type == fluidType;
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return providesFluid(type, stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(displayName);
    }
}
