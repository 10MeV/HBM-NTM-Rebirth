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
    public static final double PANEL_PERMISSION_DISTANCE_SQ = 15.0D * 15.0D;
    public static final int NETWORK_RANGE = 50;
    public static final int TERMINAL_OC_NETWORK_RANGE = 10;
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

    public static GaugeUnit tickGauge(GaugeUnit unit, RttySignal signal) {
        GaugeUnit safe = unit == null ? defaultGauge(0) : unit;
        if (!safe.active() || safe.rtty().isEmpty()) {
            return safe;
        }
        Long parsed = parseLongSignal(signal);
        if (parsed != null) {
            return safe.withValue(parsed);
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
        Long parsed = parseLongSignal(signal);
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
        return new LeverClickPlan(safe.withTurningOn(!safe.isTurningOn()), true);
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
        if (safe.ocMode()) {
            return new TerminalEvalPlan(next, null, TerminalAction.OC_PUSH_ONLY);
        }
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
        if (safe.ocMode()) {
            return new TerminalTickPlan(safe, null, TERMINAL_OC_NETWORK_RANGE);
        }
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

    private static int clampColor(int color) {
        if (color < 0) {
            return 0;
        }
        if (color > 0xffffff) {
            return 0xffffff;
        }
        return color;
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

    public record TerminalState(String[] history, String channel, String repeatCommand, boolean ocMode) {
        public TerminalState {
            history = normalizeHistory(history);
            channel = channel == null ? "" : channel;
            repeatCommand = repeatCommand == null ? "" : repeatCommand;
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
        OC_PUSH_ONLY,
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
