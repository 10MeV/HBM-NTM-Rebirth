package com.hbm.ntm.api.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public final class ConveyorMath {
    private static final double SNAP_Y_OFFSET = 0.25D;
    private static final double BASE_SPEED = 0.0625D;
    private static final double DOUBLE_LANE_OFFSET = 0.25D;
    private static final double TRIPLE_LANE_OFFSET = 0.3125D;
    private static final double TRIPLE_CENTER_DEADZONE = 0.15D;

    private ConveyorMath() {
    }

    public static Direction legacyHorizontalDirection(int metadata) {
        Direction direction = Direction.from3DDataValue(metadata);
        return direction.getAxis().isHorizontal() ? direction : Direction.NORTH;
    }

    public static int legacyMetadataForPlacementYaw(float yaw) {
        return ConveyorRoutePlanner.legacyMetadataForPlacementYaw(yaw);
    }

    public static double baseSpeed() {
        return BASE_SPEED;
    }

    public static int baseLegacyMetadata(int metadata) {
        ConveyorPathType pathType = ConveyorPathType.fromLegacyMetadata(metadata);
        return metadata - pathType.legacyOffset() * 4;
    }

    public static Direction inputDirection(int metadata) {
        return legacyHorizontalDirection(baseLegacyMetadata(metadata));
    }

    public static Direction outputDirection(int metadata) {
        ConveyorPathType pathType = ConveyorPathType.fromLegacyMetadata(metadata);
        Direction primaryOutput = inputDirection(metadata).getOpposite();

        if (pathType == ConveyorPathType.RIGHT) {
            return rotateAroundUp(primaryOutput);
        }
        if (pathType == ConveyorPathType.LEFT) {
            return rotateAroundDown(primaryOutput);
        }
        return primaryOutput;
    }

    public static Direction travelDirection(int metadata, BlockPos pos, Vec3 itemPos) {
        ConveyorPathType pathType = ConveyorPathType.fromLegacyMetadata(metadata);
        Direction primary = inputDirection(metadata);

        if (pathType != ConveyorPathType.STRAIGHT && itemPos != null) {
            int bendSide = pathType.legacyOffset() - 1;
            Direction secondary = rotateAroundUp(primary);
            double innerX = pos.getX() + 0.5D;
            double innerZ = pos.getZ() + 0.5D;

            innerX -= -primary.getStepX() * 0.5D + secondary.getStepX() * (0.5D - bendSide);
            innerZ -= -primary.getStepZ() * 0.5D + secondary.getStepZ() * (0.5D - bendSide);

            double deltaX = Math.abs(itemPos.x - innerX);
            double deltaZ = Math.abs(itemPos.z - innerZ);

            if (deltaX + deltaZ >= 1.0D) {
                return bendSide == 0 ? secondary.getOpposite() : secondary;
            }
        }

        return primary;
    }

    public static Vec3 closestSnappingPosition(BlockPos pos, Vec3 itemPos, Direction travelDirection) {
        double clampedX = Mth.clamp(itemPos.x, pos.getX(), pos.getX() + 1.0D);
        double clampedZ = Mth.clamp(itemPos.z, pos.getZ(), pos.getZ() + 1.0D);
        double snapX = pos.getX() + 0.5D;
        double snapZ = pos.getZ() + 0.5D;

        if (travelDirection.getStepX() != 0) {
            snapX = clampedX;
        }
        if (travelDirection.getStepZ() != 0) {
            snapZ = clampedZ;
        }

        return new Vec3(snapX, pos.getY() + SNAP_Y_OFFSET, snapZ);
    }

    public static Vec3 closestSnappingPosition(int metadata, BlockPos pos, Vec3 itemPos) {
        return closestSnappingPosition(pos, itemPos, travelDirection(metadata, pos, itemPos));
    }

    public static Vec3 closestDoubleLaneSnappingPosition(int metadata, BlockPos pos, Vec3 itemPos) {
        Direction direction = travelDirection(metadata, pos, itemPos);
        Vec3 snap = closestSnappingPosition(pos, itemPos, direction);
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        if (direction.getStepX() != 0) {
            return new Vec3(snap.x, snap.y, centerZ + (itemPos.z > centerZ ? 0.25D : -0.25D));
        }
        if (direction.getStepZ() != 0) {
            return new Vec3(centerX + (itemPos.x > centerX ? 0.25D : -0.25D), snap.y, snap.z);
        }
        return snap;
    }

    public static Vec3 chuteSnappingPosition(BlockGetter level, BlockPos pos, int metadata, Vec3 itemPos) {
        Direction direction = chuteTravelDirection(level, pos, metadata, itemPos);
        if (direction == Direction.UP) {
            return new Vec3(pos.getX() + 0.5D, itemPos.y, pos.getZ() + 0.5D);
        }
        return closestSnappingPosition(pos, itemPos, direction);
    }

    public static Direction chuteTravelDirection(BlockGetter level, BlockPos pos, int metadata, Vec3 itemPos) {
        if (isConveyorOrEnterable(level, pos.below()) || itemPos.y > pos.getY() + SNAP_Y_OFFSET) {
            return Direction.UP;
        }
        return inputDirection(metadata);
    }

    public static Vec3 chuteTravelLocation(BlockGetter level, BlockPos pos, int metadata, Vec3 itemPos, double speed) {
        if (isConveyorOrEnterable(level, pos.below())) {
            speed *= 5.0D;
        } else if (itemPos.y > pos.getY() + SNAP_Y_OFFSET) {
            speed *= 3.0D;
        }
        Direction direction = chuteTravelDirection(level, pos, metadata, itemPos);
        Vec3 snap = chuteSnappingPosition(level, pos, metadata, itemPos);
        return travelLocation(pos, itemPos, direction, snap, speed);
    }

    public static Direction liftTravelDirection(BlockGetter level, BlockPos pos, int metadata) {
        return isLiftTop(level, pos) ? inputDirection(metadata) : Direction.DOWN;
    }

    public static Vec3 liftSnappingPosition(BlockGetter level, BlockPos pos, int metadata, Vec3 itemPos) {
        if (isLiftTop(level, pos)) {
            return closestSnappingPosition(pos, itemPos, inputDirection(metadata));
        }
        return new Vec3(pos.getX() + 0.5D, itemPos.y, pos.getZ() + 0.5D);
    }

    public static Vec3 liftTravelLocation(BlockGetter level, BlockPos pos, int metadata, Vec3 itemPos, double speed) {
        Direction direction = liftTravelDirection(level, pos, metadata);
        Vec3 snap = liftSnappingPosition(level, pos, metadata, itemPos);
        return travelLocation(pos, itemPos, direction, snap, speed);
    }

    public static Vec3 closestTripleLaneSnappingPosition(int metadata, BlockPos pos, Vec3 itemPos) {
        Direction direction = travelDirection(metadata, pos, itemPos);
        Vec3 snap = closestSnappingPosition(pos, itemPos, direction);
        double centerX = pos.getX() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;

        if (direction.getStepX() != 0) {
            double offset = itemPos.z > centerZ + 0.15D ? 0.3125D : itemPos.z < centerZ - 0.15D ? -0.3125D : 0.0D;
            return new Vec3(snap.x, snap.y, centerZ + offset);
        }
        if (direction.getStepZ() != 0) {
            double offset = itemPos.x > centerX + 0.15D ? 0.3125D : itemPos.x < centerX - 0.15D ? -0.3125D : 0.0D;
            return new Vec3(centerX + offset, snap.y, snap.z);
        }
        return snap;
    }

    public static Vec3 travelLocation(BlockPos pos, Vec3 itemPos, Direction travelDirection, Vec3 snap, double speed) {
        Vec3 destination = new Vec3(
                snap.x - travelDirection.getStepX() * speed,
                snap.y - travelDirection.getStepY() * speed,
                snap.z - travelDirection.getStepZ() * speed
        );
        Vec3 motion = destination.subtract(itemPos);
        double length = motion.length();

        if (length < 1.0E-7D) {
            return itemPos;
        }

        return itemPos.add(motion.scale(speed / length));
    }

    public static Vec3 travelLocation(int metadata, BlockPos pos, Vec3 itemPos, double speed) {
        Direction direction = travelDirection(metadata, pos, itemPos);
        Vec3 snap = closestSnappingPosition(pos, itemPos, direction);
        return travelLocation(pos, itemPos, direction, snap, speed);
    }

    public static Vec3 expressTravelLocation(int metadata, BlockPos pos, Vec3 itemPos, double speed) {
        return travelLocation(metadata, pos, itemPos, speed * 3.0D);
    }

    public static boolean isConveyor(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof IConveyorBelt;
    }

    public static boolean isEnterable(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof IEnterableBlock;
    }

    public static boolean isConveyorOrEnterable(BlockGetter level, BlockPos pos) {
        Object block = level.getBlockState(pos).getBlock();
        return block instanceof IConveyorBelt || block instanceof IEnterableBlock;
    }

    public static boolean isLiftTop(BlockGetter level, BlockPos pos) {
        boolean bottom = !isConveyor(level, pos.below());
        return !isConveyor(level, pos.above()) && !bottom && !isEnterable(level, pos.above());
    }

    public static Direction entryDirection(BlockPos previous, BlockPos current) {
        int dx = Integer.compare(previous.getX(), current.getX());
        int dy = Integer.compare(previous.getY(), current.getY());
        int dz = Integer.compare(previous.getZ(), current.getZ());
        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
            return null;
        }
        return Direction.fromDelta(dx, dy, dz);
    }

    public static double doubleLaneOffset() {
        return DOUBLE_LANE_OFFSET;
    }

    public static double tripleLaneOffset() {
        return TRIPLE_LANE_OFFSET;
    }

    public static double tripleCenterDeadzone() {
        return TRIPLE_CENTER_DEADZONE;
    }

    private static Direction rotateAroundUp(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getClockWise() : direction;
    }

    private static Direction rotateAroundDown(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getCounterClockWise() : direction;
    }
}
