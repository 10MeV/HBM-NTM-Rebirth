package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKInventoryPlanner {
    public static final double ACTIVE_USE_DISTANCE_SQ = 128.0D;
    public static final int DEFAULT_STACK_LIMIT = 64;
    public static final String ITEMS_TAG = "items";
    public static final String SLOT_TAG = "slot";
    public static final String CUSTOM_NAME_TAG = "name";

    private RBMKInventoryPlanner() {
    }

    public static ActiveUsePlan planActiveUse(boolean blockEntityStillAtPosition, double playerDistanceSq) {
        return new ActiveUsePlan(
                blockEntityStillAtPosition && playerDistanceSq <= ACTIVE_USE_DISTANCE_SQ,
                blockEntityStillAtPosition,
                playerDistanceSq,
                ACTIVE_USE_DISTANCE_SQ);
    }

    public static SlottedInventoryContract slottedContract(int slotCount) {
        int slots = Math.max(0, slotCount);
        return new SlottedInventoryContract(
                slots,
                DEFAULT_STACK_LIMIT,
                ITEMS_TAG,
                SLOT_TAG,
                CUSTOM_NAME_TAG,
                true,
                false,
                List.of(),
                true,
                true);
    }

    public static SlotSetPlan planSetSlot(int slot, int slotCount, int incomingStackSize) {
        if (!validSlot(slot, slotCount)) {
            return SlotSetPlan.reject(slot, SlotFailure.SLOT_OUT_OF_RANGE);
        }
        int limited = Math.min(Math.max(0, incomingStackSize), DEFAULT_STACK_LIMIT);
        return new SlotSetPlan(true, null, slot, Math.max(0, incomingStackSize), limited);
    }

    public static SlotClosingPlan planStackInSlotOnClosing(int slot, int slotCount, boolean slotPresent, int stackSize) {
        if (!validSlot(slot, slotCount)) {
            return SlotClosingPlan.reject(slot, SlotFailure.SLOT_OUT_OF_RANGE);
        }
        if (!slotPresent) {
            return new SlotClosingPlan(true, slot, false, 0, false);
        }
        return new SlotClosingPlan(true, slot, true, Math.max(0, stackSize), true);
    }

    public static SlotDecreasePlan planDecreaseStack(int slot, int slotCount, boolean slotPresent, int stackSize,
            int amount) {
        if (!validSlot(slot, slotCount)) {
            return SlotDecreasePlan.reject(slot, SlotFailure.SLOT_OUT_OF_RANGE);
        }
        if (amount <= 0) {
            return SlotDecreasePlan.reject(slot, SlotFailure.NON_POSITIVE_AMOUNT);
        }
        if (!slotPresent) {
            return new SlotDecreasePlan(true, null, slot, false, 0, 0, 0, false);
        }

        int safeStackSize = Math.max(0, stackSize);
        if (safeStackSize <= amount) {
            return new SlotDecreasePlan(true, null, slot, true, safeStackSize, safeStackSize, 0, true);
        }

        int remaining = safeStackSize - amount;
        return new SlotDecreasePlan(true, null, slot, true, safeStackSize, amount, remaining, remaining == 0);
    }

    public static SidedAccessPlan defaultSidedAccess(int slot, int slotCount, boolean itemValidForSlot) {
        boolean valid = validSlot(slot, slotCount);
        return new SidedAccessPlan(
                valid,
                valid && itemValidForSlot,
                false,
                List.of());
    }

    public static List<StoredSlot> normalizeStoredSlots(int slotCount, List<StoredSlot> storedSlots) {
        int slots = Math.max(0, slotCount);
        if (storedSlots == null || storedSlots.isEmpty()) {
            return List.of();
        }
        List<StoredSlot> normalized = new ArrayList<>();
        for (StoredSlot storedSlot : storedSlots) {
            if (storedSlot != null && validSlot(storedSlot.slot(), slots)) {
                normalized.add(new StoredSlot(storedSlot.slot(), Math.max(0, storedSlot.stackSize())));
            }
        }
        return List.copyOf(normalized);
    }

    public static NbtReadPlan planReadNbt(int slotCount, boolean diagnosticMode, List<StoredSlot> serializedSlots,
            String serializedCustomName) {
        if (diagnosticMode) {
            return new NbtReadPlan(true, true, List.of(), "", false, false);
        }
        String customName = serializedCustomName == null ? "" : serializedCustomName;
        return new NbtReadPlan(
                true,
                false,
                normalizeStoredSlots(slotCount, serializedSlots),
                customName,
                true,
                !customName.isEmpty());
    }

    public static NbtWritePlan planWriteNbt(boolean diagnosticMode, List<StoredSlot> occupiedSlots,
            String customName) {
        if (diagnosticMode) {
            return new NbtWritePlan(true, true, List.of(), "", false, false);
        }
        String safeName = customName == null ? "" : customName;
        return new NbtWritePlan(
                true,
                false,
                normalizeStoredSlots(Integer.MAX_VALUE, occupiedSlots),
                safeName,
                true,
                !safeName.isEmpty());
    }

    public static CustomNamePlan planCustomName(String customName) {
        String safeName = customName == null ? "" : customName;
        return new CustomNamePlan(safeName, !safeName.isEmpty(), true);
    }

    public static LoadableContract loadableContract() {
        return new LoadableContract(
                "canLoad",
                "load",
                "canUnload",
                "provideNext",
                "unload",
                true,
                true);
    }

    private static boolean validSlot(int slot, int slotCount) {
        return slot >= 0 && slot < Math.max(0, slotCount);
    }

    public enum SlotFailure {
        SLOT_OUT_OF_RANGE,
        NON_POSITIVE_AMOUNT
    }

    public record ActiveUsePlan(
            boolean usable,
            boolean blockEntityStillAtPosition,
            double playerDistanceSq,
            double maxDistanceSq) {
    }

    public record SlottedInventoryContract(
            int slotCount,
            int stackLimit,
            String itemsTag,
            String slotTag,
            String customNameTag,
            boolean canInsertDelegatesToIsItemValidForSlot,
            boolean defaultCanExtract,
            List<Integer> defaultAccessibleSlots,
            boolean writesSlotsWhenNotDiagnostic,
            boolean writesCustomNameWhenPresent) {
    }

    public record SlotSetPlan(
            boolean accepted,
            SlotFailure failure,
            int slot,
            int requestedStackSize,
            int storedStackSize) {
        private static SlotSetPlan reject(int slot, SlotFailure failure) {
            return new SlotSetPlan(false, failure, slot, 0, 0);
        }
    }

    public record SlotClosingPlan(
            boolean accepted,
            int slot,
            boolean returnedStack,
            int returnedStackSize,
            boolean clearsSlot) {
        private static SlotClosingPlan reject(int slot, SlotFailure failure) {
            return new SlotClosingPlan(false, slot, false, 0, false);
        }
    }

    public record SlotDecreasePlan(
            boolean accepted,
            SlotFailure failure,
            int slot,
            boolean returnedStack,
            int originalStackSize,
            int returnedStackSize,
            int remainingStackSize,
            boolean clearsSlot) {
        private static SlotDecreasePlan reject(int slot, SlotFailure failure) {
            return new SlotDecreasePlan(false, failure, slot, false, 0, 0, 0, false);
        }
    }

    public record SidedAccessPlan(
            boolean slotInRange,
            boolean canInsert,
            boolean canExtract,
            List<Integer> accessibleSlots) {
    }

    public record StoredSlot(int slot, int stackSize) {
    }

    public record NbtReadPlan(
            boolean accepted,
            boolean skippedByDiagnosticMode,
            List<StoredSlot> restoredSlots,
            String customName,
            boolean readItemsTag,
            boolean hasCustomName) {
    }

    public record NbtWritePlan(
            boolean accepted,
            boolean skippedByDiagnosticMode,
            List<StoredSlot> writtenSlots,
            String customName,
            boolean writesItemsTag,
            boolean writesCustomName) {
    }

    public record CustomNamePlan(String customName, boolean hasCustomName, boolean markDirty) {
    }

    public record LoadableContract(
            String canLoadMethod,
            String loadMethod,
            String canUnloadMethod,
            String provideNextMethod,
            String unloadMethod,
            boolean loadRequiresCanLoadCheck,
            boolean provideNextRequiresCanUnloadCheck) {
    }
}
