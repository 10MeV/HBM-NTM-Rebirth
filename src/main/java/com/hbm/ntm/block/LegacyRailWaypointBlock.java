package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.rail.HbmRail;
import com.hbm.ntm.rail.HbmRailTraversal;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct modern equivalent of 1.7.10 {@code BlockRailWaypointSystem}.
 * Subclasses provide source-authored node chains and can gate individual
 * segments (for example the straight/turn path of a rail switch).
 */
@SuppressWarnings("deprecation")
public abstract class LegacyRailWaypointBlock extends LegacyXrMultiblockBlock implements HbmRail {
    private static final VoxelShape RAIL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private final List<RailDef> railDefinitions = new ArrayList<>();

    protected LegacyRailWaypointBlock(Properties properties) {
        super(properties);
    }

    protected final RailDef addRailDef(String name) {
        RailDef definition = new RailDef(name);
        railDefinitions.add(definition);
        return definition;
    }

    /** 1.7.10 {@code canCross}; switch blocks override this with saved state. */
    protected boolean canCross(Level level, BlockPos corePos, BlockState coreState, Vec3 from, Vec3 to,
            RailDef definition) {
        return true;
    }

    @Override
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        return ModBlocks.RAIL_DUMMY.get().defaultBlockState().setValue(RailDummyBlock.FACING, coreState.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return RAIL_SHAPE;
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public Vec3 getSnappingPosition(Level level, BlockPos railPos, Vec3 trainPosition) {
        return snapAndMove(level, railPos, trainPosition, Vec3.ZERO, 0.0D, new RailContext());
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion,
            double speed, RailContext context, MoveContext moveContext) {
        return snapAndMove(level, railPos, trainPosition, motion, speed, context);
    }

    @Override
    public TrackGauge getGauge(Level level, BlockPos railPos) {
        return TrackGauge.STANDARD;
    }

    private Vec3 snapAndMove(Level level, BlockPos railPos, Vec3 trainPosition, Vec3 motion, double speed,
            RailContext context) {
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, railPos);
        BlockState coreState = level.getBlockState(corePos);
        if (coreState.getBlock() != this) {
            return trainPosition;
        }
        double moveAngle = Math.atan2(motion.x, motion.z) * 180.0D / Math.PI + 90.0D;
        List<List<Link>> links = buildLinks(level, corePos, coreState);
        Link closest = null;
        Vec3 startingPosition = null;
        List<Link> closestDefinition = null;
        double bestDistance = Double.MAX_VALUE;
        boolean forward = true;

        // Preserve the original selection order, including its shared direction
        // flag across candidates, rather than replacing it with a new pathfinder.
        for (List<Link> chain : links) {
            for (Link link : chain) {
                Vec3 point = closestPointOnLink(link.from(), link.to(), trainPosition);
                if (point == null || !canCross(level, corePos, coreState, trainPosition, point, link.definition())) {
                    continue;
                }
                double distance = point.distanceTo(trainPosition);
                double linkAngle = HbmRailTraversal.yawBetween(link.to(), link.from());
                double angularDifference = angularDifference(linkAngle, -moveAngle);
                if (angularDifference < -180.0D) {
                    angularDifference += 180.0D;
                    linkAngle += 180.0D;
                    forward = false;
                }
                if (angularDifference > 0.0D) {
                    angularDifference -= 180.0D;
                    linkAngle -= 180.0D;
                    forward = false;
                }
                if (distance < bestDistance) {
                    closest = link;
                    startingPosition = point;
                    closestDefinition = chain;
                    bestDistance = distance;
                }
            }
        }

        if (closest == null || closestDefinition == null || startingPosition == null) {
            return trainPosition;
        }

        double distanceRemaining = speed;
        boolean engaged = false;
        Vec3 currentPosition = startingPosition;
        for (int index = forward ? 0 : closestDefinition.size() - 1;
                forward ? index < closestDefinition.size() : index >= 0;
                index += forward ? 1 : -1) {
            Link link = closestDefinition.get(index);
            if (!engaged) {
                if (link == closest) {
                    engaged = true;
                } else {
                    continue;
                }
            }
            Vec3 nextNode = forward ? link.to() : link.from();
            Vec3 delta = nextNode.subtract(currentPosition);
            if (!canCross(level, corePos, coreState, currentPosition, nextNode, link.definition())) {
                break;
            }
            double length = delta.length();
            if (length >= distanceRemaining) {
                context.overshoot(0.0D);
                double newYaw = HbmRailTraversal.yawBetween(nextNode, currentPosition);
                context.yaw(Math.abs(angularDifference(newYaw, moveAngle)) < 45.0D ? (float) newYaw : (float) moveAngle);
                // The legacy implementation normalized first and then divided by
                // the original link length again. This unusual distance formula
                // is retained exactly rather than silently corrected.
                return currentPosition.subtract(delta.normalize().scale(distanceRemaining / length));
            }
            distanceRemaining -= length;
            currentPosition = nextNode;
        }
        context.overshoot(distanceRemaining).nextRailPos(BlockPos.containing(currentPosition));
        return currentPosition;
    }

