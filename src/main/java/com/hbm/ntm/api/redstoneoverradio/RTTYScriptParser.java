package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public interface RTTYScriptParser {
    StatementReturn eval(ParseContext context, String line);

    void generateJumpPoints(ParseContext context, String line, int index);

    enum StatementReturn {
        OK,
        UNRECOGNIZED_COMMAND,
        PARAMETER_ERROR,
        END_TICK,
        SHUTDOWN,
        SKIP,
        UNDEFINED,
        STACK_EXCEEDED
    }

    final class ParseContext {
        public static final int MAX_BUFFER_LENGTH = 256;
        public static final int MAX_STACK_SIZE = 256;

        private Level level;
        private CompoundTag variables = new CompoundTag();
        private final Map<String, Integer> jumps = new HashMap<>();
        private String buffer = "";
        private final String[] stack = new String[MAX_STACK_SIZE];
        private int stackSize;
        private String splitString = ";";
        private int clockSpeed = 1;
        private int current;
        private int maxClockSpeed = RTTYAutocalState.DEFAULT_MAX_CLOCK_SPEED;

        public ParseContext(Level level) {
            this.level = level;
            for (int index = 0; index < stack.length; index++) {
                stack[index] = "";
            }
        }

        public Level level() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public CompoundTag variables() {
            return variables;
        }

        public void setVariables(CompoundTag variables) {
            this.variables = variables == null ? new CompoundTag() : variables;
        }

        public Map<String, Integer> jumps() {
            return jumps;
        }

        public String buffer() {
            return buffer;
        }

        public void setBuffer(String buffer) {
            String value = buffer == null ? "" : buffer;
            this.buffer = value.length() > MAX_BUFFER_LENGTH ? value.substring(0, MAX_BUFFER_LENGTH) : value;
        }

        public String splitString() {
            return splitString;
        }

        public void setSplitString(String splitString) {
            this.splitString = splitString == null ? "" : splitString;
        }

        public boolean push(String value) {
            if (stackSize >= MAX_STACK_SIZE) {
                return false;
            }
            String entry = value == null ? "" : value;
            stack[stackSize++] = entry.length() > MAX_BUFFER_LENGTH
                    ? entry.substring(0, MAX_BUFFER_LENGTH)
                    : entry;
            return true;
        }

        public String pop() {
            if (stackSize <= 0) {
                return null;
            }
            if (stackSize > MAX_STACK_SIZE) {
                stackSize = MAX_STACK_SIZE;
            }
            String value = stack[--stackSize];
            stack[stackSize] = "";
            return value;
        }

        public String peek() {
            if (stackSize <= 0) {
                return null;
            }
            if (stackSize > MAX_STACK_SIZE) {
                stackSize = MAX_STACK_SIZE;
            }
            return stack[stackSize - 1];
        }

        public int stackSize() {
            return stackSize;
        }

        public void saveRuntime(CompoundTag tag) {
            tag.putInt("current", current);
            tag.putInt("clockSpeed", clockSpeed);
            tag.putString("buffer", buffer);
            tag.putString("splitString", splitString);
            tag.put("variables", variables.copy());
            tag.putInt("stackSize", stackSize);
            for (int index = 0; index < MAX_STACK_SIZE; index++) {
                tag.putString("st" + index, stack[index]);
            }
        }

        public void loadRuntime(CompoundTag tag) {
            current = tag.getInt("current");
            setClockSpeed(Math.max(1, tag.getInt("clockSpeed")));
            setBuffer(tag.getString("buffer"));
            setSplitString(tag.getString("splitString"));
            setVariables(tag.getCompound("variables"));
            stackSize = Math.max(0, Math.min(MAX_STACK_SIZE, tag.getInt("stackSize")));
            for (int index = 0; index < MAX_STACK_SIZE; index++) {
                String value = tag.getString("st" + index);
                stack[index] = value.length() > MAX_BUFFER_LENGTH ? value.substring(0, MAX_BUFFER_LENGTH) : value;
            }
        }

        public int clockSpeed() {
            return clockSpeed;
        }

        public void setClockSpeed(int clockSpeed) {
            this.clockSpeed = clockSpeed;
        }

        public int current() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int maxClockSpeed() {
            return maxClockSpeed;
        }

        public void setMaxClockSpeed(int maxClockSpeed) {
            this.maxClockSpeed = Math.max(1, maxClockSpeed);
        }

        public void turnOff() {
            clockSpeed = 1;
            current = 0;
            buffer = "";
            if (!variables.isEmpty()) {
                variables = new CompoundTag();
            }
        }
    }
}
