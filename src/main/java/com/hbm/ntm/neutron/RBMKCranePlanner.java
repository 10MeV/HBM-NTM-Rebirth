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
    public static final int NETWORK_RANGE = 250;
    public static final double MAX_RENDER_DISTANCE_SQ = 65536.0D;
    public static final String HELD_ITEM_NBT_KEY = "held";

    private RBMKCranePlanner() {
    }

    public static String[] nbtKeys() {
        return new String[] {
                "crane",
                "craneRotationOffset",
                "centerX",
                "centerY",
                "centerZ",
                "spanF",
                "spanB",
                "spanL",
                "spanR",
                "height",
                "posFront",
                "posLeft",
                HELD_ITEM_NBT_KEY
        };
    }

    public static CranePacketField[] packetLayout(boolean setUpCrane) {
        if (!setUpCrane) {
            return new CranePacketField[] {
                    new CranePacketField("setUpCrane", CraneFieldType.BOOLEAN, CraneFieldCondition.ALWAYS)
            };
        }
        return new CranePacketField[] {
                new CranePacketField("setUpCrane", CraneFieldType.BOOLEAN, CraneFieldCondition.ALWAYS),
                new CranePacketField("craneRotationOffset", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("centerX", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("centerY", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("centerZ", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("spanF", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("spanB", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("spanL", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("spanR", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("height", CraneFieldType.INT, CraneFieldCondition.IF_SETUP),
                new CranePacketField("posFront", CraneFieldType.DOUBLE, CraneFieldCondition.IF_SETUP),
                new CranePacketField("posLeft", CraneFieldType.DOUBLE, CraneFieldCondition.IF_SETUP),
                new CranePacketField("progress", CraneFieldType.DOUBLE, CraneFieldCondition.IF_SETUP),
                new CranePacketField("hasItemLoaded", CraneFieldType.BOOLEAN, CraneFieldCondition.IF_SETUP),
                new CranePacketField("loadedHeat", CraneFieldType.DOUBLE, CraneFieldCondition.IF_SETUP),
                new CranePacketField("loadedEnrichment", CraneFieldType.DOUBLE, CraneFieldCondition.IF_SETUP)
        };
    }

    public static CraneNbtSnapshot nbtSnapshot(CraneState state, boolean hasHeldItem) {
        CraneState safe = state == null ? CraneState.empty() : state;
        return new CraneNbtSnapshot(
                safe.setUpCrane(),
                safe.craneRotationOffset(),
                safe.center().getX(),
                safe.center().getY(),
                safe.center().getZ(),
                safe.bounds().spanFront(),
                safe.bounds().spanBack(),
                safe.bounds().spanLeft(),
                safe.bounds().spanRight(),
                safe.height(),
                safe.position().posFront(),
                safe.position().posLeft(),
                hasHeldItem,
                HELD_ITEM_NBT_KEY);
    }

    public static CraneState stateFromNbt(boolean setUpCrane, int craneRotationOffset, int centerX, int centerY,
            int centerZ, int spanF, int spanB, int spanL, int spanR, int height, double posFront, double posLeft) {
        return new CraneState(
                setUpCrane,
                craneRotationOffset,
                new BlockPos(centerX, centerY, centerZ),
                new CraneBounds(spanF, spanB, spanL, spanR),
                height,
                new CranePosition(posFront, posLeft));
    }

    public static CraneServerTickPlan planServerTick(boolean setUpCrane, boolean hasTargetColumn, boolean hasLoadedRod,
            double loadedRodHullHeat, double loadedRodEnrichment) {
        return new CraneServerTickPlan(
                setUpCrane,
                hasTargetColumn ? COLUMN_INDICATOR_TICKS : 0,
                hasLoadedRod ? loadedRodHullHeat : 0.0D,
                hasLoadedRod ? loadedRodEnrichment : 0.0D,
                true,
                NETWORK_RANGE);
    }

    public static CraneClientTickPlan planClientTick(CraneClientState state) {
        CraneClientState safe = state == null ? CraneClientState.empty() : state;
        double nextFront;
        double nextLeft;
        double nextProgress;
        int nextTurnProgress = safe.turnProgress();

        if (safe.turnProgress() > 0) {
            nextFront = safe.posFront() + ((safe.syncFront() - safe.posFront()) / (double) safe.turnProgress());
            nextLeft = safe.posLeft() + ((safe.syncLeft() - safe.posLeft()) / (double) safe.turnProgress());
            nextProgress = safe.progress() + ((safe.syncProgress() - safe.progress()) / (double) safe.turnProgress());
            nextTurnProgress--;
        } else {
            nextFront = safe.syncFront();
            nextLeft = safe.syncLeft();
            nextProgress = safe.syncProgress();
        }

        CraneClientState next = new CraneClientState(
                nextFront,
                nextLeft,
                nextProgress,
                safe.syncFront(),
                safe.syncLeft(),
                safe.syncProgress(),
                safe.tiltFront(),
                safe.tiltLeft(),
                safe.posFront(),
                safe.posLeft(),
                safe.progress(),
                safe.tiltFront(),
                safe.tiltLeft(),
                nextTurnProgress);
        return new CraneClientTickPlan(next);
    }

    public static CraneOperationBox operationBox(BlockPos consolePos, Direction facing, Direction side) {
        BlockPos safePos = consolePos == null ? BlockPos.ZERO : consolePos;
        Direction safeFacing = facing == null ? Direction.NORTH : facing;
        Direction safeSide = side == null ? Direction.WEST : side;
        double minX = safePos.getX() + 0.5D - safeSide.getStepX() * 1.5D;
        double maxX = safePos.getX() + 0.5D + safeSide.getStepX() * 1.5D + safeFacing.getStepX() * 2.0D;
        double minZ = safePos.getZ() + 0.5D - safeSide.getStepZ() * 1.5D;
        double maxZ = safePos.getZ() + 0.5D + safeSide.getStepZ() * 1.5D + safeFacing.getStepZ() * 2.0D;
        return new CraneOperationBox(
                Math.min(minX, maxX),
                safePos.getY(),
                Math.min(minZ, maxZ),
                Math.max(minX, maxX),
                safePos.getY() + 2.0D,
                Math.max(minZ, maxZ));
    }

    public static RenderContract renderContract() {
        return new RenderContract(true, MAX_RENDER_DISTANCE_SQ);
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

    public enum CraneFieldType {
        BOOLEAN,
        INT,
        DOUBLE
    }

    public enum CraneFieldCondition {
        ALWAYS,
        IF_SETUP
    }

    public record CranePacketField(String name, CraneFieldType type, CraneFieldCondition condition) {
    }

    public record CraneState(
            boolean setUpCrane,
            int craneRotationOffset,
            BlockPos center,
            CraneBounds bounds,
            int height,
            CranePosition position) {
        public CraneState {
            center = center == null ? BlockPos.ZERO : center;
            bounds = bounds == null ? CraneBounds.ZERO : bounds;
            position = position == null ? CranePosition.ZERO : position;
        }

        public static CraneState empty() {
            return new CraneState(false, 0, BlockPos.ZERO, CraneBounds.ZERO, 0, CranePosition.ZERO);
        }
    }

    public record CraneNbtSnapshot(
            boolean setUpCrane,
            int craneRotationOffset,
            int centerX,
            int centerY,
            int centerZ,
            int spanF,
            int spanB,
            int spanL,
            int spanR,
            int height,
            double posFront,
            double posLeft,
            boolean writeHeldItem,
            String heldItemKey) {
    }

    public record CraneServerTickPlan(
            boolean setUpCrane,
            int targetColumnIndicatorTicks,
            double loadedHeat,
            double loadedEnrichment,
            boolean sendNetwork,
            int networkRange) {
    }

    public record CraneClientState(
            double posFront,
            double posLeft,
            double progress,
            double syncFront,
            double syncLeft,
            double syncProgress,
            double tiltFront,
            double tiltLeft,
            double lastPosFront,
            double lastPosLeft,
            double lastProgress,
            double lastTiltFront,
            double lastTiltLeft,
            int turnProgress) {
        public static CraneClientState empty() {
            return new CraneClientState(0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D,
                    1.0D, 0.0D, 0.0D, 0);
        }
    }

    public record CraneClientTickPlan(CraneClientState state) {
    }

    public record CraneOperationBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    }

    public record RenderContract(boolean infiniteBoundingBox, double maxRenderDistanceSq) {
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
