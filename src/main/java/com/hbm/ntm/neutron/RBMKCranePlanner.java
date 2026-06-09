package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class RBMKCranePlanner {
    public static final double MOVE_SPEED = 0.05D;
    public static final double LOAD_SPEED = 0.04D;
    public static final int COLUMN_INDICATOR_TICKS = 10;
    public static final int MAX_ROOM_EXTENT = 16;
    public static final int CRANE_HEIGHT = 7;
    public static final int ROTATION_STEP = 90;

    private RBMKCranePlanner() {
    }

    public static SetupPlan planSetup(BlockPos targetColumn, int columnHeight, int spanF, int spanB, int spanL,
            int spanR) {
        BlockPos center = targetColumn.above(columnHeight + 1);
        return new SetupPlan(center, clampExtent(spanF), clampExtent(spanB), clampExtent(spanL), clampExtent(spanR),
                CRANE_HEIGHT, true);
    }

    public static int planRoomExtent(boolean[] airByDistance) {
        if (airByDistance == null) {
            return MAX_ROOM_EXTENT;
        }
        int max = Math.min(MAX_ROOM_EXTENT, airByDistance.length + 1);
        for (int i = 1; i < max; i++) {
            if (!airByDistance[i - 1]) {
                return i - 1;
            }
        }
        return MAX_ROOM_EXTENT;
    }

    public static int cycleRotation(int currentRotationOffset) {
        return Math.floorMod(currentRotationOffset + ROTATION_STEP, 360);
    }

    public static MovePlan planMove(CranePosition position, MovementInput input, CraneBounds bounds,
            boolean craneLoading) {
        if (position == null) {
            position = CranePosition.ZERO;
        }
        if (input == null) {
            input = MovementInput.NONE;
        }
        if (bounds == null) {
            bounds = CraneBounds.ZERO;
        }
        if (craneLoading) {
            return new MovePlan(position, 0.0D, 0.0D);
        }

        double tiltFront = 0.0D;
        double tiltLeft = 0.0D;
        double posFront = position.posFront();
        double posLeft = position.posLeft();

        if (input.up() && !input.down()) {
            tiltFront = 30.0D;
            posFront += MOVE_SPEED;
        }
        if (!input.up() && input.down()) {
            tiltFront = -30.0D;
            posFront -= MOVE_SPEED;
        }
        if (input.left() && !input.right()) {
            tiltLeft = 30.0D;
            posLeft += MOVE_SPEED;
        }
        if (!input.left() && input.right()) {
            tiltLeft = -30.0D;
            posLeft -= MOVE_SPEED;
        }

        return new MovePlan(
                new CranePosition(
                        clamp(posFront, -bounds.spanBack(), bounds.spanFront()),
                        clamp(posLeft, -bounds.spanRight(), bounds.spanLeft())),
                tiltFront,
                tiltLeft);
    }

    public static LoadMotionPlan planLoadMotion(double progress, boolean goesDown, boolean targetLoadable,
            boolean canTargetInteract, boolean craneHasItem) {
        double nextProgress = clamp(progress, 0.0D, 1.0D);
        boolean nextGoesDown = goesDown;
        Interaction interaction = Interaction.NONE;

        if (nextGoesDown) {
            if (nextProgress > 0.0D) {
                nextProgress = Math.max(0.0D, nextProgress - LOAD_SPEED);
            } else {
                nextProgress = 0.0D;
                nextGoesDown = false;
                if (targetLoadable && canTargetInteract) {
                    interaction = craneHasItem ? Interaction.LOAD_TO_COLUMN : Interaction.UNLOAD_FROM_COLUMN;
                }
            }
        } else if (nextProgress != 1.0D) {
            nextProgress = Math.min(1.0D, nextProgress + LOAD_SPEED);
        }

        return new LoadMotionPlan(nextProgress, nextGoesDown, interaction);
    }

    public static boolean canTargetInteract(boolean columnLoadable, boolean craneHasItem, boolean columnCanLoad,
            boolean columnCanUnload) {
        if (!columnLoadable) {
            return false;
        }
        return craneHasItem ? columnCanLoad : columnCanUnload;
    }

    public static BlockPos targetColumnPos(BlockPos center, Direction facing, Direction left, double posFront,
            double posLeft) {
        Direction safeFacing = facing == null ? Direction.NORTH : facing;
        Direction safeLeft = left == null ? Direction.WEST : left;
        int x = (int) Math.floor(center.getX() - safeFacing.getStepX() * posFront - safeLeft.getStepX() * posLeft
                + 0.5D);
        int z = (int) Math.floor(center.getZ() - safeFacing.getStepZ() * posFront - safeLeft.getStepZ() * posLeft
                + 0.5D);
        return new BlockPos(x, center.getY() - 1, z);
    }

    public static RodInfoPlan rodInfo(RBMKFuelRodSpec spec, RBMKFuelRodState state, String legacyRodId) {
        if (spec == null || state == null) {
            return RodInfoPlan.empty();
        }
        return new RodInfoPlan(
                true,
                state.hullHeat(),
                state.coreHeat(),
                state.enrichment(spec),
                state.xenonLevel(),
                legacyRodId == null ? "" : legacyRodId);
    }

    private static int clampExtent(int value) {
        return Math.max(0, Math.min(MAX_ROOM_EXTENT, value));
    }

    private static double clamp(double value, double min, double max) {
        if (value < min || Double.isNaN(value)) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public enum Interaction {
        NONE,
        LOAD_TO_COLUMN,
        UNLOAD_FROM_COLUMN
    }

    public record SetupPlan(
            BlockPos center,
            int spanFront,
            int spanBack,
            int spanLeft,
            int spanRight,
            int height,
            boolean setUpCrane) {
    }

    public record CranePosition(double posFront, double posLeft) {
        public static final CranePosition ZERO = new CranePosition(0.0D, 0.0D);
    }

    public record CraneBounds(int spanFront, int spanBack, int spanLeft, int spanRight) {
        public static final CraneBounds ZERO = new CraneBounds(0, 0, 0, 0);
    }

    public record MovementInput(boolean up, boolean down, boolean left, boolean right) {
        public static final MovementInput NONE = new MovementInput(false, false, false, false);
    }

    public record MovePlan(CranePosition position, double tiltFront, double tiltLeft) {
    }

    public record LoadMotionPlan(double progress, boolean goesDown, Interaction interaction) {
    }

    public record RodInfoPlan(
            boolean present,
            double hullHeat,
            double coreHeat,
            double enrichment,
            double xenonLevel,
            String legacyRodId) {
        public static RodInfoPlan empty() {
            return new RodInfoPlan(false, 0.0D, 0.0D, 0.0D, 0.0D, "");
        }
    }
}