    private List<List<Link>> buildLinks(Level level, BlockPos corePos, BlockState coreState) {
        Vec3 core = new Vec3(corePos.getX() + 0.5D, corePos.getY(), corePos.getZ() + 0.5D);
        List<List<Link>> links = new ArrayList<>();
        for (RailDef definition : railDefinitions) {
            List<Link> chain = new ArrayList<>();
            links.add(chain);
            for (int index = 0; index < definition.nodes.size() - 1; index++) {
                Vec3 first = nodePosition(core, definition.nodes.get(index), coreState.getValue(FACING));
                Vec3 second = nodePosition(core, definition.nodes.get(index + 1), coreState.getValue(FACING));
                ParticleUtil.spawnDroneLine(level, first.x, first.y, first.z,
                        second.x - first.x, second.y - first.y, second.z - first.z, 0xFF0000);
                chain.add(new Link(first, second, definition));
            }
        }
        return links;
    }

    /** Exact XZ projection from {@code BlockRailWaypointSystem#getClosestPointOnLink}. */
    private static Vec3 closestPointOnLink(Vec3 first, Vec3 second, Vec3 point) {
        Vec3 ap = new Vec3(point.x - first.x, 0.0D, point.z - first.z);
        Vec3 ab = new Vec3(second.x - first.x, 0.0D, second.z - first.z);
        double magnitude = ab.x * ab.x + ab.z * ab.z;
        double distance = (ap.x * ab.x + ap.z * ab.z) / magnitude;
        if (distance < 0.0D) {
            return first;
        }
        if (distance > 1.0D) {
            return second;
        }
        return new Vec3(first.x + ab.x * distance, first.y + (second.y - first.y) * distance,
                first.z + ab.z * distance);
    }

    private static Vec3 nodePosition(Vec3 core, Vec3 node, Direction facing) {
        float rotation = switch (facing) {
            case NORTH -> (float) (Math.PI / 2.0D);
            case WEST -> (float) Math.PI;
            case SOUTH -> (float) (Math.PI * 1.5D);
            case EAST -> 0.0F;
            default -> throw new IllegalArgumentException("Rail waypoint facing must be horizontal: " + facing);
        };
        return core.add(node.yRot(rotation));
    }

    private static double angularDifference(double alpha, double beta) {
        double delta = (beta - alpha + 180.0D) % 360.0D - 180.0D;
        return delta < -180.0D ? delta + 360.0D : delta;
    }

    protected static final class RailDef {
        private final String name;
        private final List<Vec3> nodes = new ArrayList<>();

        private RailDef(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public RailDef addNode(double x, double y, double z) {
            nodes.add(new Vec3(x, y, z));
            return this;
        }
    }

    private record Link(Vec3 from, Vec3 to, RailDef definition) {
    }
}
