package com.hbm.lib;

import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

/**
 * Narrow legacy facade for Energy MK2 helpers from 1.7.10 {@code com.hbm.lib.Library}.
 */
@Deprecated(forRemoval = false)
public final class Library {
    public static final Direction POS_X = Direction.EAST;
    public static final Direction NEG_X = Direction.WEST;
    public static final Direction POS_Y = Direction.UP;
    public static final Direction NEG_Y = Direction.DOWN;
    public static final Direction POS_Z = Direction.SOUTH;
    public static final Direction NEG_Z = Direction.NORTH;

    private Library() {
    }

    public static boolean canConnect(BlockGetter level, int x, int y, int z, Direction cableSide) {
        return canConnect(level, new BlockPos(x, y, z), cableSide);
    }

    public static boolean canConnect(BlockGetter level, BlockPos targetPos, Direction cableSide) {
        return HbmEnergyConnectionUtil.canConnectLegacy(level, targetPos, cableSide);
    }

    public static long chargeItemsFromTE(ItemStack[] slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return clampLegacyPower(power, maxPower);
        }
        ItemStack stack = slots[index];
        return stack == null ? clampLegacyPower(power, maxPower)
                : HbmBatteryTransfer.chargeItemsFromPower(stack, power, maxPower);
    }

    public static long chargeItemsFromTE(List<ItemStack> slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return clampLegacyPower(power, maxPower);
        }
        ItemStack stack = slots.get(index);
        return stack == null ? clampLegacyPower(power, maxPower)
                : HbmBatteryTransfer.chargeItemsFromPower(stack, power, maxPower);
    }

    public static long chargeTEFromItems(ItemStack[] slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return power;
        }
        ItemStack stack = slots[index];
        return stack == null ? power : HbmBatteryTransfer.chargePowerFromItem(stack, power, maxPower);
    }

    public static long chargeTEFromItems(List<ItemStack> slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return power;
        }
        ItemStack stack = slots.get(index);
        return stack == null ? power : HbmBatteryTransfer.chargePowerFromItem(stack, power, maxPower);
    }

    private static boolean isValidSlot(ItemStack[] slots, int index) {
        return slots != null && index >= 0 && index < slots.length;
    }

    private static boolean isValidSlot(List<ItemStack> slots, int index) {
        return slots != null && index >= 0 && index < slots.size();
    }

    private static long clampLegacyPower(long power, long maxPower) {
        if (power < 0L) {
            return 0L;
        }
        if (power > maxPower) {
            return maxPower;
        }
        return power;
    }
}
