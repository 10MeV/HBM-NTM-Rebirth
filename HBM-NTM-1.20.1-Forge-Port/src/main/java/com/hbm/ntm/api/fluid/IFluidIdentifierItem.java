package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IFluidIdentifierItem {
    FluidType getIdentifiedFluid(@Nullable Level level, BlockPos pos, ItemStack stack);

    default FluidType getPrimaryType(ItemStack stack) {
        return getIdentifiedFluid(null, BlockPos.ZERO, stack);
    }

    default boolean setIdentifiedFluid(ItemStack stack, FluidType type, boolean primary) {
        return false;
    }
}
