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

    public static List<TankData> watchTanks(DataSlotSink sink, Iterable<HbmFluidTank> tanks) {
        List<TankData> watched = new ArrayList<>();
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                if (tank != null) {
                    watched.add(watchTank(sink, tank));
                }
            }
        }
        return List.copyOf(watched);
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

    @FunctionalInterface
    public interface DataSlotSink {
        void add(DataSlot slot);
    }
}
