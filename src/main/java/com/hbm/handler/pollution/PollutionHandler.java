package com.hbm.handler.pollution;

import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/**
 * Legacy package facade for the 1.7.10 pollution handler.
 */
@Deprecated(forRemoval = false)
public final class PollutionHandler {
    public static final String fileName = PollutionSavedData.DATA_NAME + ".dat";
    public static final float SOOT_PER_SECOND = PollutionManager.SOOT_PER_SECOND;
    public static final float HEAVY_METAL_PER_SECOND = PollutionManager.HEAVY_METAL_PER_SECOND;
    public static final float POISON_PER_SECOND = PollutionManager.POISON_PER_SECOND;

    public static Vec3 targetCoords;

    public static void incrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        incrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void incrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        PollutionManager.incrementPollution(level, pos, toModern(type), amount);
    }

    public static void decrementPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        decrementPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void decrementPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        PollutionManager.decrementPollution(level, pos, toModern(type), amount);
    }

    public static boolean applyPollutionDelta(Level level, int x, int y, int z, PollutionType type, float amount) {
        return applyPollutionDelta(level, new BlockPos(x, y, z), type, amount);
    }

    public static boolean applyPollutionDelta(Level level, BlockPos pos, PollutionType type, float amount) {
        return PollutionManager.applyPollutionDelta(level, pos, toModern(type), amount);
    }

    public static void setPollution(Level level, int x, int y, int z, PollutionType type, float amount) {
        setPollution(level, new BlockPos(x, y, z), type, amount);
    }

    public static void setPollution(Level level, BlockPos pos, PollutionType type, float amount) {
        PollutionManager.setPollution(level, pos, toModern(type), amount);
    }

    public static float getPollution(Level level, int x, int y, int z, PollutionType type) {
        return getPollution(level, new BlockPos(x, y, z), type);
    }

    public static float getPollution(Level level, BlockPos pos, PollutionType type) {
        return PollutionManager.getPollution(level, pos, toModern(type));
    }

    public static PollutionData getPollutionData(Level level, int x, int y, int z) {
        return getPollutionData(level, new BlockPos(x, y, z));
    }

    public static PollutionData getPollutionData(Level level, BlockPos pos) {
        return PollutionData.fromModern(PollutionManager.getPollutionData(level, pos));
    }

    public static void setRampantTarget(Level level, BlockPos pos) {
        if (pos != null) {
            targetCoords = Vec3.atCenterOf(pos);
            PollutionManager.setRampantTarget(level, pos);
        }
    }

    public static PollutionType fromModern(com.hbm.ntm.pollution.PollutionType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SOOT -> PollutionType.SOOT;
            case POISON -> PollutionType.POISON;
            case HEAVYMETAL -> PollutionType.HEAVYMETAL;
            case FALLOUT -> PollutionType.FALLOUT;
        };
    }

    public static com.hbm.ntm.pollution.PollutionType toModern(PollutionType type) {
        return type == null ? null : type.modern();
    }

    public static final class PollutionData {
        public final float[] pollution = new float[PollutionType.values().length];

        public static PollutionData fromModern(PollutionSavedData.PollutionSample sample) {
            PollutionData data = new PollutionData();
            if (sample != null) {
                for (PollutionType type : PollutionType.values()) {
                    data.pollution[type.ordinal()] = sample.get(type.modern());
                }
            }
            return data;
        }

        public PollutionSavedData.PollutionSample toModern() {
            PollutionSavedData.PollutionSample sample = new PollutionSavedData.PollutionSample();
            for (PollutionType type : PollutionType.values()) {
                sample.set(type.modern(), pollution[type.ordinal()]);
            }
            return sample;
        }
    }

    public enum PollutionType {
        SOOT,
        POISON,
        HEAVYMETAL,
        FALLOUT;

        public com.hbm.ntm.pollution.PollutionType modern() {
            return switch (this) {
                case SOOT -> com.hbm.ntm.pollution.PollutionType.SOOT;
                case POISON -> com.hbm.ntm.pollution.PollutionType.POISON;
                case HEAVYMETAL -> com.hbm.ntm.pollution.PollutionType.HEAVYMETAL;
                case FALLOUT -> com.hbm.ntm.pollution.PollutionType.FALLOUT;
            };
        }

        public static PollutionType byName(String name) {
            if (name == null) {
                return null;
            }
            String normalized = name.toUpperCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
            for (PollutionType type : values()) {
                if (type.name().replace("_", "").equals(normalized)) {
                    return type;
                }
            }
            return null;
        }
    }

    private PollutionHandler() {
    }
}
