package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class RBMKConsolePlanner {
    public static final int CONSOLE_GRID_SIZE = 15;
    public static final int DISPLAY_GRID_SIZE = 7;
    public static final int CONSOLE_SCREEN_COUNT = 6;
    public static final int FLUX_BUFFER_SIZE = 60;
    public static final int RESCAN_INTERVAL_TICKS = 10;

    private RBMKConsolePlanner() {
    }

    public static RelativeColumn relativeColumn(int index, int gridSize, int rotation) {
        int half = gridSize / 2;
        int i = Math.floorMod(index, gridSize) - half;
        int j = index / gridSize - half;
        return switch (Math.floorMod(rotation, 4)) {
            case 1 -> new RelativeColumn(-j, i);
            case 2 -> new RelativeColumn(-i, -j);
            case 3 -> new RelativeColumn(j, -i);
            default -> new RelativeColumn(i, j);
        };
    }

    public static List<BlockPos> scanPositions(BlockPos target, int gridSize, int rotation) {
        BlockPos safeTarget = target == null ? BlockPos.ZERO : target;
        int count = gridSize * gridSize;
        List<BlockPos> positions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            RelativeColumn relative = relativeColumn(index, gridSize, rotation);
            positions.add(safeTarget.offset(relative.x(), 0, relative.z()));
        }
        return List.copyOf(positions);
    }

    public static int rotate(int currentRotation) {
        return Math.floorMod(currentRotation + 1, 4);
    }

    public static int[] shiftFluxBuffer(int[] currentBuffer, double flux) {
        int[] next = new int[FLUX_BUFFER_SIZE];
        if (currentBuffer != null) {
            int copy = Math.min(currentBuffer.length - 1, next.length - 1);
            for (int i = 0; i < copy; i++) {
                next[i] = currentBuffer[i + 1];
            }
        }
        next[next.length - 1] = (int) flux;
        return next;
    }

    public static ColumnSnapshot columnSnapshot(ColumnType type, CompoundTag legacyData, double heat, double maxHeat,
            int craneIndicator, boolean moderated) {
        CompoundTag data = legacyData == null ? new CompoundTag() : legacyData.copy();
        data.putDouble("heat", heat);
        data.putDouble("maxHeat", maxHeat);
        data.putByte("indicator", (byte) craneIndicator);
        if (moderated) {
            data.putBoolean("moderated", true);
        }
        return new ColumnSnapshot(type, data);
    }

    public static ColumnSnapshot displayColumnSnapshot(ColumnType type, CompoundTag legacyData, double heat,
            double maxHeat, int craneIndicator, RBMKControlRodPlanner.RBMKColor manualColor) {
        ColumnSnapshot snapshot = columnSnapshot(type, legacyData, heat, maxHeat, craneIndicator, false);
        snapshot.data().putByte("color", (byte) (manualColor == null ? -1 : manualColor.ordinal()));
        return snapshot;
    }

    public static ScreenDisplayPlan prepareScreen(ScreenType type, int[] selectedColumns, ColumnSnapshot[] columns) {
        ScreenType safeType = type == null ? ScreenType.NONE : type;
        if (safeType == ScreenType.NONE) {
            return new ScreenDisplayPlan(null, 0.0D, 0, false);
        }

        double value = 0.0D;
        int count = 0;
        if (selectedColumns != null && columns != null) {
            for (int index : selectedColumns) {
                if (index < 0 || index >= columns.length) {
                    continue;
                }
                ColumnSnapshot column = columns[index];
                if (column == null || column.data() == null) {
                    continue;
                }
                CompoundTag data = column.data();
                switch (safeType) {
                    case COL_TEMP -> {
                        count++;
                        value += data.getDouble("heat");
                    }
                    case FUEL_DEPLETION -> {
                        if (data.contains("enrichment")) {
                            count++;
                            value += 100.0D - data.getDouble("enrichment") * 100.0D;
                        }
                    }
                    case FUEL_POISON -> {
                        if (data.contains("xenon")) {
                            count++;
                            value += data.getDouble("xenon");
                        }
                    }
                    case FUEL_TEMP -> {
                        if (data.contains("c_heat")) {
                            count++;
                            value += data.getDouble("c_heat");
                        }
                    }
                    case ROD_EXTRACTION -> {
                        if (data.contains("level")) {
                            count++;
                            value += data.getDouble("level") * 100.0D;
                        }
                    }
                    case NONE -> {
                    }
                }
            }
        }

        double average = value / (double) count;
        String display = screenPrefix(safeType) + legacyOneDecimal(average) + screenSuffix(safeType);
        return new ScreenDisplayPlan(display, average, count, count == 0);
    }

    public static ScreenType toggleScreen(ScreenType current) {
        ScreenType safeCurrent = current == null ? ScreenType.NONE : current;
        ScreenType[] values = ScreenType.values();
        return values[(safeCurrent.ordinal() + 1) % values.length];
    }

    public static int[] selectedColumns(boolean[] selected) {
        if (selected == null) {
            return new int[0];
        }
        List<Integer> columns = new ArrayList<>();
        int max = Math.min(CONSOLE_GRID_SIZE * CONSOLE_GRID_SIZE, selected.length);
        for (int i = 0; i < max; i++) {
            if (selected[i]) {
                columns.add(i);
            }
        }
        return columns.stream().mapToInt(Integer::intValue).toArray();
    }

    public static ManualLevelControlPlan planManualLevelControl(double targetLevel, int[] selectedColumns,
            BlockPos target, int rotation) {
        List<ColumnAction> actions = selectedActions(selectedColumns, target, rotation);
        return new ManualLevelControlPlan(clamp01(targetLevel), actions);
    }

    public static ColorAssignmentPlan planColorAssignment(int rawColor, int[] selectedColumns, BlockPos target,
            int rotation) {
        RBMKControlRodPlanner.RBMKColor[] values = RBMKControlRodPlanner.RBMKColor.values();
        RBMKControlRodPlanner.RBMKColor color = values[Math.abs(rawColor % values.length)];
        return new ColorAssignmentPlan(color, selectedActions(selectedColumns, target, rotation));
    }

    public static CompressorCyclePlan planCompressorCycle(int[] selectedColumns, BlockPos target, int rotation) {
        return new CompressorCyclePlan(selectedActions(selectedColumns, target, rotation));
    }

    private static List<ColumnAction> selectedActions(int[] selectedColumns, BlockPos target, int rotation) {
        BlockPos safeTarget = target == null ? BlockPos.ZERO : target;
        List<ColumnAction> actions = new ArrayList<>();
        if (selectedColumns != null) {
            for (int index : selectedColumns) {
                if (index < 0 || index >= CONSOLE_GRID_SIZE * CONSOLE_GRID_SIZE) {
                    continue;
                }
                RelativeColumn relative = relativeColumn(index, CONSOLE_GRID_SIZE, rotation);
                actions.add(new ColumnAction(index, relative, safeTarget.offset(relative.x(), 0, relative.z())));
            }
        }
        return List.copyOf(actions);
    }

    private static String screenPrefix(ScreenType type) {
        return switch (type) {
            case COL_TEMP -> "rbmk.screen.temp=";
            case FUEL_DEPLETION -> "rbmk.screen.depletion=";
            case FUEL_POISON -> "rbmk.screen.xenon=";
            case FUEL_TEMP -> "rbmk.screen.core=";
            case ROD_EXTRACTION -> "rbmk.screen.rod=";
            case NONE -> "";
        };
    }

    private static String screenSuffix(ScreenType type) {
        return switch (type) {
            case COL_TEMP, FUEL_TEMP -> "\u00B0C";
            case FUEL_DEPLETION, FUEL_POISON, ROD_EXTRACTION -> "%";
            case NONE -> "";
        };
    }

    private static double legacyOneDecimal(double value) {
        return ((int) (value * 10.0D)) / 10.0D;
    }

    private static double clamp01(double value) {
        if (value < 0.0D || Double.isNaN(value)) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }

    public enum ColumnType {
        BLANK(0),
        FUEL(10),
        FUEL_SIM(90),
        CONTROL(20),
        CONTROL_AUTO(30),
        BOILER(40),
        MODERATOR(50),
        ABSORBER(60),
        REFLECTOR(70),
        OUTGASSER(80),
        BREEDER(100),
        STORAGE(110),
        COOLER(120),
        HEATEX(130);

        private final int offset;

        ColumnType(int offset) {
            this.offset = offset;
        }

        public int offset() {
            return offset;
        }
    }

    public enum ScreenType {
        NONE(0),
        COL_TEMP(18),
        ROD_EXTRACTION(36),
        FUEL_DEPLETION(54),
        FUEL_POISON(72),
        FUEL_TEMP(90);

        private final int offset;

        ScreenType(int offset) {
            this.offset = offset;
        }

        public int offset() {
            return offset;
        }
    }

    public record RelativeColumn(int x, int z) {
    }

    public record ColumnSnapshot(ColumnType type, CompoundTag data) {
    }

    public record ScreenDisplayPlan(String display, double average, int sampleCount, boolean legacyNoSampleDivision) {
    }

    public record ColumnAction(int index, RelativeColumn relative, BlockPos worldPos) {
    }

    public record ManualLevelControlPlan(double targetLevel, List<ColumnAction> actions) {
    }

    public record ColorAssignmentPlan(RBMKControlRodPlanner.RBMKColor color, List<ColumnAction> actions) {
    }

    public record CompressorCyclePlan(List<ColumnAction> actions) {
    }
}
