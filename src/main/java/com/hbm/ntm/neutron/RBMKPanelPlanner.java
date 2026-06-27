package com.hbm.ntm.neutron;

import java.util.Arrays;

public final class RBMKPanelPlanner {
    public static final int GAUGE_COUNT = 4;
    public static final int GRAPH_COUNT = 2;
    public static final int GRAPH_HISTORY_SIZE = 30;
    public static final int INDICATOR_COUNT = 6;
    public static final int KEY_COUNT = 4;
    public static final int LEVER_COUNT = 2;
    public static final int NUMITRON_COUNT = 2;
    public static final int TERMINAL_HISTORY_SIZE = 17;
    public static final int DISPLAY_GRID_SIZE = 7;
    public static final int DISPLAY_COLUMN_COUNT = DISPLAY_GRID_SIZE * DISPLAY_GRID_SIZE;
    public static final int DISPLAY_SCAN_INTERVAL = 10;
    public static final int GRAPH_SAMPLE_INTERVAL = 10;
    public static final double PANEL_PERMISSION_DISTANCE_SQ = 15.0D * 15.0D;
    public static final int NETWORK_RANGE = 50;
    public static final int KEY_CLICK_TICKS = 7;
    public static final float LEVER_FLIP_SPEED = 1.0F / 10.0F;

    private RBMKPanelPlanner() {
    }

    public static GaugeUnit defaultGauge(int index) {
        int color = switch (index) {
            case 0 -> 0x800000;
            case 1 -> 0x804000;
            case 2 -> 0x808000;
            case 3 -> 0x000080;
            default -> 0;
        };
        return new GaugeUnit(false, false, color, "Gauge " + (index + 1), "", 0L, 100L, 0L, 0.0D, 0.0D);
    }

    public static GraphUnit defaultGraph(int index) {
        return new GraphUnit(false, false, "Graph " + (index + 1), "", new long[GRAPH_HISTORY_SIZE],
                0L, false, 0L, false);
    }

    public static IndicatorUnit defaultIndicator(int index) {
        int color = index % 2 == 0 ? 0xff0000 : 0xffff00;
        return new IndicatorUnit(false, false, false, color, "Indicator " + (index + 1), "", 0L, 100L);
    }

    public static KeyUnit defaultKey(int index) {
        int color = switch (index) {
            case 0 -> 0xff0000;
            case 1 -> 0xffff00;
            case 2 -> 0x0080ff;
            case 3 -> 0x00ff00;
            default -> 0;
        };
        return new KeyUnit(false, false, false, color, "Button " + (index + 1), "", "", 0);
    }

    public static LeverUnit defaultLever(int index) {
        return new LeverUnit(index, false, false, "Lever " + (index + 1), "", "", "", false, 0.0F, 0.0F);
    }

    public static NumitronUnit defaultNumitron(int index) {
        return new NumitronUnit(false, false, true, 0b01111111L, true, "Display " + (index + 1), "", 0L);
    }

    public static PanelTickPlan planPanelTick(PanelType type, long worldTime, boolean clientSide, boolean terminalOcMode) {
        PanelType safeType = type == null ? PanelType.GAUGE : type;
        if (clientSide) {
            boolean clientUpdate = safeType == PanelType.GAUGE || safeType == PanelType.LEVER;
            return new PanelTickPlan(false, clientUpdate, false, false, NETWORK_RANGE);
        }
        return switch (safeType) {
            case GAUGE, INDICATOR, KEYPAD, LEVER, NUMITRON ->
                    new PanelTickPlan(true, false, false, true, NETWORK_RANGE);
            case GRAPH -> new PanelTickPlan(worldTime % GRAPH_SAMPLE_INTERVAL == 0, false, false, true, NETWORK_RANGE);
            case TERMINAL -> new PanelTickPlan(true, false, false, true, NETWORK_RANGE);
            case DISPLAY -> new PanelTickPlan(worldTime % DISPLAY_SCAN_INTERVAL == 0, false,
                    worldTime % DISPLAY_SCAN_INTERVAL == 0, worldTime % DISPLAY_SCAN_INTERVAL == 0, NETWORK_RANGE);
        };
    }

    public static PanelPermissionPlan planPanelPermission(double distanceSq) {
        return new PanelPermissionPlan(distanceSq < PANEL_PERMISSION_DISTANCE_SQ, PANEL_PERMISSION_DISTANCE_SQ);
    }

