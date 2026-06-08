package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface Insertable {
    boolean insertItem(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack);
}
