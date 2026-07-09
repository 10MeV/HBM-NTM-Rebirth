package com.hbm.ntm.util;

import com.hbm.ntm.api.common.HalfLifeType;
import com.hbm.ntm.config.RtgConfig;
import com.hbm.ntm.item.RtgPelletItem;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy-name RTG utility facade.
 */
@Deprecated(forRemoval = false)
public final class RTGUtil {
    private RTGUtil() {
    }

    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        return HbmRtgUtil.getLifespan(halfLife, type, realYears);
    }

    public static short getPower(RtgPelletItem fuel, ItemStack stack) {
        return RtgConfig.scaleRtgPower() ? RtgPelletItem.getScaledPower(fuel, stack) : fuel.getHeat(stack);
    }

    public static boolean hasHeat(ItemStack[] inventory, int[] rtgSlots) {
        for (int slot : rtgSlots) {
            ItemStack stack = inventory[slot];
            if (stack != null && stack.getItem() instanceof RtgPelletItem) {
                return true;
            }
        }
        return false;
    }

    public static int updateRTGs(ItemStack[] inventory, int[] rtgSlots) {
        int newHeat = 0;
        for (int slot : rtgSlots) {
            ItemStack stack = inventory[slot];
            if (stack == null || !(stack.getItem() instanceof RtgPelletItem pellet)) {
                continue;
            }
            newHeat += getPower(pellet, stack);
            inventory[slot] = RtgPelletItem.handleDecay(stack, pellet);
        }
        return newHeat;
    }
}
