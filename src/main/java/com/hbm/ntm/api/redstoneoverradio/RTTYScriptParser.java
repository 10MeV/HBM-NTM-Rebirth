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
        UNDEFINED
    }

    final class ParseContext {
        private Level level;
        private CompoundTag variables = new CompoundTag();
        private final Map<String, Integer> jumps = new HashMap<>();
        private String buffer = "";
        private int clockSpeed = 1;
        private int current;
        private int maxClockSpeed = RTTYAutocalState.DEFAULT_MAX_CLOCK_SPEED;

        public ParseContext(Level level) {
            this.level = level;
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
            this.buffer = buffer == null ? "" : buffer;
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