    public static PanelField[] serializationLayout(PanelType type) {
        return switch (type == null ? PanelType.GAUGE : type) {
            case GAUGE -> new PanelField[] {
                    new PanelField("active", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("polling", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("color", PanelFieldType.INT, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("label", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("rtty", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("min", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("max", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("value", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS)
            };
            case GRAPH -> new PanelField[] {
                    new PanelField("active", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("polling", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("label", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("rtty", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("minBound", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("min", PanelFieldType.LONG, 1, PanelFieldCondition.IF_MIN_BOUND),
                    new PanelField("maxBound", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("max", PanelFieldType.LONG, 1, PanelFieldCondition.IF_MAX_BOUND),
                    new PanelField("values", PanelFieldType.LONG, GRAPH_HISTORY_SIZE, PanelFieldCondition.IF_ACTIVE)
            };
            case INDICATOR -> new PanelField[] {
                    new PanelField("active", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("polling", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("light", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("color", PanelFieldType.INT, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("label", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("rtty", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("min", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("max", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS)
            };
            case KEYPAD -> new PanelField[] {
                    new PanelField("active", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("polling", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("isPressed", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("color", PanelFieldType.INT, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("label", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("rtty", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("command", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS)
            };
            case LEVER -> new PanelField[] {
                    new PanelField("active", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("polling", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("flipProgress", PanelFieldType.FLOAT, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("label", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("rtty", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("commandOn", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("commandOff", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS)
            };
            case NUMITRON -> new PanelField[] {
                    new PanelField("shorten_number", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("active_digits", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("leading_zeroes", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("active", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("polling", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("label", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("rtty", PanelFieldType.STRING, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("value", PanelFieldType.LONG, 1, PanelFieldCondition.ALWAYS)
            };
            case TERMINAL -> new PanelField[] {
                    new PanelField("doesRepeat", PanelFieldType.BOOLEAN, 1, PanelFieldCondition.ALWAYS),
                    new PanelField("history", PanelFieldType.STRING, TERMINAL_HISTORY_SIZE, PanelFieldCondition.ALWAYS)
            };
            case DISPLAY -> new PanelField[] {
                    new PanelField("columnTypeOrdinal", PanelFieldType.BYTE, DISPLAY_COLUMN_COUNT,
                            PanelFieldCondition.ALWAYS),
                    new PanelField("columnData", PanelFieldType.NBT, DISPLAY_COLUMN_COUNT,
                            PanelFieldCondition.IF_COLUMN_PRESENT)
            };
        };
    }

    public static String[] nbtKeys(PanelType type, int index) {
        return switch (type == null ? PanelType.GAUGE : type) {
            case GAUGE -> indexedKeys(index, "active", "polling", "color", "label", "rtty", "min", "max", "value");
            case GRAPH -> graphNbtKeys(index);
            case INDICATOR -> indexedKeys(index, "active", "polling", "color", "label", "rtty", "min", "max", "light");
            case KEYPAD -> indexedKeys(index, "active", "polling", "isPressed", "color", "label", "rtty", "command");
            case LEVER -> indexedKeys(index, "active", "polling", "isTurningOn", "flipProgress", "label", "rtty",
                    "commandOn", "commandOff");
            case NUMITRON -> indexedKeys(index, "shorten_number", "active_digits", "leading_zeroes", "active",
                    "polling", "label", "rtty", "value");
            case TERMINAL -> terminalNbtKeys();
            case DISPLAY -> new String[] {"tX", "tY", "tZ", "rotation"};
        };
    }

    public static GaugeControlPlan planGaugeControl(GaugeUnit[] current, int activeMask, int pollingMask,
            GaugeControlEntry[] entries) {
        GaugeUnit[] units = normalizeGauges(current);
        for (int i = 0; i < units.length; i++) {
            GaugeControlEntry entry = entries != null && i < entries.length && entries[i] != null
                    ? entries[i] : GaugeControlEntry.empty();
            units[i] = new GaugeUnit(maskBit(activeMask, i), maskBit(pollingMask, i), entry.color(), entry.label(),
                    entry.rtty(), entry.min(), entry.max(), units[i].value(), units[i].renderValue(),
                    units[i].lastRenderValue());
        }
        return new GaugeControlPlan(units, PanelControlPersistence.NONE);
    }

    public static GraphControlPlan planGraphControl(GraphUnit[] current, int activeMask, int pollingMask,
            GraphControlEntry[] entries) {
        GraphUnit[] units = normalizeGraphs(current);
        for (int i = 0; i < units.length; i++) {
            GraphControlEntry entry = entries != null && i < entries.length && entries[i] != null
                    ? entries[i] : GraphControlEntry.empty();
            long min = entry.min() == null ? units[i].min() : entry.min();
            boolean minBound = entry.min() != null;
            long max = entry.max() == null ? units[i].max() : entry.max();
            boolean maxBound = entry.max() != null;
            units[i] = new GraphUnit(maskBit(activeMask, i), maskBit(pollingMask, i), entry.label(), entry.rtty(),
                    units[i].values(), min, minBound, max, maxBound);
        }
        return new GraphControlPlan(units, PanelControlPersistence.MARK_CHANGED);
    }

    public static IndicatorControlPlan planIndicatorControl(IndicatorUnit[] current, int activeMask, int pollingMask,
            IndicatorControlEntry[] entries) {
        IndicatorUnit[] units = normalizeIndicators(current);
        for (int i = 0; i < units.length; i++) {
            IndicatorControlEntry entry = entries != null && i < entries.length && entries[i] != null
                    ? entries[i] : IndicatorControlEntry.empty();
            long min = entry.min() == null ? Integer.MIN_VALUE : entry.min();
            long max = entry.max() == null ? Integer.MAX_VALUE : entry.max();
            units[i] = new IndicatorUnit(maskBit(activeMask, i), maskBit(pollingMask, i), units[i].light(),
                    entry.color(), entry.label(), entry.rtty(), min, max);
        }
        return new IndicatorControlPlan(units, PanelControlPersistence.NONE);
    }

    public static KeyControlPlan planKeyControl(KeyUnit[] current, int activeMask, int pollingMask,
            KeyControlEntry[] entries) {
        KeyUnit[] units = normalizeKeys(current);
        for (int i = 0; i < units.length; i++) {
            KeyControlEntry entry = entries != null && i < entries.length && entries[i] != null
                    ? entries[i] : KeyControlEntry.empty();
            units[i] = new KeyUnit(maskBit(activeMask, i), maskBit(pollingMask, i), units[i].isPressed(),
                    entry.color(), entry.label(), entry.rtty(), entry.command(), units[i].clickTimer());
        }
        return new KeyControlPlan(units, PanelControlPersistence.NONE);
    }

    public static LeverControlPlan planLeverControl(LeverUnit[] current, int activeMask, int pollingMask,
            LeverControlEntry[] entries) {
        LeverUnit[] units = normalizeLevers(current);
        for (int i = 0; i < units.length; i++) {
            LeverControlEntry entry = entries != null && i < entries.length && entries[i] != null
                    ? entries[i] : LeverControlEntry.empty();
            units[i] = new LeverUnit(i, maskBit(activeMask, i), maskBit(pollingMask, i), entry.label(), entry.rtty(),
                    entry.commandOn(), entry.commandOff(), units[i].isTurningOn(), units[i].flipProgress(),
                    units[i].prevFlipProgress());
        }
        return new LeverControlPlan(units, PanelControlPersistence.MARK_DIRTY);
    }

    public static NumitronControlPlan planNumitronControl(NumitronUnit[] current, int activeMask, int pollingMask,
            int shortenNumberMask, int leadingZeroesMask, NumitronControlEntry[] entries) {
        NumitronUnit[] units = normalizeNumitrons(current);
        for (int i = 0; i < units.length; i++) {
            NumitronControlEntry entry = entries != null && i < entries.length && entries[i] != null
                    ? entries[i] : NumitronControlEntry.empty();
            units[i] = new NumitronUnit(maskBit(activeMask, i), maskBit(pollingMask, i),
                    maskBit(shortenNumberMask, i), units[i].activeDigits(), maskBit(leadingZeroesMask, i),
                    entry.label(), entry.rtty(), units[i].value());
        }
        return new NumitronControlPlan(units, PanelControlPersistence.NONE);
    }

    public static TerminalControlPlan planTerminalControl(TerminalState state, String command, boolean hasCommand) {
        if (!hasCommand) {
            return new TerminalControlPlan(state == null ? TerminalState.empty() : state, null, TerminalAction.NONE,
                    PanelControlPersistence.NONE);
        }
        TerminalEvalPlan eval = evalTerminal(state, command);
        return new TerminalControlPlan(eval.state(), eval.broadcast(), eval.action(), PanelControlPersistence.MARK_CHANGED);
    }

    public static TerminalNbtSnapshot terminalNbtSnapshot(TerminalState state) {
        TerminalState safe = state == null ? TerminalState.empty() : state;
        String[] history = new String[TERMINAL_HISTORY_SIZE];
        for (int i = 0; i < history.length; i++) {
            history[i] = blankToSpace(safe.history()[i]);
        }
        return new TerminalNbtSnapshot(blankToSpace(safe.channel()), blankToSpace(safe.repeatCommand()), history);
    }

    public static TerminalState terminalStateFromNbt(String channel, String repeatCommand, boolean ocMode,
            String[] history) {
        String[] normalized = new String[TERMINAL_HISTORY_SIZE];
        if (history != null) {
            for (int i = 0; i < Math.min(history.length, normalized.length); i++) {
                normalized[i] = spaceToBlank(history[i]);
            }
        }
        return new TerminalState(normalized, spaceToBlank(channel), spaceToBlank(repeatCommand), false);
    }

    public static DisplayNbtSnapshot displayNbtSnapshot(int targetX, int targetY, int targetZ, int rotation) {
        return new DisplayNbtSnapshot(targetX, targetY, targetZ, normalizeRotation(rotation));
    }

    public static int rotateDisplay(int rotation) {
        return normalizeRotation(rotation + 1);
    }

    public static int displayRelativeX(int columnIndex, int rotation) {
        int x = columnIndex % DISPLAY_GRID_SIZE - DISPLAY_GRID_SIZE / 2;
        int z = columnIndex / DISPLAY_GRID_SIZE - DISPLAY_GRID_SIZE / 2;
        return switch (normalizeRotation(rotation)) {
            case 0 -> x;
            case 1 -> -z;
            case 2 -> -x;
            case 3 -> z;
            default -> x;
        };
    }

    public static int displayRelativeZ(int columnIndex, int rotation) {
        int x = columnIndex % DISPLAY_GRID_SIZE - DISPLAY_GRID_SIZE / 2;
        int z = columnIndex / DISPLAY_GRID_SIZE - DISPLAY_GRID_SIZE / 2;
        return switch (normalizeRotation(rotation)) {
            case 0 -> z;
            case 1 -> x;
            case 2 -> -z;
            case 3 -> -x;
            default -> z;
        };
    }

    public static GaugeUnit tickGauge(GaugeUnit unit, RttySignal signal) {
        GaugeUnit safe = unit == null ? defaultGauge(0) : unit;
        if (!safe.active() || safe.rtty().isEmpty()) {
            return safe;
        }
        Integer parsed = parseIntegerSignal(signal);
        if (parsed != null) {
            return safe.withValue(parsed.longValue());
        }
        return safe.polling() ? safe.withValue(0L) : safe;
    }

    public static GaugeUnit tickGaugeClient(GaugeUnit unit) {
        GaugeUnit safe = unit == null ? defaultGauge(0) : unit;
        double renderValue = safe.renderValue() + (safe.value() - safe.renderValue()) * 0.1D;
        return safe.withRenderValue(renderValue, safe.renderValue());
    }

    public static GraphUnit tickGraph(GraphUnit unit, RttySignal signal) {
        GraphUnit safe = unit == null ? defaultGraph(0) : unit;
        if (!safe.active() || safe.rtty().isEmpty()) {
            return safe;
        }
        Long parsed = parseLongSignal(signal);
        if (parsed != null) {
            return safe.withPushedValue(parsed);
        }
        return safe.polling() ? safe.withPushedValue(0L) : safe;
    }

    public static GraphStats graphStats(GraphUnit unit) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0L;
        int count = 0;
        long[] values = unit == null ? new long[0] : unit.values();
        for (long value : values) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
            sum += value;
            count++;
        }
        return new GraphStats(min == Long.MAX_VALUE ? 0L : min, max == Long.MIN_VALUE ? 0L : max,
                count > 0 ? (double) sum / count : 0.0D);
    }

    public static IndicatorUnit tickIndicator(IndicatorUnit unit, RttySignal signal) {
        IndicatorUnit safe = unit == null ? defaultIndicator(0) : unit;
        if (!safe.active() || safe.rtty().isEmpty()) {
            return safe;
        }
        Integer parsed = parseIntegerSignal(signal);
        if (parsed != null) {
            return safe.withLight(decideIndicatorLight(parsed, safe.min(), safe.max()));
        }
        return safe.polling() ? safe.withLight(decideIndicatorLight(0L, safe.min(), safe.max())) : safe;
    }

    public static boolean decideIndicatorLight(long value, long min, long max) {
        return min <= max ? value >= min && value <= max : value < max || value > min;
    }

    public static KeyClickPlan clickKey(KeyUnit unit) {
        KeyUnit safe = unit == null ? defaultKey(0) : unit;
        if (!safe.active()) {
            return new KeyClickPlan(safe, null, false);
        }
        if (!safe.polling()) {
            RttyBroadcast broadcast = safe.canSend() ? new RttyBroadcast(safe.rtty(), safe.command()) : null;
            return new KeyClickPlan(safe.withPressed(true, KEY_CLICK_TICKS), broadcast, true);
        }
        return new KeyClickPlan(safe.withPressed(!safe.isPressed(), safe.clickTimer()), null, true);
    }

    public static KeyTickPlan tickKey(KeyUnit unit) {
        KeyUnit safe = unit == null ? defaultKey(0) : unit;
        if (!safe.active()) {
            return new KeyTickPlan(safe, null);
        }
        if (safe.polling() && safe.isPressed()) {
            return new KeyTickPlan(safe, safe.canSend() ? new RttyBroadcast(safe.rtty(), safe.command()) : null);
        }
        if (!safe.polling() && safe.isPressed()) {
            int nextTimer = safe.clickTimer() - 1;
            if (safe.clickTimer() <= 0) {
                return new KeyTickPlan(safe.withPressed(false, nextTimer), null);
            }
            return new KeyTickPlan(safe.withPressed(true, nextTimer), null);
        }
        return new KeyTickPlan(safe, null);
    }

    public static LeverClickPlan clickLever(LeverUnit unit) {
        LeverUnit safe = unit == null ? defaultLever(0) : unit;
        if (!safe.active()) {
            return new LeverClickPlan(safe, false);
        }
        boolean startSound = safe.flipProgress() <= 0.0F || safe.flipProgress() >= 1.0F;
        return new LeverClickPlan(safe.withTurningOn(!safe.isTurningOn()), startSound);
    }

    public static LeverTickPlan tickLever(LeverUnit unit) {
        LeverUnit safe = unit == null ? defaultLever(0) : unit;
        float previous = safe.flipProgress();
        if (!safe.active()) {
            return new LeverTickPlan(safe.withPrevious(previous), null, false, false);
        }

        RttyBroadcast broadcast = null;
        boolean stopSound = false;
        boolean arcFlash = false;
        float progress = safe.flipProgress();

        if (safe.polling()) {
            if (progress >= 1.0F && safe.canSendOn()) {
                broadcast = new RttyBroadcast(safe.rtty(), safe.commandOn());
            }
            if (progress <= 0.0F && safe.canSendOff()) {
                broadcast = new RttyBroadcast(safe.rtty(), safe.commandOff());
            }
        }

        if (safe.isTurningOn() && progress < 1.0F) {
            progress += LEVER_FLIP_SPEED;
            if (progress >= 1.0F) {
                progress = 1.0F;
                stopSound = true;
                arcFlash = true;
                if (!safe.polling() && safe.canSendOn()) {
                    broadcast = new RttyBroadcast(safe.rtty(), safe.commandOn());
                }
            }
        } else if (!safe.isTurningOn() && progress > 0.0F) {
            if (previous >= 1.0F) {
                arcFlash = true;
            }
            progress -= LEVER_FLIP_SPEED;
            if (progress <= 0.0F) {
                progress = 0.0F;
                stopSound = true;
                if (!safe.polling() && safe.canSendOff()) {
                    broadcast = new RttyBroadcast(safe.rtty(), safe.commandOff());
                }
            }
        }

        return new LeverTickPlan(safe.withProgress(progress, previous), broadcast, stopSound, arcFlash);
    }

    public static NumitronUnit tickNumitron(NumitronUnit unit, RttySignal signal) {
        NumitronUnit safe = unit == null ? defaultNumitron(0) : unit;
        if (!safe.active() || safe.rtty().isEmpty()) {
            return safe;
        }
        Long parsed = parseLongSignal(signal);
        if (parsed != null) {
            return safe.withValue(parsed);
        }
        return safe.polling() ? safe.withValue(0L) : safe;
    }

    public static TerminalEvalPlan evalTerminal(TerminalState state, String command) {
        TerminalState safe = state == null ? TerminalState.empty() : state;
        if (command == null) {
            return new TerminalEvalPlan(safe, null, TerminalAction.NONE);
        }
        TerminalState next = safe.push(command);
        if (command.isEmpty()) {
            return new TerminalEvalPlan(next, null, TerminalAction.NONE);
        }
        if (command.startsWith("chan ")) {
            String channel = command.substring(5);
            return new TerminalEvalPlan(next.withChannel(channel).push("Set channel to "
                    + (channel.isEmpty() ? "<none>" : channel)), null, TerminalAction.SET_CHANNEL);
        }
        if (command.equals("chan")) {
            return new TerminalEvalPlan(next.withChannel("").push("Set channel to <none>"), null, TerminalAction.SET_CHANNEL);
        }
        if (command.startsWith("start ")) {
            return new TerminalEvalPlan(next.withRepeat(command.substring(6)).push("Repeating signal on " + safe.channel()),
                    null, TerminalAction.START_REPEAT);
        }
        if (command.equals("stop")) {
            return new TerminalEvalPlan(next.withRepeat("").push("Stopping repeat signal"), null, TerminalAction.STOP_REPEAT);
        }
        if (command.startsWith("send ")) {
            if (safe.channel().isEmpty()) {
                return new TerminalEvalPlan(next.push("Cannot send - no channel set"), null, TerminalAction.SEND_FAILED);
            }
            return new TerminalEvalPlan(next.push("Sent signal on " + safe.channel()),
                    new RttyBroadcast(safe.channel(), command.substring(5)), TerminalAction.SEND_ONCE);
        }
        if (command.equals("horse")) {
            return new TerminalEvalPlan(next.push("Horse."), null, TerminalAction.HORSE);
        }
        if (command.equals("selfdestruct")) {
            return new TerminalEvalPlan(next, null, TerminalAction.SELF_DESTRUCT);
        }
        if (command.equals("clear")) {
            return new TerminalEvalPlan(next.clearHistory(), null, TerminalAction.CLEAR);
        }
        return new TerminalEvalPlan(next.push("Unrecognized command!"), null, TerminalAction.UNRECOGNIZED);
    }

    public static TerminalTickPlan tickTerminal(TerminalState state) {
        TerminalState safe = state == null ? TerminalState.empty() : state;
        if (!safe.channel().isEmpty() && !safe.repeatCommand().isEmpty()) {
            return new TerminalTickPlan(safe, new RttyBroadcast(safe.channel(), safe.repeatCommand()), NETWORK_RANGE);
        }
        return new TerminalTickPlan(safe, null, NETWORK_RANGE);
    }

    private static Long parseLongSignal(RttySignal signal) {
        if (signal == null || signal.stale() || signal.value() == null) {
            return null;
        }
        try {
            return Long.parseLong(signal.value());
        } catch (RuntimeException ignored) {
            return 0L;
        }
    }

    private static Integer parseIntegerSignal(RttySignal signal) {
        if (signal == null || signal.stale() || signal.value() == null) {
            return null;
        }
        try {
            return Integer.parseInt(signal.value());
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private static int clampColor(int color) {
        if (color < 0) {
            return 0;
        }
        if (color > 0xffffff) {
            return 0xffffff;
        }
        return color;
    }

    private static boolean maskBit(int mask, int bit) {
        return (mask & (1 << bit)) != 0;
    }

    private static int normalizeRotation(int rotation) {
        return Math.floorMod(rotation, 4);
    }

    private static String[] indexedKeys(int index, String... prefixes) {
        String[] keys = new String[prefixes.length];
        for (int i = 0; i < prefixes.length; i++) {
            keys[i] = prefixes[i] + index;
        }
        return keys;
    }

    private static String[] graphNbtKeys(int index) {
        String[] keys = new String[8 + GRAPH_HISTORY_SIZE];
        String[] head = indexedKeys(index, "active", "polling", "label", "rtty", "minBound", "min",
                "maxBound", "max");
        System.arraycopy(head, 0, keys, 0, head.length);
        for (int i = 0; i < GRAPH_HISTORY_SIZE; i++) {
            keys[head.length + i] = "value" + index + "_" + i;
        }
        return keys;
    }

    private static String[] terminalNbtKeys() {
        String[] keys = new String[3 + TERMINAL_HISTORY_SIZE];
        keys[0] = "channel";
        keys[1] = "repeatCmd";
        keys[2] = "ocMode";
        for (int i = 0; i < TERMINAL_HISTORY_SIZE; i++) {
            keys[3 + i] = "history" + i;
        }
        return keys;
    }

    private static String blankToSpace(String value) {
        return value == null || value.isEmpty() ? " " : value;
    }

    private static String spaceToBlank(String value) {
        return value == null || value.equals(" ") ? "" : value;
    }

    private static GaugeUnit[] normalizeGauges(GaugeUnit[] current) {
        GaugeUnit[] units = new GaugeUnit[GAUGE_COUNT];
        for (int i = 0; i < units.length; i++) {
            units[i] = current != null && i < current.length && current[i] != null ? current[i] : defaultGauge(i);
        }
        return units;
    }

    private static GraphUnit[] normalizeGraphs(GraphUnit[] current) {
        GraphUnit[] units = new GraphUnit[GRAPH_COUNT];
        for (int i = 0; i < units.length; i++) {
            units[i] = current != null && i < current.length && current[i] != null ? current[i] : defaultGraph(i);
        }
        return units;
    }

    private static IndicatorUnit[] normalizeIndicators(IndicatorUnit[] current) {
        IndicatorUnit[] units = new IndicatorUnit[INDICATOR_COUNT];
        for (int i = 0; i < units.length; i++) {
            units[i] = current != null && i < current.length && current[i] != null ? current[i] : defaultIndicator(i);
        }
        return units;
    }

    private static KeyUnit[] normalizeKeys(KeyUnit[] current) {
        KeyUnit[] units = new KeyUnit[KEY_COUNT];
        for (int i = 0; i < units.length; i++) {
            units[i] = current != null && i < current.length && current[i] != null ? current[i] : defaultKey(i);
        }
        return units;
    }

    private static LeverUnit[] normalizeLevers(LeverUnit[] current) {
        LeverUnit[] units = new LeverUnit[LEVER_COUNT];
        for (int i = 0; i < units.length; i++) {
            units[i] = current != null && i < current.length && current[i] != null ? current[i] : defaultLever(i);
        }
        return units;
    }

    private static NumitronUnit[] normalizeNumitrons(NumitronUnit[] current) {
        NumitronUnit[] units = new NumitronUnit[NUMITRON_COUNT];
        for (int i = 0; i < units.length; i++) {
            units[i] = current != null && i < current.length && current[i] != null ? current[i] : defaultNumitron(i);
        }
        return units;
    }

    public enum PanelType {
        GAUGE,
        GRAPH,
        INDICATOR,
        KEYPAD,
        LEVER,
        NUMITRON,
        TERMINAL,
        DISPLAY
    }

    public enum PanelFieldType {
        BOOLEAN,
        BYTE,
        INT,
        LONG,
        FLOAT,
        STRING,
        NBT
    }

    public enum PanelFieldCondition {
        ALWAYS,
        IF_ACTIVE,
        IF_MIN_BOUND,
        IF_MAX_BOUND,
        IF_COLUMN_PRESENT
    }

    public enum PanelControlPersistence {
        NONE,
        MARK_DIRTY,
        MARK_CHANGED
    }

    public record PanelTickPlan(
            boolean updateServerUnits,
            boolean updateClientUnits,
            boolean rescanDisplay,
            boolean sendNetwork,
            int networkRange) {
    }

    public record PanelPermissionPlan(boolean permitted, double maxDistanceSq) {
    }

    public record PanelField(String name, PanelFieldType type, int count, PanelFieldCondition condition) {
    }

    public record RttySignal(String value, boolean stale) {
    }

    public record RttyBroadcast(String channel, String command) {
    }

    public record GaugeUnit(
            boolean active,
            boolean polling,
            int color,
            String label,
            String rtty,
            long min,
            long max,
            long value,
            double renderValue,
            double lastRenderValue) {
        public GaugeUnit {
            color = clampColor(color);
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
        }

        GaugeUnit withValue(long value) {
            return new GaugeUnit(active, polling, color, label, rtty, min, max, value, renderValue, lastRenderValue);
        }

        GaugeUnit withRenderValue(double renderValue, double lastRenderValue) {
            return new GaugeUnit(active, polling, color, label, rtty, min, max, value, renderValue, lastRenderValue);
        }
    }

    public record GaugeControlEntry(int color, String label, String rtty, long min, long max) {
        public GaugeControlEntry {
            color = clampColor(color);
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
        }

        static GaugeControlEntry empty() {
            return new GaugeControlEntry(0, "", "", 0L, 0L);
        }
    }

    public record GaugeControlPlan(GaugeUnit[] units, PanelControlPersistence persistence) {
    }

    public record GraphUnit(
            boolean active,
            boolean polling,
            String label,
            String rtty,
            long[] values,
            long min,
            boolean minBound,
            long max,
            boolean maxBound) {
        public GraphUnit {
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
            values = normalizeValues(values, GRAPH_HISTORY_SIZE);
            if (max < min) {
                long temp = max;
                max = min;
                min = temp;
            }
        }

        GraphUnit withPushedValue(long value) {
            long[] next = Arrays.copyOf(values, values.length);
            for (int i = 1; i < next.length; i++) {
                next[i - 1] = next[i];
            }
            next[next.length - 1] = value;
            return new GraphUnit(active, polling, label, rtty, next, min, minBound, max, maxBound);
        }
    }

    public record GraphStats(long min, long max, double average) {
    }

    public record GraphControlEntry(String label, String rtty, Long min, Long max) {
        public GraphControlEntry {
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
        }

        static GraphControlEntry empty() {
            return new GraphControlEntry("", "", null, null);
        }
    }

    public record GraphControlPlan(GraphUnit[] units, PanelControlPersistence persistence) {
    }

    public record IndicatorUnit(
            boolean active,
            boolean polling,
            boolean light,
            int color,
            String label,
            String rtty,
            long min,
            long max) {
        public IndicatorUnit {
            color = clampColor(color);
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
        }

        IndicatorUnit withLight(boolean light) {
            return new IndicatorUnit(active, polling, light, color, label, rtty, min, max);
        }
    }

    public record IndicatorControlEntry(int color, String label, String rtty, Long min, Long max) {
        public IndicatorControlEntry {
            color = clampColor(color);
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
        }

        static IndicatorControlEntry empty() {
            return new IndicatorControlEntry(0, "", "", null, null);
        }
    }

    public record IndicatorControlPlan(IndicatorUnit[] units, PanelControlPersistence persistence) {
    }

    public record KeyUnit(
            boolean active,
            boolean polling,
            boolean isPressed,
            int color,
            String label,
            String rtty,
            String command,
            int clickTimer) {
        public KeyUnit {
            color = clampColor(color);
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
            command = command == null ? "" : command;
        }

        boolean canSend() {
            return !rtty.isEmpty() && !command.isEmpty();
        }

        KeyUnit withPressed(boolean pressed, int clickTimer) {
            return new KeyUnit(active, polling, pressed, color, label, rtty, command, clickTimer);
        }
    }

    public record KeyClickPlan(KeyUnit unit, RttyBroadcast broadcast, boolean clickSound) {
    }

    public record KeyTickPlan(KeyUnit unit, RttyBroadcast broadcast) {
    }

    public record KeyControlEntry(int color, String label, String rtty, String command) {
        public KeyControlEntry {
            color = clampColor(color);
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
            command = command == null ? "" : command;
        }

        static KeyControlEntry empty() {
            return new KeyControlEntry(0, "", "", "");
        }
    }

    public record KeyControlPlan(KeyUnit[] units, PanelControlPersistence persistence) {
    }

    public record LeverUnit(
            int index,
            boolean active,
            boolean polling,
            String label,
            String rtty,
            String commandOn,
            String commandOff,
            boolean isTurningOn,
            float flipProgress,
            float prevFlipProgress) {
        public LeverUnit {
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
            commandOn = commandOn == null ? "" : commandOn;
            commandOff = commandOff == null ? "" : commandOff;
            if (flipProgress < 0.0F) {
                flipProgress = 0.0F;
            }
            if (flipProgress > 1.0F) {
                flipProgress = 1.0F;
            }
        }

        boolean canSendOn() {
            return !rtty.isEmpty() && !commandOn.isEmpty();
        }

        boolean canSendOff() {
            return !rtty.isEmpty() && !commandOff.isEmpty();
        }

        LeverUnit withTurningOn(boolean turningOn) {
            return new LeverUnit(index, active, polling, label, rtty, commandOn, commandOff,
                    turningOn, flipProgress, prevFlipProgress);
        }

        LeverUnit withPrevious(float previous) {
            return new LeverUnit(index, active, polling, label, rtty, commandOn, commandOff,
                    isTurningOn, flipProgress, previous);
        }

        LeverUnit withProgress(float progress, float previous) {
            return new LeverUnit(index, active, polling, label, rtty, commandOn, commandOff,
                    isTurningOn, progress, previous);
        }
    }

    public record LeverClickPlan(LeverUnit unit, boolean startSound) {
    }

    public record LeverTickPlan(LeverUnit unit, RttyBroadcast broadcast, boolean stopSound, boolean arcFlash) {
    }

    public record LeverControlEntry(String label, String rtty, String commandOn, String commandOff) {
        public LeverControlEntry {
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
            commandOn = commandOn == null ? "" : commandOn;
            commandOff = commandOff == null ? "" : commandOff;
        }

        static LeverControlEntry empty() {
            return new LeverControlEntry("", "", "", "");
        }
    }

    public record LeverControlPlan(LeverUnit[] units, PanelControlPersistence persistence) {
    }

    public record NumitronUnit(
            boolean active,
            boolean polling,
            boolean shortenNumber,
            long activeDigits,
            boolean leadingZeroes,
            String label,
            String rtty,
            long value) {
        public NumitronUnit {
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
            if (activeDigits < 0L) {
                activeDigits = 0L;
            }
            if (activeDigits > 127L) {
                activeDigits = 127L;
            }
        }

        NumitronUnit withValue(long value) {
            return new NumitronUnit(active, polling, shortenNumber, activeDigits, leadingZeroes, label, rtty, value);
        }
    }

    public record NumitronControlEntry(String label, String rtty) {
        public NumitronControlEntry {
            label = label == null ? "" : label;
            rtty = rtty == null ? "" : rtty;
        }

        static NumitronControlEntry empty() {
            return new NumitronControlEntry("", "");
        }
    }

    public record NumitronControlPlan(NumitronUnit[] units, PanelControlPersistence persistence) {
    }

    public record TerminalState(String[] history, String channel, String repeatCommand, boolean ocMode) {
        public TerminalState {
            history = normalizeHistory(history);
            channel = channel == null ? "" : channel;
            repeatCommand = repeatCommand == null ? "" : repeatCommand;
            ocMode = false;
        }

        public static TerminalState empty() {
            return new TerminalState(new String[TERMINAL_HISTORY_SIZE], "", "", false);
        }

        TerminalState push(String message) {
            String[] next = Arrays.copyOf(history, history.length);
            for (int i = next.length - 2; i > 0; i--) {
                next[i] = next[i - 1];
            }
            next[0] = message == null ? "" : message;
            return new TerminalState(next, channel, repeatCommand, ocMode);
        }

        TerminalState withChannel(String channel) {
            return new TerminalState(history, channel, repeatCommand, ocMode);
        }

        TerminalState withRepeat(String repeatCommand) {
            return new TerminalState(history, channel, repeatCommand, ocMode);
        }

        TerminalState clearHistory() {
            return new TerminalState(new String[TERMINAL_HISTORY_SIZE], channel, repeatCommand, ocMode);
        }
    }

    public enum TerminalAction {
        NONE,
        SET_CHANNEL,
        START_REPEAT,
        STOP_REPEAT,
        SEND_ONCE,
        SEND_FAILED,
        HORSE,
        SELF_DESTRUCT,
        CLEAR,
        UNRECOGNIZED
    }

    public record TerminalEvalPlan(TerminalState state, RttyBroadcast broadcast, TerminalAction action) {
    }

    public record TerminalTickPlan(TerminalState state, RttyBroadcast broadcast, int networkRange) {
    }

    public record TerminalControlPlan(
            TerminalState state,
            RttyBroadcast broadcast,
            TerminalAction action,
            PanelControlPersistence persistence) {
    }

    public record TerminalNbtSnapshot(String channel, String repeatCommand, String[] history) {
    }

    public record DisplayNbtSnapshot(int targetX, int targetY, int targetZ, int rotation) {
    }

    private static long[] normalizeValues(long[] values, int size) {
        long[] normalized = new long[size];
        if (values != null) {
            System.arraycopy(values, 0, normalized, 0, Math.min(values.length, normalized.length));
        }
        return normalized;
    }

    private static String[] normalizeHistory(String[] history) {
        String[] normalized = new String[TERMINAL_HISTORY_SIZE];
        if (history != null) {
            for (int i = 0; i < Math.min(history.length, normalized.length); i++) {
                normalized[i] = history[i] == null ? "" : history[i];
            }
        }
        for (int i = 0; i < normalized.length; i++) {
            if (normalized[i] == null) {
                normalized[i] = "";
            }
        }
        return normalized;
    }
}
