package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

public final class RBMKConsolePlanner {
    public static final int CONSOLE_GRID_SIZE = 15;
    public static final int DISPLAY_GRID_SIZE = 7;
    public static final int CONSOLE_SCREEN_COUNT = 6;
    public static final int FLUX_BUFFER_SIZE = 60;
    public static final int RESCAN_INTERVAL_TICKS = 10;
    public static final int NETWORK_RANGE = 50;
    public static final double PERMISSION_DISTANCE = 20.0D;
    public static final double PERMISSION_DISTANCE_SQ = PERMISSION_DISTANCE * PERMISSION_DISTANCE;
    public static final String TARGET_X_TAG = "tX";
    public static final String TARGET_Y_TAG = "tY";
    public static final String TARGET_Z_TAG = "tZ";
    public static final String SCREEN_TYPE_TAG_PREFIX = "t";
    public static final String SCREEN_COLUMNS_TAG_PREFIX = "s";
    public static final String ROTATION_TAG = "rotation";

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

    public static ConsoleTickPlan planTick(long gameTime) {
        boolean fullRescan = gameTime % RESCAN_INTERVAL_TICKS == 0L;
        return new ConsoleTickPlan(fullRescan, fullRescan, NETWORK_RANGE);
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

    public static ScreenTogglePlan planToggleScreen(int slot, ScreenState[] screens) {
        if (!validScreenSlot(slot)) {
            return new ScreenTogglePlan(false, slot, ScreenType.NONE, ScreenType.NONE, normalizeScreens(screens));
        }
        ScreenState[] next = normalizeScreens(screens);
        ScreenType oldType = next[slot].type();
        ScreenType newType = toggleScreen(oldType);
        next[slot] = new ScreenState(newType, next[slot].columns(), next[slot].display());
        return new ScreenTogglePlan(true, slot, oldType, newType, next);
    }

    public static ScreenSelectionPlan planScreenSelection(int slot, boolean[] selected, ScreenState[] screens) {
        if (!validScreenSlot(slot)) {
            return new ScreenSelectionPlan(false, slot, new int[0], normalizeScreens(screens));
        }
        ScreenState[] next = normalizeScreens(screens);
        int[] columns = selectedColumns(selected);
        next[slot] = new ScreenState(next[slot].type(), columns, next[slot].display());
        return new ScreenSelectionPlan(true, slot, columns, next);
    }

    public static ScreenState defaultScreen() {
        return new ScreenState(ScreenType.NONE, new int[0], null);
    }

    public static ScreenState[] defaultScreens() {
        ScreenState[] screens = new ScreenState[CONSOLE_SCREEN_COUNT];
        for (int i = 0; i < screens.length; i++) {
            screens[i] = defaultScreen();
        }
        return screens;
    }

    public static ConsolePacketPlan planPacket(long gameTime, ColumnSnapshot[] columns, int[] fluxBuffer,
            ScreenState[] screens) {
        boolean fullPacket = gameTime % RESCAN_INTERVAL_TICKS == 0L;
        ScreenState[] safeScreens = normalizeScreens(screens);
        if (fullPacket) {
            return new ConsolePacketPlan(
                    true,
                    true,
                    normalizeColumns(columns),
                    normalizeFluxBuffer(fluxBuffer),
                    safeScreens,
                    List.of());
        }

        List<ScreenType> types = new ArrayList<>(CONSOLE_SCREEN_COUNT);
        for (ScreenState screen : safeScreens) {
            types.add(screen.type());
        }
        return new ConsolePacketPlan(false, true, new ColumnSnapshot[0], new int[0], safeScreens, types);
    }

    public static ConsoleNbtPlan nbtSnapshot(BlockPos target, int rotation, ScreenState[] screens) {
        BlockPos safeTarget = target == null ? BlockPos.ZERO : target;
        return new ConsoleNbtPlan(
                safeTarget,
                (byte) Math.floorMod(rotation, 4),
                normalizeScreens(screens),
                TARGET_X_TAG,
                TARGET_Y_TAG,
                TARGET_Z_TAG,
                SCREEN_TYPE_TAG_PREFIX,
                SCREEN_COLUMNS_TAG_PREFIX,
                ROTATION_TAG);
    }

    public static ConsolePermissionPlan planPermission(double distanceSq) {
        return new ConsolePermissionPlan(distanceSq < PERMISSION_DISTANCE_SQ, distanceSq, PERMISSION_DISTANCE_SQ);
    }

    public static FancyStatsPlan fancyStats(ColumnSnapshot column) {
        if (column == null || column.data() == null) {
            return new FancyStatsPlan(false, List.of());
        }

        CompoundTag data = column.data();
        List<FancyStatLine> stats = new ArrayList<>();
        stats.add(FancyStatLine.translation(
                FancyStatStyle.YELLOW,
                "rbmk.heat",
                legacyOneDecimal(data.getDouble("heat")) + "\u00B0C"));

        switch (column.type() == null ? ColumnType.BLANK : column.type()) {
            case FUEL, FUEL_SIM -> {
                stats.add(FancyStatLine.translation(
                        FancyStatStyle.GREEN,
                        "rbmk.rod.depletion",
                        legacyThreeDecimal((1.0D - data.getDouble("enrichment")) * 100.0D) + "%"));
                stats.add(FancyStatLine.translation(
                        FancyStatStyle.DARK_PURPLE,
                        "rbmk.rod.xenon",
                        legacyThreeDecimal(data.getDouble("xenon")) + "%"));
                stats.add(FancyStatLine.translation(
                        FancyStatStyle.DARK_RED,
                        "rbmk.rod.coreTemp",
                        legacyOneDecimal(data.getDouble("c_coreHeat")) + "\u00B0C"));
                stats.add(FancyStatLine.translation(
                        FancyStatStyle.RED,
                        "rbmk.rod.skinTemp",
                        legacyOneDecimal(data.getDouble("c_heat")) + "\u00B0C",
                        legacyOneDecimal(data.getDouble("c_maxHeat")) + "\u00B0C"));
            }
            case BOILER -> {
                stats.add(FancyStatLine.translation(
                        FancyStatStyle.BLUE,
                        "rbmk.boiler.water",
                        Integer.toString(data.getInt("water")),
                        Integer.toString(data.getInt("maxWater"))));
                stats.add(FancyStatLine.translation(
                        FancyStatStyle.WHITE,
                        "rbmk.boiler.steam",
                        Integer.toString(data.getInt("steam")),
                        Integer.toString(data.getInt("maxSteam"))));
                stats.add(FancyStatLine.fluidTranslation(
                        FancyStatStyle.YELLOW,
                        "rbmk.boiler.type",
                        data.getShort("type")));
            }
            case CONTROL -> {
                if (data.contains("color")) {
                    short color = data.getShort("color");
                    RBMKControlRodPlanner.RBMKColor[] colors = RBMKControlRodPlanner.RBMKColor.values();
                    if (color >= 0 && color < colors.length) {
                        stats.add(FancyStatLine.translation(
                                FancyStatStyle.YELLOW,
                                "rbmk.control." + colors[color].name().toLowerCase(Locale.ROOT)));
                    }
                }
                stats.add(controlLevelLine(data));
            }
            case CONTROL_AUTO -> stats.add(controlLevelLine(data));
            case HEATEX -> {
                stats.add(FancyStatLine.fluidLiteral(
                        FancyStatStyle.BLUE,
                        data.getShort("type"),
                        data.getInt("water") + "/" + data.getInt("maxWater") + "mB"));
                stats.add(FancyStatLine.fluidLiteral(
                        FancyStatStyle.RED,
                        data.getShort("hottype"),
                        data.getInt("steam") + "/" + data.getInt("maxSteam") + "mB"));
            }
            default -> {
            }
        }

        if (data.getBoolean("moderated")) {
            stats.add(FancyStatLine.translation(FancyStatStyle.YELLOW, "rbmk.moderated"));
        }
        return new FancyStatsPlan(true, stats);
    }

    public static AABB renderBoundingBox(BlockPos origin) {
        BlockPos safeOrigin = origin == null ? BlockPos.ZERO : origin;
        return new AABB(
                safeOrigin.getX() - 2.0D,
                safeOrigin.getY(),
                safeOrigin.getZ() - 2.0D,
                safeOrigin.getX() + 3.0D,
                safeOrigin.getY() + 4.0D,
                safeOrigin.getZ() + 3.0D);
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

    private static ColumnSnapshot[] normalizeColumns(ColumnSnapshot[] columns) {
        ColumnSnapshot[] normalized = new ColumnSnapshot[CONSOLE_GRID_SIZE * CONSOLE_GRID_SIZE];
        if (columns != null) {
            System.arraycopy(columns, 0, normalized, 0, Math.min(columns.length, normalized.length));
        }
        return normalized;
    }

    private static int[] normalizeFluxBuffer(int[] fluxBuffer) {
        int[] normalized = new int[FLUX_BUFFER_SIZE];
        if (fluxBuffer != null) {
            System.arraycopy(fluxBuffer, 0, normalized, 0, Math.min(fluxBuffer.length, normalized.length));
        }
        return normalized;
    }

    private static ScreenState[] normalizeScreens(ScreenState[] screens) {
        ScreenState[] normalized = defaultScreens();
        if (screens != null) {
            for (int i = 0; i < Math.min(screens.length, normalized.length); i++) {
                normalized[i] = screens[i] == null ? defaultScreen() : screens[i];
            }
        }
        return normalized;
    }

    private static boolean validScreenSlot(int slot) {
        return slot >= 0 && slot < CONSOLE_SCREEN_COUNT;
    }

    private static FancyStatLine controlLevelLine(CompoundTag data) {
        return FancyStatLine.translation(
                FancyStatStyle.YELLOW,
                "rbmk.control.level",
                ((int) (data.getDouble("level") * 100.0D)) + "%");
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

    private static double legacyThreeDecimal(double value) {
        return ((int) (value * 1000.0D)) / 1000.0D;
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

    public record ConsoleTickPlan(boolean rescan, boolean prepareScreenInfo, int networkRange) {
    }

    public record ScreenDisplayPlan(String display, double average, int sampleCount, boolean legacyNoSampleDivision) {
    }

    public record ScreenTogglePlan(
            boolean accepted,
            int slot,
            ScreenType oldType,
            ScreenType newType,
            ScreenState[] screens) {
        public ScreenTogglePlan {
            screens = screens == null ? new ScreenState[0] : screens.clone();
        }
    }

    public record ScreenSelectionPlan(boolean accepted, int slot, int[] selectedColumns, ScreenState[] screens) {
        public ScreenSelectionPlan {
            selectedColumns = selectedColumns == null ? new int[0] : selectedColumns.clone();
            screens = screens == null ? new ScreenState[0] : screens.clone();
        }
    }

    public record ScreenState(ScreenType type, int[] columns, String display) {
        public ScreenState {
            type = type == null ? ScreenType.NONE : type;
            columns = columns == null ? new int[0] : columns.clone();
        }
    }

    public record ConsolePacketPlan(
            boolean fullPacket,
            boolean writesPacketFlag,
            ColumnSnapshot[] columns,
            int[] fluxBuffer,
            ScreenState[] screens,
            List<ScreenType> incrementalScreenTypes) {
        public ConsolePacketPlan {
            columns = columns == null ? new ColumnSnapshot[0] : columns.clone();
            fluxBuffer = fluxBuffer == null ? new int[0] : fluxBuffer.clone();
            screens = screens == null ? new ScreenState[0] : screens.clone();
            incrementalScreenTypes = incrementalScreenTypes == null ? List.of() : List.copyOf(incrementalScreenTypes);
        }
    }

    public record ConsoleNbtPlan(
            BlockPos target,
            byte rotation,
            ScreenState[] screens,
            String targetXTag,
            String targetYTag,
            String targetZTag,
            String screenTypeTagPrefix,
            String screenColumnsTagPrefix,
            String rotationTag) {
        public ConsoleNbtPlan {
            target = target == null ? BlockPos.ZERO : target;
            screens = screens == null ? new ScreenState[0] : screens.clone();
        }
    }

    public record ConsolePermissionPlan(boolean permitted, double distanceSq, double maxDistanceSq) {
    }

    public enum FancyStatStyle {
        YELLOW,
        GREEN,
        DARK_PURPLE,
        DARK_RED,
        RED,
        BLUE,
        WHITE
    }

    public record FancyStatsPlan(boolean available, List<FancyStatLine> lines) {
        public FancyStatsPlan {
            lines = lines == null ? List.of() : List.copyOf(lines);
        }
    }

    public record FancyStatLine(
            FancyStatStyle style,
            String translationKey,
            List<String> arguments,
            Short fluidId,
            boolean literalFluidLine) {
        private static FancyStatLine translation(FancyStatStyle style, String translationKey, String... arguments) {
            return new FancyStatLine(
                    style,
                    translationKey == null ? "" : translationKey,
                    arguments == null ? List.of() : List.of(arguments),
                    null,
                    false);
        }

        private static FancyStatLine fluidTranslation(FancyStatStyle style, String translationKey, short fluidId) {
            return new FancyStatLine(style, translationKey == null ? "" : translationKey, List.of(), fluidId, false);
        }

        private static FancyStatLine fluidLiteral(FancyStatStyle style, short fluidId, String value) {
            return new FancyStatLine(style, "", List.of(value == null ? "" : value), fluidId, true);
        }

        public FancyStatLine {
            arguments = arguments == null ? List.of() : List.copyOf(arguments);
        }
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
