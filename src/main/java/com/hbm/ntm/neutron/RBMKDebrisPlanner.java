package com.hbm.ntm.neutron;

public final class RBMKDebrisPlanner {
    public static final double GRAVITY = 0.04D;
    public static final double GROUND_HORIZONTAL_DAMPING = 0.85D;
    public static final double GROUND_VERTICAL_BOUNCE = -0.5D;
    public static final float AIR_ROTATION_STEP = 10.0F;
    public static final double RADIATION_RADIUS = 2.5D;
    public static final int RADIATION_DURATION_TICKS = 60 * 20;
    public static final int DESPAWN_ENTITY_ID_STAGGER = 50;

    private RBMKDebrisPlanner() {
    }

    public static RBMKDebrisPlan rbmk(RBMKDebrisType type) {
        RBMKDebrisType safeType = type == null ? RBMKDebrisType.BLANK : type;
        return switch (safeType) {
            case BLANK -> new RBMKDebrisPlan(0.5F, 0.5F, 3 * 60 * 20, "debris_metal", null, false);
            case ELEMENT -> new RBMKDebrisPlan(1.0F, 1.0F, 3 * 60 * 20, "debris_metal", null, false);
            case FUEL -> new RBMKDebrisPlan(0.25F, 0.25F, 10 * 60 * 20, "debris_fuel", 9, false);
            case ROD -> new RBMKDebrisPlan(0.75F, 0.5F, 60 * 20, "debris_metal", null, false);
            case GRAPHITE -> new RBMKDebrisPlan(0.25F, 0.25F, 15 * 60 * 20, "debris_graphite", 4, false);
            case LID -> new RBMKDebrisPlan(1.0F, 0.5F, 30 * 20, "rbmk_lid", null, true);
        };
    }

    public static ZirnoxDebrisPlan zirnox(ZirnoxDebrisType type) {
        ZirnoxDebrisType safeType = type == null ? ZirnoxDebrisType.BLANK : type;
        return switch (safeType) {
            case BLANK -> new ZirnoxDebrisPlan(0.5F, 0.5F, 3 * 60 * 20, "debris_metal", null, false);
            case ELEMENT -> new ZirnoxDebrisPlan(0.75F, 0.5F, 10 * 60 * 20, "debris_element", 7, false);
            case SHRAPNEL -> new ZirnoxDebrisPlan(0.5F, 0.5F, 15 * 60 * 20, "debris_shrapnel", null, false);
            case GRAPHITE -> new ZirnoxDebrisPlan(0.25F, 0.25F, 15 * 60 * 20, "debris_graphite", 4, false);
            case CONCRETE -> new ZirnoxDebrisPlan(0.75F, 0.5F, 60 * 20, "debris_concrete", null, true);
            case EXCHANGER -> new ZirnoxDebrisPlan(1.0F, 0.5F, 60 * 20, "debris_exchanger", null, true);
        };
    }

    public static MotionPlan tickMotion(double motionX, double motionY, double motionZ, float rotation, float lastRotation,
            boolean onGround) {
        double nextMotionY = motionY - GRAVITY;
        double nextMotionX = motionX;
        double nextMotionZ = motionZ;
        float nextRotation = rotation;
        float nextLastRotation = rotation;

        if (onGround) {
            nextMotionX *= GROUND_HORIZONTAL_DAMPING;
            nextMotionZ *= GROUND_HORIZONTAL_DAMPING;
            nextMotionY *= GROUND_VERTICAL_BOUNCE;
        } else {
            nextRotation += AIR_ROTATION_STEP;
            if (nextRotation >= 360.0F) {
                nextRotation -= 360.0F;
                nextLastRotation -= 360.0F;
            }
        }

        return new MotionPlan(nextMotionX, nextMotionY, nextMotionZ, nextRotation, nextLastRotation);
    }

    public static boolean shouldDespawn(int ticksExisted, int entityId, int lifetime, boolean permanentScrap) {
        return !permanentScrap && ticksExisted > lifetime + Math.floorMod(entityId, DESPAWN_ENTITY_ID_STAGGER);
    }

    public static boolean shouldTraceBlockBreak(boolean destructiveType, double motionY) {
        return destructiveType && motionY > 0.0D;
    }

    public static DebrisTypeSelection<RBMKDebrisType> rbmkTypeFromLegacyOrdinal(int ordinal) {
        RBMKDebrisType[] values = RBMKDebrisType.values();
        int index = Math.abs(ordinal) % values.length;
        return new DebrisTypeSelection<>(values[index], index);
    }

    public static DebrisTypeSelection<ZirnoxDebrisType> zirnoxTypeFromLegacyOrdinal(int ordinal) {
        ZirnoxDebrisType[] values = ZirnoxDebrisType.values();
        int index = Math.abs(ordinal) % values.length;
        return new DebrisTypeSelection<>(values[index], index);
    }

    public enum RBMKDebrisType {
        BLANK,
        ELEMENT,
        FUEL,
        ROD,
        GRAPHITE,
        LID
    }

    public enum ZirnoxDebrisType {
        BLANK,
        ELEMENT,
        SHRAPNEL,
        GRAPHITE,
        CONCRETE,
        EXCHANGER
    }

    public record RBMKDebrisPlan(
            float width,
            float height,
            int lifetimeTicks,
            String pickupItemId,
            Integer radiationAmplifier,
            boolean breaksBlocksOnUpwardHit) {
    }

    public record ZirnoxDebrisPlan(
            float width,
            float height,
            int lifetimeTicks,
            String pickupItemId,
            Integer radiationAmplifier,
            boolean breaksBlocksOnUpwardHit) {
    }

    public record MotionPlan(
            double motionX,
            double motionY,
            double motionZ,
            float rotation,
            float lastRotation) {
    }

    public record DebrisTypeSelection<T extends Enum<T>>(T type, int legacyIndex) {
    }
}
