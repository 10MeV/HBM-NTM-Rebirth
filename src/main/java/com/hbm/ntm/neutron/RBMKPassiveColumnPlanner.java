package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKPassiveColumnPlanner {
    public static final int STORAGE_SLOT_COUNT = 12;
    public static final int STORAGE_COMPACTION_INTERVAL = 10;
    public static final int STORAGE_LOAD_SLOT = 11;
    public static final int STORAGE_UNLOAD_SLOT = 0;
    public static final String STORAGE_CONTAINER_NAME = "container.rbmkStorage";

    private RBMKPassiveColumnPlanner() {
    }

    public static PassiveColumnContract columnContract(PassiveColumnType type) {
        PassiveColumnType safeType = type == null ? PassiveColumnType.BLANK : type;
        return switch (safeType) {
            case BLANK -> new PassiveColumnContract(
                    safeType,
                    RBMKNeutronHandler.RBMKType.OTHER,
                    RBMKConsolePlanner.ColumnType.BLANK,
                    List.of(debris(RBMKColumnLifecyclePlanner.DebrisType.BLANK, 1, 2)),
                    false);
            case MODERATOR -> new PassiveColumnContract(
                    safeType,
                    RBMKNeutronHandler.RBMKType.MODERATOR,
                    RBMKConsolePlanner.ColumnType.MODERATOR,
                    List.of(debris(RBMKColumnLifecyclePlanner.DebrisType.GRAPHITE, 2, 3)),
                    false);
            case REFLECTOR -> new PassiveColumnContract(
                    safeType,
                    RBMKNeutronHandler.RBMKType.REFLECTOR,
                    RBMKConsolePlanner.ColumnType.REFLECTOR,
                    List.of(debris(RBMKColumnLifecyclePlanner.DebrisType.BLANK, 1, 2)),
                    false);
            case ABSORBER -> new PassiveColumnContract(
                    safeType,
                    RBMKNeutronHandler.RBMKType.ABSORBER,
                    RBMKConsolePlanner.ColumnType.ABSORBER,
                    List.of(debris(RBMKColumnLifecyclePlanner.DebrisType.BLANK, 1, 2)),
                    false);
            case STORAGE -> new PassiveColumnContract(
                    safeType,
                    RBMKNeutronHandler.RBMKType.OTHER,
                    RBMKConsolePlanner.ColumnType.STORAGE,
                    List.of(),
                    false);
        };
    }

    public static StorageContract storageContract() {
        return new StorageContract(
                STORAGE_CONTAINER_NAME,
                STORAGE_SLOT_COUNT,
                STORAGE_LOAD_SLOT,
                STORAGE_UNLOAD_SLOT,
                accessibleStorageSlots(),
                true,
                true,
                false,
                STORAGE_COMPACTION_INTERVAL);
    }

    public static StorageInventoryRule storageInventoryRule(boolean itemIsRbmkRod) {
        return new StorageInventoryRule(
                itemIsRbmkRod,
                true,
                accessibleStorageSlots(),
                "ItemRBMKRod");
    }

    public static StorageTickPlan planStorageTick(long gameTime, List<Boolean> occupiedSlots) {
        boolean compact = gameTime % STORAGE_COMPACTION_INTERVAL == 0;
        RBMKColumnLifecyclePlanner.StorageCompactionPlan compaction = compact
                ? RBMKColumnLifecyclePlanner.planStorageCompaction(occupiedSlots)
                : emptyCompaction(occupiedSlots);
        return new StorageTickPlan(compact, compaction);
    }

    public static StorageLoadState planStorageLoad(List<Boolean> occupiedSlots) {
        List<Boolean> slots = normalizeSlots(occupiedSlots);
        boolean canLoad = !slots.get(STORAGE_LOAD_SLOT);
        return new StorageLoadState(canLoad, STORAGE_LOAD_SLOT);
    }

    public static StorageLoadTransferPlan planStorageLoadTransfer(List<Boolean> occupiedSlots, boolean stackPresent) {
        List<Boolean> slots = normalizeSlots(occupiedSlots);
        boolean accepted = stackPresent && !slots.get(STORAGE_LOAD_SLOT);
        List<Boolean> after = new ArrayList<>(slots);
        if (accepted) {
            after.set(STORAGE_LOAD_SLOT, true);
        }
        return new StorageLoadTransferPlan(
                accepted,
                STORAGE_LOAD_SLOT,
                true,
                false,
                slots,
                List.copyOf(after));
    }

    public static StorageUnloadState planStorageUnload(List<Boolean> occupiedSlots) {
        List<Boolean> slots = normalizeSlots(occupiedSlots);
        boolean canUnload = slots.get(STORAGE_UNLOAD_SLOT);
        return new StorageUnloadState(canUnload, STORAGE_UNLOAD_SLOT);
    }

    public static StorageUnloadTransferPlan planStorageUnloadTransfer(List<Boolean> occupiedSlots) {
        List<Boolean> slots = normalizeSlots(occupiedSlots);
        boolean accepted = slots.get(STORAGE_UNLOAD_SLOT);
        List<Boolean> after = new ArrayList<>(slots);
        if (accepted) {
            after.set(STORAGE_UNLOAD_SLOT, false);
        }
        return new StorageUnloadTransferPlan(
                accepted,
                STORAGE_UNLOAD_SLOT,
                true,
                slots,
                List.copyOf(after));
    }

    private static RBMKColumnLifecyclePlanner.DebrisRange debris(
            RBMKColumnLifecyclePlanner.DebrisType type,
            int min,
            int max) {
        return new RBMKColumnLifecyclePlanner.DebrisRange(type, min, max);
    }

    private static List<Integer> accessibleStorageSlots() {
        List<Integer> slots = new ArrayList<>(STORAGE_SLOT_COUNT);
        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            slots.add(i);
        }
        return List.copyOf(slots);
    }

    private static RBMKColumnLifecyclePlanner.StorageCompactionPlan emptyCompaction(List<Boolean> occupiedSlots) {
        List<Boolean> slots = normalizeSlots(occupiedSlots);
        return new RBMKColumnLifecyclePlanner.StorageCompactionPlan(slots, slots, List.of());
    }

    private static List<Boolean> normalizeSlots(List<Boolean> occupiedSlots) {
        List<Boolean> slots = new ArrayList<>(STORAGE_SLOT_COUNT);
        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            slots.add(occupiedSlots != null && i < occupiedSlots.size() && Boolean.TRUE.equals(occupiedSlots.get(i)));
        }
        return List.copyOf(slots);
    }

    public enum PassiveColumnType {
        BLANK,
        MODERATOR,
        REFLECTOR,
        ABSORBER,
        STORAGE
    }

    public record PassiveColumnContract(
            PassiveColumnType type,
            RBMKNeutronHandler.RBMKType neutronType,
            RBMKConsolePlanner.ColumnType consoleType,
            List<RBMKColumnLifecyclePlanner.DebrisRange> meltDebris,
            boolean moderated) {
    }

    public record StorageContract(
            String containerName,
            int slotCount,
            int loadSlot,
            int unloadSlot,
            List<Integer> accessibleSlots,
            boolean acceptsFuelRodItems,
            boolean canExtractAnyStoredRod,
            boolean loadPathChecksItemType,
            int compactionInterval) {
    }

    public record StorageInventoryRule(
            boolean validForSlot,
            boolean canExtract,
            List<Integer> accessibleSlots,
            String acceptedLegacyItemClass) {
    }

    public record StorageTickPlan(
            boolean compactedThisTick,
            RBMKColumnLifecyclePlanner.StorageCompactionPlan compaction) {
    }

    public record StorageLoadState(boolean canLoad, int targetSlot) {
    }

    public record StorageUnloadState(boolean canUnload, int sourceSlot) {
    }

    public record StorageLoadTransferPlan(
            boolean accepted,
            int targetSlot,
            boolean copyInputStack,
            boolean checksItemType,
            List<Boolean> before,
            List<Boolean> after) {
    }

    public record StorageUnloadTransferPlan(
            boolean accepted,
            int sourceSlot,
            boolean clearSourceSlot,
            List<Boolean> before,
            List<Boolean> after) {
    }
}
