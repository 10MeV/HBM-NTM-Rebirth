package com.hbm.ntm.api.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface DesignatorItem {
    boolean isReady(Level level, ItemStack stack, BlockPos launchPos);

    Vec3 getCoords(Level level, ItemStack stack, BlockPos launchPos);

    /**
     * Resolves the legacy missile-designator contract: launch targets are horizontal X/Z coordinates.
     * The supplied launch Y is retained only for callers that need a complete {@link BlockPos} carrier.
     */
    default BlockPos getHorizontalTarget(Level level, ItemStack stack, BlockPos launchPos) {
        Vec3 coords = getCoords(level, stack, launchPos);
        return BlockPos.containing(coords.x, launchPos.getY(), coords.z);
    }
}
