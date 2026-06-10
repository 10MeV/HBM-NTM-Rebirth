package com.hbm.ntm.api.entity;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.world.item.ItemStack;

public final class RadarInventoryProfile {
    public static final int SLOT_COUNT = 10;
    public static final int COMMAND_LINK_SLOT_COUNT = 8;
    public static final int SLOT_LINKER = 8;
    public static final int SLOT_BATTERY = 9;
    public static final int PLAYER_INVENTORY_START = SLOT_COUNT;
    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    public static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private RadarInventoryProfile() {
    }

    public static boolean isValidStack(int slot, ItemStack stack) {
        return (slot >= 0 && slot < COMMAND_LINK_SLOT_COUNT && isCommandLinkItem(stack))
                || (slot == SLOT_LINKER && isRadarLinker(stack))
                || (slot == SLOT_BATTERY && HbmInventoryMenuHelper.isBatteryLike(stack));
    }

    public static boolean isCommandLinkItem(ItemStack stack) {
        return stack != null && (stack.is(ModItems.SAT_RELAY.get()) || isRadarLinker(stack));
    }

    public static boolean isRadarLinker(ItemStack stack) {
        return stack != null && stack.is(ModItems.RADAR_LINKER.get());
    }

    public static boolean isMachineMenuSlot(int menuIndex) {
        return menuIndex >= 0 && menuIndex < SLOT_COUNT;
    }

    public static QuickMovePlan quickMovePlan(int menuIndex, ItemStack stack) {
        if (isMachineMenuSlot(menuIndex)) {
            return QuickMovePlan.toPlayerInventory();
        }
        if (isRadarLinker(stack)) {
            return QuickMovePlan.toScreenLinkerThenCommandLinks();
        }
        if (stack != null && stack.is(ModItems.SAT_RELAY.get())) {
            return QuickMovePlan.toCommandLinks();
        }
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return QuickMovePlan.toBattery();
        }
        return QuickMovePlan.none();
    }

    public record QuickMovePlan(int primaryStart, int primaryEnd, boolean reversePrimary,
                                int fallbackStart, int fallbackEnd, boolean reverseFallback) {
        private static QuickMovePlan toPlayerInventory() {
            return new QuickMovePlan(PLAYER_INVENTORY_START, HOTBAR_END, true, -1, -1, false);
        }

        private static QuickMovePlan toScreenLinkerThenCommandLinks() {
            return new QuickMovePlan(SLOT_LINKER, SLOT_LINKER + 1, false, 0, COMMAND_LINK_SLOT_COUNT, false);
        }

        private static QuickMovePlan toCommandLinks() {
            return new QuickMovePlan(0, COMMAND_LINK_SLOT_COUNT, false, -1, -1, false);
        }

        private static QuickMovePlan toBattery() {
            return new QuickMovePlan(SLOT_BATTERY, SLOT_BATTERY + 1, false, -1, -1, false);
        }

        private static QuickMovePlan none() {
            return new QuickMovePlan(-1, -1, false, -1, -1, false);
        }

        public boolean hasPrimary() {
            return primaryStart >= 0 && primaryEnd > primaryStart;
        }

        public boolean hasFallback() {
            return fallbackStart >= 0 && fallbackEnd > fallbackStart;
        }
    }
}
