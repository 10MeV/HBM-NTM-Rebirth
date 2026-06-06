package com.hbm.ntm.api.conveyor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class ConveyorRoutePlanner {
    public static final int CREATIVE_MAX_CONVEYORS = 256;

    private ConveyorRoutePlanner() {
    }

    public static int legacyMetadataForPlacementYaw(float yaw) {
        int quadrant = Mth.floor(yaw * 4.0F / 360.0F + 0.5D) & 3;
        return switch (quadrant) {
            case 1 -> Direction.EAST.get3DDataValue();
            case 2 -> Direction.SOUTH.get3DDataValue();
            case 3 -> Direction.WEST.get3DDataValue();
            default -> Direction.NORTH.get3DDataValue();
        };
    }

    public static ConveyorBlockKind blockKindForDirection(ConveyorWandType type, Direction direction) {
        if (direction == Direction.UP) {
            return ConveyorBlockKind.LIFT;
        }
        if (direction == Direction.DOWN) {
            return ConveyorBlockKind.CHUTE;
        }
        return switch (type) {
            case EXPRESS -> ConveyorBlockKind.EXPRESS;
            case DOUBLE -> ConveyorBlockKind.DOUBLE;
            case TRIPLE -> ConveyorBlockKind.TRIPLE;
            default -> ConveyorBlockKind.REGULAR;
        };
    }

    public static int metadataForDirection(ConveyorBlockKind kind, Direction direction, Direction targetDirection, Direction horizontalDirection) {
        if (kind != ConveyorBlockKind.CHUTE && kind != ConveyorBlockKind.LIFT) {
            return direction.getOpposite().get3DDataValue();
        }
        if (targetDirection == Direction.UP || targetDirection == Direction.DOWN) {
            return horizontalDirection.getOpposite().get3DDataValue();
        }
        return targetDirection.get3DDataValue();
    }

    public static RouteResult plan(RouteContext context) {
        Direction direction = context.startSide();
        Direction targetDirection = context.endSide();
        boolean hasVertical = context.type().hasVertical();
        BlockPos targetExit = context.end().relative(targetDirection);
        BlockPos cursor = context.start().relative(direction);

        if (context.start().equals(context.end()) && context.startSide() == context.endSide()
                && direction.getAxis() == Direction.Axis.Y) {
            BlockPos placePos = cursor;
            if (!context.isReplaceable(placePos)) {
                return RouteResult.obstructed();
            }
            Placement placement = new Placement(
                    placePos,
                    blockKindForDirection(context.type(), Direction.NORTH),
                    legacyMetadataForPlacementYaw(context.playerYaw()));
            return RouteResult.success(List.of(placement));
        }

        if (direction.getAxis() == Direction.Axis.Y) {
            direction = getTargetDirection(cursor, context.end(), hasVertical);
        }

        ConveyorBlockKind targetKind = context.blockKindAt(context.end());
        boolean targetHorizontal = targetDirection.getAxis() != Direction.Axis.Y;
        boolean shouldTurnToTarget = targetHorizontal || targetKind.isEnterableTarget();

        Direction horizontalDirection = direction.getAxis() == Direction.Axis.Y
                ? Direction.from3DDataValue(legacyMetadataForPlacementYaw(context.playerYaw())).getOpposite()
                : direction;

        if (hasVertical && cursor.getY() > targetExit.getY() && context.isReplaceable(cursor.below())) {
            direction = Direction.DOWN;
        }

        List<Placement> placements = new ArrayList<>();
        for (int loopDepth = 1; loopDepth <= context.maxConveyors(); loopDepth++) {
            if (!context.isReplaceable(cursor)) {
                return RouteResult.obstructed();
            }

            ConveyorBlockKind kind = blockKindForDirection(context.type(), direction);
            int metadata = metadataForDirection(kind, direction, targetDirection, horizontalDirection);

            BlockPos next = cursor.relative(direction);
            int fromDistance = taxiDistance(cursor, targetExit);
            int toDistance = taxiDistance(next, targetExit);
            int finalDistance = taxiDistance(next, context.end());
            boolean notAtTarget = (shouldTurnToTarget ? finalDistance : fromDistance) > 0;
            boolean willBeObstructed = notAtTarget && !context.isReplaceable(next);
            boolean shouldTurn = (toDistance >= fromDistance && notAtTarget) || willBeObstructed;

            if (shouldTurn) {
                Direction newDirection = getTargetDirection(
                        cursor,
                        shouldTurnToTarget ? context.end() : targetExit,
                        targetExit,
                        direction,
                        willBeObstructed,
                        hasVertical);

                if (newDirection == Direction.UP) {
                    kind = ConveyorBlockKind.LIFT;
                } else if (newDirection == Direction.DOWN) {
                    kind = ConveyorBlockKind.CHUTE;
                } else if (rotateAroundUp(direction) == newDirection) {
                    metadata += 8;
                } else if (rotateAroundDown(direction) == newDirection) {
                    metadata += 4;
                }

                direction = newDirection;
                if (direction.getAxis() != Direction.Axis.Y) {
                    horizontalDirection = direction;
                }
            }

            placements.add(new Placement(cursor, kind, metadata));

            if (cursor.equals(targetExit)) {
                return RouteResult.success(placements);
            }

            cursor = cursor.relative(direction);
        }

        return RouteResult.notEnoughConveyors(placements);
    }

    public static Direction getTargetDirection(BlockPos from, BlockPos target, boolean hasVertical) {
        return getTargetDirection(from, target, target, null, false, hasVertical);
    }

    public static Direction getTargetDirection(BlockPos from, BlockPos target, BlockPos targetExit, Direction heading,
            boolean willBeObstructed, boolean hasVertical) {
        if (hasVertical && (from.getY() != target.getY() || from.getY() != targetExit.getY())
                && (willBeObstructed || (from.getX() == target.getX() && from.getZ() == target.getZ())
                        || (from.getX() == targetExit.getX() && from.getZ() == targetExit.getZ()))) {
            return from.getY() > target.getY() ? Direction.DOWN : Direction.UP;
        }

        if (Math.abs(from.getX() - target.getX()) > Math.abs(from.getZ() - target.getZ())) {
            if (heading == Direction.EAST || heading == Direction.WEST) {
                return from.getZ() > target.getZ() ? Direction.NORTH : Direction.SOUTH;
            }
            return from.getX() > target.getX() ? Direction.WEST : Direction.EAST;
        }

        if (heading == Direction.NORTH || heading == Direction.SOUTH) {
            return from.getX() > target.getX() ? Direction.WEST : Direction.EAST;
        }
        return from.getZ() > target.getZ() ? Direction.NORTH : Direction.SOUTH;
    }

    public static int taxiDistance(BlockPos from, BlockPos to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY()) + Math.abs(from.getZ() - to.getZ());
    }

    private static Direction rotateAroundUp(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getClockWise() : direction;
    }

    private static Direction rotateAroundDown(Direction direction) {
        return direction.getAxis().isHorizontal() ? direction.getCounterClockWise() : direction;
    }

    public record RouteContext(
            ConveyorWandType type,
            BlockPos start,
            Direction startSide,
            BlockPos end,
            Direction endSide,
            int maxConveyors,
            float playerYaw,
            Replaceability replaceability,
            BlockKindLookup blockKindLookup) {

        public boolean isReplaceable(BlockPos pos) {
            return replaceability.isReplaceable(pos);
        }

        public ConveyorBlockKind blockKindAt(BlockPos pos) {
            return blockKindLookup.blockKindAt(pos);
        }
    }

    @FunctionalInterface
    public interface Replaceability {
        boolean isReplaceable(BlockPos pos);
    }

    @FunctionalInterface
    public interface BlockKindLookup {
        ConveyorBlockKind blockKindAt(BlockPos pos);
    }

    public record Placement(BlockPos pos, ConveyorBlockKind kind, int legacyMetadata) {
    }

    public record RouteResult(Status status, List<Placement> placements) {
        public static RouteResult success(List<Placement> placements) {
            return new RouteResult(Status.SUCCESS, List.copyOf(placements));
        }

        public static RouteResult obstructed() {
            return new RouteResult(Status.OBSTRUCTED, List.of());
        }

        public static RouteResult notEnoughConveyors(List<Placement> placements) {
            return new RouteResult(Status.NOT_ENOUGH_CONVEYORS, List.copyOf(placements));
        }
    }

    public enum Status {
        SUCCESS,
        NOT_ENOUGH_CONVEYORS,
        OBSTRUCTED
    }

    public enum ConveyorBlockKind {
        REGULAR(false),
        EXPRESS(false),
        DOUBLE(false),
        TRIPLE(false),
        LIFT(true),
        CHUTE(true),
        CRANE(true),
        ENTERABLE(true),
        OTHER(false);

        private final boolean enterableTarget;

        ConveyorBlockKind(boolean enterableTarget) {
            this.enterableTarget = enterableTarget;
        }

        public boolean isEnterableTarget() {
            return enterableTarget;
        }
    }

    public enum ConveyorWandType {
        REGULAR(true),
        EXPRESS(false),
        DOUBLE(false),
        TRIPLE(false);

        private final boolean hasVertical;

        ConveyorWandType(boolean hasVertical) {
            this.hasVertical = hasVertical;
        }

        public boolean hasVertical() {
            return hasVertical;
        }
    }
}
