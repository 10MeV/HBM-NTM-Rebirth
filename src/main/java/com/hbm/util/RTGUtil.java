package com.hbm.util;

import com.hbm.ntm.api.common.HalfLifeType;
import com.hbm.ntm.item.RtgPelletItem;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for RTG lifespan helpers.
 */
@Deprecated(forRemoval = false)
public final class RTGUtil {
    private RTGUtil() {
    }

    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        return com.hbm.ntm.util.RTGUtil.getLifespan(halfLife, type, realYears);
    }

    public static long getLifespan(float halfLife, com.hbm.interfaces.HalfLifeType type, boolean realYears) {
        return com.hbm.ntm.util.RTGUtil.getLifespan(halfLife, type.toModern(), realYears);
    }

    public static short getPower(RtgPelletItem fuel, ItemStack stack) {
        return com.hbm.ntm.util.RTGUtil.getPower(fuel, stack);
    }

    public static boolean hasHeat(ItemStack[] inventory, int[] rtgSlots) {
        return com.hbm.ntm.util.RTGUtil.hasHeat(inventory, rtgSlots);
    }

    public static int updateRTGs(ItemStack[] inventory, int[] rtgSlots) {
        return com.hbm.ntm.util.RTGUtil.updateRTGs(inventory, rtgSlots);
    }
}
