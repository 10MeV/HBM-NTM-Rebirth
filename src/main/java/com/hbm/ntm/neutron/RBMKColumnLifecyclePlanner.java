package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class RBMKColumnLifecyclePlanner {
    public static final int LEGACY_METADATA_OFFSET = 10;
    public static final int STORAGE_SLOTS = 12;

    private RBMKColumnLifecyclePlanner() {
    }

    public static LidType lidFromLegacyMeta(int meta) {
        int lidOrdinal = meta - LEGACY_METADATA_OFFSET;
        if (lidOrdinal == Direction.EAST.ordinal()) {
            return LidType.STANDARD;
        }
        if (lidOrdinal == Direction.SOUTH.ordinal()) {
            return LidType.GLASS;
        }
        return LidType.NONE;
    }

    public static int legacyMetaForLid(LidType lidType) {
        Direction direction = switch (lidType == null ? LidType.NONE : lidType) {
            case STANDARD -> Direction.EAST;
            case GLASS -> Direction.SOUTH;
            case NONE -> Direction.NORTH;
        };
        return LEGACY_METADATA_OFFSET + direction.ordinal();
    }

    public static boolean hasLid(boolean lidRemovable, LidType lidType) {
        return !lidRemovable || (lidType != null && lidType != LidType.NONE);
    }

    public static ActivationPlan planOpenActivation(boolean holdingLid, boolean hasLid, boolean sneaking) {
        if (holdingLid && !hasLid) {
            return new ActivationPlan(false, false, ActivationFailure.HELD_LID_AND_COLUMN_HAS_NO_LID);
        }
        return new ActivationPlan(true, !sneaking, null);
    }

    public static LidRemovalPlan planScrewdriverLidRemoval(BlockPos origin, int columnHeight, LidType lidType,
            boolean lidRemovable) {
        if (!hasLid(lidRemovable, lidType) || !lidRemovable) {
            return LidRemovalPlan.reject();
        }
        return new LidRemovalPlan(
                true,
                legacyMetaForLid(LidType.NONE),
                true,
                true,
                lidDrop(origin, columnHeight, lidType));
    }

    public static LidDropPlan planBreakLidDrop(BlockPos origin, int columnHeight, LidType lidType, boolean dropLids) {
        if (!dropLids || lidType == null || lidType == LidType.NONE) {
            return LidDropPlan.none();
        }
        return lidDrop(origin, columnHeight, lidType);
    }

    public static ColumnMeltPlan planColumnMelt(
            ColumnKind kind,
            BlockPos origin,
            int columnHeight,
            int reduce,
            boolean randomExtraReduce,
            LidType lidType,
            boolean moderated) {
        ColumnKind safeKind = kind == null ? ColumnKind.OTHER : kind;
        List<DebrisRange> debris = new ArrayList<>();

        switch (safeKind) {
            case BLANK, ABSORBER, REFLECTOR, BOILER, HEATEX -> debris.add(new DebrisRange(DebrisType.BLANK, 1, 2));
            case OUTGASSER -> debris.add(new DebrisRange(DebrisType.BLANK, 4, 5));
            case MODERATOR -> debris.add(new DebrisRange(DebrisType.GRAPHITE, 2, 3));
            case CONTROL, CONTROL_AUTO -> {
                if (moderated) {
                    debris.add(new DebrisRange(DebrisType.GRAPHITE, 2, 3));
                }
                debris.add(new DebrisRange(DebrisType.ROD, 2, 3));
            }
            default -> {
            }
        }

        boolean controlRod = safeKind == ColumnKind.CONTROL || safeKind == ColumnKind.CONTROL_AUTO;
        if (!controlRod && lidType == LidType.STANDARD) {
            debris.add(new DebrisRange(DebrisType.LID, 1, 1));
        }

        return new ColumnMeltPlan(
                safeKind,
                RBMKMeltPlanner.standardMelt(origin, columnHeight, reduce, randomExtraReduce),
                debris,
                !controlRod);
    }

    public static ReaSimBoilerPlan planReaSimBoil(RBMKThermalState thermalState, RBMKRuntimeSettings settings) {
        if (thermalState == null || settings == null || !settings.reasimBoilers()) {
            return new ReaSimBoilerPlan(false, 0);
        }
        int processedWater = RBMKThermalRuntime.boilWater(thermalState, settings);
        return new ReaSimBoilerPlan(processedWater > 0, processedWater);
    }

    public static FluidPortPlan planLoaderFluidPorts(BlockPos origin, int columnHeight, LoaderConnection loaderConnection,
            boolean outgasserFallbackBottomPort) {
        LoaderConnection connection = loaderConnection == null ? LoaderConnection.NONE : loaderConnection;
        List<FluidPort> ports = new ArrayList<>();
        ports.add(new FluidPort(origin.above(columnHeight + 1), Direction.UP));

        int loaderDepth = switch (connection) {
            case BELOW_ONE -> 1;
            case BELOW_TWO -> 2;
            case NONE -> 0;
        };

        if (loaderDepth > 0) {
            BlockPos loader = origin.below(loaderDepth);
            ports.add(new FluidPort(loader.east(), Direction.EAST));
            ports.add(new FluidPort(loader.west(), Direction.WEST));
            ports.add(new FluidPort(loader.south(), Direction.SOUTH));
            ports.add(new FluidPort(loader.north(), Direction.NORTH));
            ports.add(new FluidPort(loader.below(), Direction.DOWN));
        } else if (outgasserFallbackBottomPort) {
            ports.add(new FluidPort(origin.below(), Direction.DOWN));
        }

        return new FluidPortPlan(connection, ports);
    }

    public static StorageLoadPlan planStorageLoad(List<Boolean> occupiedSlots) {
        List<Boolean> slots = normalizeStorageSlots(occupiedSlots);
        if (slots.get(STORAGE_SLOTS - 1)) {
            return new StorageLoadPlan(false, STORAGE_SLOTS - 1);
        }
        return new StorageLoadPlan(true, STORAGE_SLOTS - 1);
    }

    public static StorageUnloadPlan planStorageUnload(List<Boolean> occupiedSlots) {
        List<Boolean> slots = normalizeStorageSlots(occupiedSlots);
        if (!slots.get(0)) {
            return new StorageUnloadPlan(false, 0);
        }
        return new StorageUnloadPlan(true, 0);
    }

    public static StorageCompactionPlan planStorageCompaction(List<Boolean> occupiedSlots) {
        List<Boolean> before = normalizeStorageSlots(occupiedSlots);
        List<Boolean> after = new ArrayList<>(before);
        List<SlotMove> moves = new ArrayList<>();

        for (int i = 0; i < STORAGE_SLOTS - 1; i++) {
            if (!after.get(i) && after.get(i + 1)) {
                after.set(i, true);
                after.set(i + 1, false);
                moves.add(new SlotMove(i + 1, i));
            }
        }

        return new StorageCompactionPlan(before, after, moves);
    }

    private static LidDropPlan lidDrop(BlockPos origin, int columnHeight, LidType lidType) {
        String itemId = switch (lidType == null ? LidType.NONE : lidType) {
            case STANDARD -> "rbmk_lid";
            case GLASS -> "rbmk_lid_glass";
            case NONE -> "";
        };
        if (itemId.isEmpty()) {
            return LidDropPlan.none();
        }
        return new LidDropPlan(true, itemId, origin.above(columnHeight));
    }

    private static List<Boolean> normalizeStorageSlots(List<Boolean> occupiedSlots) {
        List<Boolean> slots = new ArrayList<>(STORAGE_SLOTS);
        for (int i = 0; i < STORAGE_SLOTS; i++) {
            slots.add(occupiedSlots != null && i < occupiedSlots.size() && Boolean.TRUE.equals(occupiedSlots.get(i)));
        }
        return slots;
    }

    public enum LidType {
        NONE,
        STANDARD,
        GLASS
    }

    public enum ColumnKind {
        BLANK,
        FUEL,
        FUEL_SIM,
        CONTROL,
        CONTROL_AUTO,
        BOILER,
        MODERATOR,
        ABSORBER,
        REFLECTOR,
        OUTGASSER,
        BREEDER,
        STORAGE,
        COOLER,
        HEATEX,
        OTHER
    }

    public enum DebrisType {
        BLANK,
        GRAPHITE,
        ROD,
        LID
    }

    public enum LoaderConnection {
        NONE,
        BELOW_ONE,
        BELOW_TWO
    }

    public enum ActivationFailure {
        HELD_LID_AND_COLUMN_HAS_NO_LID
    }

    public record ActivationPlan(boolean handled, boolean openGui, ActivationFailure failure) {
    }

    public record LidDropPlan(boolean drop, String legacyItemId, BlockPos pos) {
        private static LidDropPlan none() {
            return new LidDropPlan(false, "", BlockPos.ZERO);
        }
    }

    public record LidRemovalPlan(
            boolean removed,
            int newLegacyMeta,
            boolean removeNeutronNodeLid,
            boolean suppressExplodeOnBrokenDuringMutation,
            LidDropPlan drop) {
        private static LidRemovalPlan reject() {
            return new LidRemovalPlan(false, 0, false, false, LidDropPlan.none());
        }
    }

    public record DebrisRange(DebrisType type, int minCount, int maxCount) {
    }

    public record ColumnMeltPlan(
            ColumnKind kind,
            RBMKMeltPlanner.StandardMeltPlan standardMelt,
            List<DebrisRange> debris,
            boolean usedBaseMeltHook) {
    }

    public record ReaSimBoilerPlan(boolean processed, int waterToSteam) {
    }

    public record FluidPort(BlockPos pos, Direction direction) {
    }

    public record FluidPortPlan(LoaderConnection loaderConnection, List<FluidPort> ports) {
    }

    public record StorageLoadPlan(boolean accepted, int targetSlot) {
    }

    public record StorageUnloadPlan(boolean accepted, int sourceSlot) {
    }

    public record SlotMove(int fromSlot, int toSlot) {
    }

    public record StorageCompactionPlan(List<Boolean> before, List<Boolean> after, List<SlotMove> moves) {
    }
}
