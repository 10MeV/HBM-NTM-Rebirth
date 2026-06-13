package com.hbm.ntm.fluid;

import com.hbm.ntm.util.HbmMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;

public final class HbmFluidGuiHelper {
    private HbmFluidGuiHelper() {
    }

    public static boolean showHiddenFluidInfo() {
        return DistExecutor.unsafeRunForDist(
                () -> () -> com.hbm.ntm.client.ClientTooltipState.hasShiftDown(),
                () -> () -> false);
    }

    public static TankData watchTank(DataSlotSink sink, HbmFluidTank tank) {
        TankData data = new TankData(tank);
        data.addTo(sink);
        return data;
    }

    public static TankWatchReport watchTankReport(DataSlotSink sink, HbmFluidTank tank) {
        if (tank == null) {
            return new TankWatchReport(0, false, snapshot(0, null), null, 0);
        }
        TankData data = watchTank(sink, tank);
        return new TankWatchReport(0, true, snapshot(0, tank), data, TankData.SYNC_SLOT_COUNT);
    }

    public static List<TankData> watchTanks(DataSlotSink sink, Iterable<HbmFluidTank> tanks) {
        return watchTanksReport(sink, tanks).watchedTanks();
    }

    public static TankSyncWatchReport watchTanksReport(DataSlotSink sink, Iterable<HbmFluidTank> tanks) {
        List<TankData> watched = new ArrayList<>();
        List<TankWatchReport> reports = new ArrayList<>();
        int sourceCount = 0;
        int skippedNullTanks = 0;
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                int sourceIndex = sourceCount++;
                if (tank != null) {
                    TankData data = watchTank(sink, tank);
                    watched.add(data);
                    reports.add(new TankWatchReport(
                            sourceIndex, true, snapshot(sourceIndex, tank), data, TankData.SYNC_SLOT_COUNT));
                } else {
                    skippedNullTanks++;
                    reports.add(new TankWatchReport(
                            sourceIndex, false, snapshot(sourceIndex, null), null, 0));
                }
            }
        }
        return new TankSyncWatchReport(
                List.copyOf(watched),
                List.copyOf(reports),
                sourceCount,
                watched.size(),
                skippedNullTanks,
                watched.size() * TankData.SYNC_SLOT_COUNT,
                snapshotTankData(watched));
    }

    public static TankSnapshot snapshot(HbmFluidTank tank) {
        return snapshot(-1, tank);
    }

    public static TankSnapshot snapshot(int index, HbmFluidTank tank) {
        FluidType type = tank == null ? HbmFluids.NONE : tank.getTankType();
        int fill = tank == null ? 0 : tank.getFill();
        int capacity = tank == null ? 0 : tank.getMaxFill();
        int pressure = tank == null ? 0 : tank.getPressure();
        return new TankSnapshot(
                index,
                type.getName(),
                type.getId(),
                fill,
                capacity,
                Math.max(0, capacity - fill),
                pressure,
                type.getGuiTint());
    }

    public static TankSetSnapshot snapshotTanks(Iterable<HbmFluidTank> tanks) {
        List<TankSnapshot> snapshots = new ArrayList<>();
        if (tanks != null) {
            int index = 0;
            for (HbmFluidTank tank : tanks) {
                snapshots.add(snapshot(index++, tank));
            }
        }
        return snapshotSet(snapshots);
    }

    public static TankSetSnapshot snapshotTankData(Iterable<TankData> tanks) {
        List<TankSnapshot> snapshots = new ArrayList<>();
        if (tanks != null) {
            int index = 0;
            for (TankData tank : tanks) {
                snapshots.add(tank == null ? snapshot(index++, null) : tank.snapshot(index++));
            }
        }
        return snapshotSet(snapshots);
    }

    public static TankSetSnapshot snapshotTankData(TankData... tanks) {
        List<TankSnapshot> snapshots = new ArrayList<>();
        if (tanks != null) {
            for (int i = 0; i < tanks.length; i++) {
                TankData tank = tanks[i];
                snapshots.add(tank == null ? snapshot(i, null) : tank.snapshot(i));
            }
        }
        return snapshotSet(snapshots);
    }

    public static TankSetDiff diff(TankSetSnapshot previous, TankSetSnapshot current) {
        TankSetSnapshot before = previous == null ? snapshotTanks(List.of()) : previous;
        TankSetSnapshot after = current == null ? snapshotTanks(List.of()) : current;
        int max = Math.max(before.tankCount(), after.tankCount());
        List<TankDiff> diffs = new ArrayList<>();
        int changedTanks = 0;
        int addedTanks = 0;
        int removedTanks = 0;
        int fillDelta = after.totalFill() - before.totalFill();
        int capacityDelta = after.totalCapacity() - before.totalCapacity();
        for (int i = 0; i < max; i++) {
            TankSnapshot beforeTank = before.tank(i);
            TankSnapshot afterTank = after.tank(i);
            TankDiff tankDiff = TankDiff.between(i, beforeTank, afterTank);
            diffs.add(tankDiff);
            if (tankDiff.changed()) {
                changedTanks++;
            }
            if (tankDiff.added()) {
                addedTanks++;
            }
            if (tankDiff.removed()) {
                removedTanks++;
            }
        }
        return new TankSetDiff(before, after, List.copyOf(diffs), changedTanks, addedTanks, removedTanks,
                fillDelta, capacityDelta);
    }

    public static TankSetSyncReport compareSourceAndSynced(Iterable<TankData> tanks) {
        List<TankSnapshot> source = new ArrayList<>();
        List<TankSnapshot> synced = new ArrayList<>();
        if (tanks != null) {
            int index = 0;
            for (TankData tank : tanks) {
                source.add(tank == null ? snapshot(index, null) : tank.sourceSnapshot(index));
                synced.add(tank == null ? snapshot(index, null) : tank.snapshot(index));
                index++;
            }
        }
        TankSetSnapshot sourceSnapshot = snapshotSet(source);
        TankSetSnapshot syncedSnapshot = snapshotSet(synced);
        return new TankSetSyncReport(sourceSnapshot, syncedSnapshot, diff(sourceSnapshot, syncedSnapshot));
    }

    private static TankSetSnapshot snapshotSet(List<TankSnapshot> snapshots) {
        int totalFill = 0;
        int totalCapacity = 0;
        int nonEmpty = 0;
        int pressurized = 0;
        for (TankSnapshot snapshot : snapshots) {
            totalFill += snapshot.fill();
            totalCapacity += snapshot.capacity();
            if (!snapshot.isEmpty()) {
                nonEmpty++;
            }
            if (snapshot.pressure() != 0) {
                pressurized++;
            }
        }
        return new TankSetSnapshot(List.copyOf(snapshots), totalFill, totalCapacity, nonEmpty, pressurized);
    }

    public static int scaledFill(int fill, int capacity, int maxSize) {
        return capacity <= 0 || fill <= 0 || maxSize <= 0 ? 0 : fill * maxSize / capacity;
    }

    public static Component tankInfo(FluidType type, int fill, int capacity, int pressure) {
        FluidType displayType = type == null ? HbmFluids.NONE : type;
        MutableComponent info = displayType.getDisplayName().copy()
                .append(Component.literal(": " + fill + " / " + capacity + " mB"));
        if (pressure != 0) {
            info.append(Component.literal(" | Pressure: " + pressure + " PU").withStyle(ChatFormatting.RED));
        }
        return info;
    }

    public static Component tankInfo(HbmFluidTank tank, int fill, int capacity) {
        return tankInfo(tank.getTankType(), fill, capacity, tank.getPressure());
    }

    public static final class TankData {
        private static final int SYNC_SLOT_COUNT = 4;
        private final HbmFluidTank tank;
        private int fill;
        private int capacity;
        private int typeId;
        private int pressure;

        private TankData(HbmFluidTank tank) {
            this.tank = tank;
        }

        public int fill() {
            return fill;
        }

        public int capacity() {
            return capacity;
        }

        public FluidType type() {
            return HbmFluids.fromId(typeId);
        }

        public int pressure() {
            return pressure;
        }

        public int guiTint() {
            return type().getGuiTint();
        }

        public boolean isEmpty() {
            return type() == HbmFluids.NONE || fill <= 0;
        }

        public int scaledFill(int maxSize) {
            return HbmFluidGuiHelper.scaledFill(fill, capacity, maxSize);
        }

        public Component info() {
            return tankInfo(type(), fill, capacity, pressure);
        }

        public TankSnapshot snapshot() {
            return snapshot(-1);
        }

        public TankSnapshot snapshot(int index) {
            FluidType type = type();
            return new TankSnapshot(index, type.getName(), type.getId(), fill, capacity,
                    Math.max(0, capacity - fill), pressure, type.getGuiTint());
        }

        public TankSnapshot sourceSnapshot() {
            return sourceSnapshot(-1);
        }

        public TankSnapshot sourceSnapshot(int index) {
            return HbmFluidGuiHelper.snapshot(index, tank);
        }

        public List<Component> tooltip() {
            return tooltip(false);
        }

        public List<Component> tooltip(boolean showHidden) {
            List<Component> tooltip = new ArrayList<>();
            FluidType type = type();
            tooltip.add(type.getDisplayName());
            tooltip.add(Component.literal(fill + " / " + capacity + " mB"));
            if (pressure != 0) {
                tooltip.add(Component.literal("Pressure: " + pressure + " PU").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Pressurized, use compressor!")
                        .withStyle(HbmMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_RED));
            }
            type.appendInfo(tooltip, showHidden);
            return tooltip;
        }

        private void addTo(DataSlotSink sink) {
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getFill();
                }

                @Override
                public void set(int value) {
                    fill = value;
                }
            });
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getMaxFill();
                }

                @Override
                public void set(int value) {
                    capacity = value;
                }
            });
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getTankType().getId();
                }

                @Override
                public void set(int value) {
                    typeId = value;
                }
            });
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getPressure();
                }

                @Override
                public void set(int value) {
                    pressure = HbmFluidTank.clampPressure(value);
                }
            });
        }
    }

    public record TankSnapshot(
            int index,
            String fluid,
            int fluidId,
            int fill,
            int capacity,
            int space,
            int pressure,
            int guiTint) {
        public TankSnapshot {
            fluid = fluid == null ? HbmFluids.NONE.getName() : fluid;
            fill = Math.max(0, fill);
            capacity = Math.max(0, capacity);
            space = Math.max(0, space);
            pressure = HbmFluidTank.clampPressure(pressure);
        }

        public FluidType type() {
            return HbmFluids.fromId(fluidId);
        }

        public boolean isEmpty() {
            return type() == HbmFluids.NONE || fill <= 0;
        }

        public int scaledFill(int maxSize) {
            return HbmFluidGuiHelper.scaledFill(fill, capacity, maxSize);
        }

        public Component info() {
            return tankInfo(type(), fill, capacity, pressure);
        }

        public List<Component> tooltip(boolean showHidden) {
            List<Component> tooltip = new ArrayList<>();
            FluidType type = type();
            tooltip.add(type.getDisplayName());
            tooltip.add(Component.literal(fill + " / " + capacity + " mB"));
            if (pressure != 0) {
                tooltip.add(Component.literal("Pressure: " + pressure + " PU").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Pressurized, use compressor!")
                        .withStyle(HbmMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_RED));
            }
            type.appendInfo(tooltip, showHidden);
            return tooltip;
        }

        public List<Component> tooltip() {
            return tooltip(false);
        }
    }

    public record TankSetSnapshot(
            List<TankSnapshot> tanks,
            int totalFill,
            int totalCapacity,
            int nonEmptyTanks,
            int pressurizedTanks) {
        public TankSetSnapshot {
            tanks = tanks == null ? List.of() : List.copyOf(tanks);
            totalFill = Math.max(0, totalFill);
            totalCapacity = Math.max(0, totalCapacity);
            nonEmptyTanks = Math.max(0, nonEmptyTanks);
            pressurizedTanks = Math.max(0, pressurizedTanks);
        }

        public boolean isEmpty() {
            return nonEmptyTanks <= 0 || totalFill <= 0;
        }

        public int tankCount() {
            return tanks.size();
        }

        public TankSnapshot tank(int index) {
            return index >= 0 && index < tanks.size() ? tanks.get(index) : null;
        }

        public int scaledTotalFill(int maxSize) {
            return HbmFluidGuiHelper.scaledFill(totalFill, totalCapacity, maxSize);
        }
    }

    public record TankWatchReport(
            int sourceIndex,
            boolean tankPresent,
            TankSnapshot sourceSnapshot,
            TankData syncedData,
            int dataSlots) {
        public TankWatchReport {
            sourceIndex = Math.max(0, sourceIndex);
            sourceSnapshot = sourceSnapshot == null ? snapshot(sourceIndex, null) : sourceSnapshot;
            dataSlots = Math.max(0, dataSlots);
        }
    }

    public record TankSyncWatchReport(
            List<TankData> watchedTanks,
            List<TankWatchReport> tankReports,
            int sourceTanks,
            int watchedCount,
            int skippedNullTanks,
            int dataSlots,
            TankSetSnapshot initialSyncedSnapshot) {
        public TankSyncWatchReport {
            watchedTanks = watchedTanks == null ? List.of() : List.copyOf(watchedTanks);
            tankReports = tankReports == null ? List.of() : List.copyOf(tankReports);
            sourceTanks = Math.max(0, sourceTanks);
            watchedCount = Math.max(0, watchedCount);
            skippedNullTanks = Math.max(0, skippedNullTanks);
            dataSlots = Math.max(0, dataSlots);
            initialSyncedSnapshot = initialSyncedSnapshot == null ? snapshotTanks(List.of()) : initialSyncedSnapshot;
        }
    }

    public record TankSetSyncReport(
            TankSetSnapshot source,
            TankSetSnapshot synced,
            TankSetDiff diff) {
        public TankSetSyncReport {
            source = source == null ? snapshotTanks(List.of()) : source;
            synced = synced == null ? snapshotTanks(List.of()) : synced;
            diff = diff == null ? HbmFluidGuiHelper.diff(source, synced) : diff;
        }

        public boolean inSync() {
            return !diff.changed();
        }
    }

    public record TankSetDiff(
            TankSetSnapshot previous,
            TankSetSnapshot current,
            List<TankDiff> tanks,
            int changedTanks,
            int addedTanks,
            int removedTanks,
            int fillDelta,
            int capacityDelta) {
        public TankSetDiff {
            previous = previous == null ? snapshotTanks(List.of()) : previous;
            current = current == null ? snapshotTanks(List.of()) : current;
            tanks = tanks == null ? List.of() : List.copyOf(tanks);
            changedTanks = Math.max(0, changedTanks);
            addedTanks = Math.max(0, addedTanks);
            removedTanks = Math.max(0, removedTanks);
        }

        public boolean changed() {
            return changedTanks > 0 || addedTanks > 0 || removedTanks > 0
                    || fillDelta != 0 || capacityDelta != 0;
        }
    }

    public record TankDiff(
            int index,
            TankSnapshot previous,
            TankSnapshot current,
            boolean added,
            boolean removed,
            boolean typeChanged,
            boolean fillChanged,
            boolean capacityChanged,
            boolean pressureChanged,
            int fillDelta,
            int capacityDelta) {
        private static TankDiff between(int index, TankSnapshot previous, TankSnapshot current) {
            boolean added = previous == null && current != null;
            boolean removed = previous != null && current == null;
            FluidType previousType = previous == null ? HbmFluids.NONE : previous.type();
            FluidType currentType = current == null ? HbmFluids.NONE : current.type();
            int previousFill = previous == null ? 0 : previous.fill();
            int currentFill = current == null ? 0 : current.fill();
            int previousCapacity = previous == null ? 0 : previous.capacity();
            int currentCapacity = current == null ? 0 : current.capacity();
            int previousPressure = previous == null ? 0 : previous.pressure();
            int currentPressure = current == null ? 0 : current.pressure();
            return new TankDiff(
                    index,
                    previous,
                    current,
                    added,
                    removed,
                    previousType != currentType,
                    previousFill != currentFill,
                    previousCapacity != currentCapacity,
                    previousPressure != currentPressure,
                    currentFill - previousFill,
                    currentCapacity - previousCapacity);
        }

        public TankDiff {
            index = Math.max(0, index);
        }

        public boolean changed() {
            return added || removed || typeChanged || fillChanged || capacityChanged || pressureChanged;
        }
    }

    @FunctionalInterface
    public interface DataSlotSink {
        void add(DataSlot slot);
    }
}
