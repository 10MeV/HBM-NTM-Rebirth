package com.hbm.ntm.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface DesignatorItem {
    boolean isReady(Level level, ItemStack stack, BlockPos launchPos);

    Vec3 getCoords(Level level, ItemStack stack, BlockPos launchPos);
}
