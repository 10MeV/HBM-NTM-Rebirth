package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class RTTYAutocalState {
    public static final int DEFAULT_MAX_CLOCK_SPEED = 20;
    public static final int HISTORY_LINES = 6;

    private static final String TAG_ON = "isOn";
    private static final String TAG_IGNORE_ERROR = "ignoreError";
    private static final String TAG_AUTO_REBOOT = "autoReboot";
    private static final String TAG_CURRENT = "current";
    private static final String TAG_CLOCK_SPEED = "clockSpeed";
    private static final String TAG_BUFFER = "buffer";
    private static final String TAG_SCRIPT = "script";
    private static final String TAG_VARIABLES = "variables";
    private static final String TAG_HISTORY = "history";

    private final RTTYScriptParser parser;
    private final RTTYScriptParser.ParseContext context;
    private final String[] history = {"", "", "", "", "", ""};
    private String[] script = new String[0];
    private boolean on;
    private boolean ignoreError;
    private boolean autoReboot;

    public RTTYAutocalState() {
        this(new RTTYMses1Parser());
    }

    public RTTYAutocalState(RTTYScriptParser parser) {
        this.parser = parser;
        this.context = new RTTYScriptParser.ParseContext(null);
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public boolean ignoreError() {
        return ignoreError;
    }

    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }

    public boolean autoReboot() {
        return autoReboot;
    }

    public void setAutoReboot(boolean autoReboot) {
        this.autoReboot = autoReboot;
    }

    public String[] scriptCopy() {
        return Arrays.copyOf(script, script.length);
    }

    public String scriptText() {
        return String.join("\n", script);
    }

    public String[] historyCopy() {
        return Arrays.copyOf(history, history.length);
    }

    public RTTYScriptParser.ParseContext context() {
        return context;
    }

    public void setMaxClockSpeed(int maxClockSpeed) {
        context.setMaxClockSpeed(maxClockSpeed);
    }

    public void tick(Level level) {
        context.setLevel(level);
        if (!on && autoReboot) {
            on = true;
        }
        if (!on) {
            return;
        }
        int emergencyBrake = 100;
        for (int i = 0; i < context.clockSpeed() && emergencyBrake > 0; i++) {
            emergencyBrake--;
            if (context.current() == script.length) {
                stop("Program has terminated");
                break;
            }
            if (context.current() < 0 || context.current() >= script.length) {
                stop("Program index is out of bounds");
                break;
            }
            try {
                int index = context.current();
                context.setCurrent(context.current() + 1);
                String line = script[index];
                RTTYScriptParser.StatementReturn result = parser.eval(context, line);
                if (result != RTTYScriptParser.StatementReturn.SKIP) {
                    pushMsg(index + ": " + line);
                }
                history[0] = "Buffer: " + context.buffer();
                if (result == RTTYScriptParser.StatementReturn.END_TICK) {
                    break;
                }
                if (result == RTTYScriptParser.StatementReturn.SHUTDOWN) {
                    stop("Program requested shutdown");
                }
                if (!ignoreError) {
                    if (result == RTTYScriptParser.StatementReturn.UNRECOGNIZED_COMMAND) {
                        stop("Unrecognized command");
                    }
                    if (result == RTTYScriptParser.StatementReturn.PARAMETER_ERROR) {
                        stop("Parameter error");
                    }
                    if (result == RTTYScriptParser.StatementReturn.UNDEFINED) {
                        stop("Undefined behavior");
                    }
                }
                if (result == RTTYScriptParser.StatementReturn.SKIP) {
                    i--;
                }
            } catch (Exception ex) {
                stop("Evaluation unsuccessful");
            }
        }
    }

    public void pushMsg(String message) {
        for (int i = 2; i < history.length; i++) {
            history[i - 1] = history[i];
        }
        history[history.length - 1] = message == null ? "" : message;
    }

    public void stop(String reason) {
        on = false;
        context.turnOff();
        pushMsg(reason);
    }

    public boolean applyControl(CompoundTag data) {
        boolean changed = false;
        if (data.contains("on")) {
            if (on) {
                stop("User requested shutdown");
            } else {
                on = true;
            }
            changed = true;
        }
        if (data.contains("ignore")) {
            ignoreError = !ignoreError;
            changed = true;
        }
        if (data.contains("auto")) {
            autoReboot = !autoReboot;
            changed = true;
        }
        if (data.contains("payload", Tag.TAG_STRING)) {
            uploadScript(data.getString("payload"));
            changed = true;
        }
        return changed;
    }

    public void uploadScript(String payload) {
        context.jumps().clear();
        script = (payload == null ? "" : payload).split("\n");
        for (int i = 0; i < script.length; i++) {
            script[i] = script[i].trim();
            parser.generateJumpPoints(context, script[i], i);
        }
        if (on) {
            stop("Script has changed");
        }
    }

    public void save(CompoundTag tag) {
        tag.putBoolean(TAG_ON, on);
        tag.putBoolean(TAG_IGNORE_ERROR, ignoreError);
        tag.putBoolean(TAG_AUTO_REBOOT, autoReboot);
        tag.putInt(TAG_CURRENT, context.current());
        tag.putInt(TAG_CLOCK_SPEED, context.clockSpeed());
        tag.putString(TAG_BUFFER, context.buffer());
        ListTag lineList = new ListTag();
        for (String line : script) {
            lineList.add(StringTag.valueOf(line));
        }
        tag.put(TAG_SCRIPT, lineList);
        tag.put(TAG_VARIABLES, context.variables());
    }

    public void saveClient(CompoundTag tag) {
        save(tag);
        ListTag historyList = new ListTag();
        for (String line : history) {
            historyList.add(StringTag.valueOf(line == null ? "" : line));
        }
        tag.put(TAG_HISTORY, historyList);
    }

    public void load(CompoundTag tag) {
        on = tag.getBoolean(TAG_ON);
        ignoreError = tag.getBoolean(TAG_IGNORE_ERROR);
        autoReboot = tag.getBoolean(TAG_AUTO_REBOOT);
        context.setCurrent(tag.getInt(TAG_CURRENT));
        context.setClockSpeed(tag.getInt(TAG_CLOCK_SPEED));
        if (context.clockSpeed() < 1) {
            context.setClockSpeed(1);
        }
        context.setBuffer(tag.getString(TAG_BUFFER));
        context.jumps().clear();
        ListTag lineList = tag.getList(TAG_SCRIPT, Tag.TAG_STRING);
        script = new String[lineList.size()];
        for (int i = 0; i < script.length; i++) {
            script[i] = lineList.getString(i);
            parser.generateJumpPoints(context, script[i], i);
        }
        context.setVariables(tag.getCompound(TAG_VARIABLES));
    }

    public void loadClient(CompoundTag tag) {
        load(tag);
        ListTag historyList = tag.getList(TAG_HISTORY, Tag.TAG_STRING);
        for (int i = 0; i < history.length; i++) {
            history[i] = i < historyList.size() ? historyList.getString(i) : "";
        }
    }
}
