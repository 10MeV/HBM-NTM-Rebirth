package com.hbm.ntm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RayTraceUtil {
    private RayTraceUtil() {
    }

    public static BlockHitResult rayTrace(Player player, double length, float partialTick) {
        return rayTrace(player, length, partialTick, ClipContext.Fluid.NONE, ClipContext.Block.COLLIDER);
    }

    public static BlockHitResult rayTrace(Player player, double length, float partialTick, ClipContext.Fluid fluidMode, ClipContext.Block blockMode) {
        Vec3 start = getPosition(player, partialTick).add(0.0D, player.getEyeHeight(), 0.0D);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 end = start.add(look.x * length, look.y * length, look.z * length);
        return player.level().clip(new ClipContext(start, end, blockMode, fluidMode, player));
    }

    /**
     * 1.7.10 {@code World#func_147447_a(..., false, false, true)} compatibility
     * for {@code Library.rayTrace}.  The old DDA walked only 200 entered cells
     * and returned the final non-collidable cell together with its entry face.
     * Modern {@link net.minecraft.world.level.BlockGetter#clip(ClipContext)}
     * instead returns a MISS at the full endpoint with the travel direction.
     */
    public static BlockHitResult rayTraceLegacyLastUncollidable(Player player, double length, float partialTick) {
        Vec3 start = getPosition(player, partialTick).add(0.0D, player.getEyeHeight(), 0.0D);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 end = start.add(look.x * length, look.y * length, look.z * length);
        return rayTraceLegacyLastUncollidable(player.level(), player, start, end);
    }

    /** Fixed-vector form used to verify the legacy DDA without client view interpolation. */
    public static BlockHitResult rayTraceLegacyLastUncollidable(Level level, Entity source, Vec3 start, Vec3 end) {
        BlockPos current = BlockPos.containing(start);
        BlockPos target = BlockPos.containing(end);
        Vec3 cursor = start;

        LegacyCellTrace initial = traceLegacyCell(level, current, cursor, end);
        if (initial.hit() != null) {
            return initial.hit();
        }

        BlockHitResult lastUncollidable = null;
        for (int steps = 0; steps < 200; steps++) {
            if (current.equals(target)) {
                return lastUncollidable;
            }

            LegacyRayStep step = nextLegacyRayStep(cursor, end, current);
            cursor = step.cursor();
            current = step.enteredBlock();

            LegacyCellTrace trace = traceLegacyCell(level, current, cursor, end);
            if (trace.hit() != null) {
                return trace.hit();
            }
            if (trace.isUncollidable()) {
                lastUncollidable = BlockHitResult.miss(cursor, step.entryFace(), current);
            }
        }
        return lastUncollidable;
    }

    /** Tests only the DDA's current cell; do not let the 200th step see cell 201 at its boundary. */
    private static LegacyCellTrace traceLegacyCell(Level level, BlockPos pos, Vec3 start, Vec3 end) {
        BlockState state = level.getBlockState(pos);
        VoxelShape collision = state.getCollisionShape(level, pos);
        return new LegacyCellTrace(collision.clip(start, end, pos), collision.isEmpty());
    }

    /** Mirrors the axis order and exact-boundary adjustment in 1.7.10 World. */
    private static LegacyRayStep nextLegacyRayStep(Vec3 cursor, Vec3 end, BlockPos current) {
        double deltaX = end.x - cursor.x;
        double deltaY = end.y - cursor.y;
        double deltaZ = end.z - cursor.z;

        double xFraction = Double.POSITIVE_INFINITY;
        double yFraction = Double.POSITIVE_INFINITY;
        double zFraction = Double.POSITIVE_INFINITY;
        if (deltaX > 0.0D) {
            xFraction = (current.getX() + 1.0D - cursor.x) / deltaX;
        } else if (deltaX < 0.0D) {
            xFraction = (current.getX() - cursor.x) / deltaX;
        }
        if (deltaY > 0.0D) {
            yFraction = (current.getY() + 1.0D - cursor.y) / deltaY;
        } else if (deltaY < 0.0D) {
            yFraction = (current.getY() - cursor.y) / deltaY;
        }
        if (deltaZ > 0.0D) {
            zFraction = (current.getZ() + 1.0D - cursor.z) / deltaZ;
        } else if (deltaZ < 0.0D) {
            zFraction = (current.getZ() - cursor.z) / deltaZ;
        }

        Direction face;
        double fraction;
        if (xFraction < yFraction && xFraction < zFraction) {
            fraction = xFraction;
            face = deltaX > 0.0D ? Direction.WEST : Direction.EAST;
        } else if (yFraction < zFraction) {
            fraction = yFraction;
            face = deltaY > 0.0D ? Direction.DOWN : Direction.UP;
        } else {
            fraction = zFraction;
            face = deltaZ > 0.0D ? Direction.NORTH : Direction.SOUTH;
        }

        Vec3 boundary = cursor.add(deltaX * fraction, deltaY * fraction, deltaZ * fraction);
        BlockPos entered = BlockPos.containing(boundary);
        if (face == Direction.EAST) {
            entered = entered.west();
        } else if (face == Direction.UP) {
            entered = entered.below();
        } else if (face == Direction.SOUTH) {
            entered = entered.north();
        }
        return new LegacyRayStep(boundary, entered, face);
    }

    private record LegacyRayStep(Vec3 cursor, BlockPos enteredBlock, Direction entryFace) {
    }

    private record LegacyCellTrace(BlockHitResult hit, boolean isUncollidable) {
    }

    public static HitResult getMouseOver(Player player, double reach) {
        return getMouseOver(player, reach, 0.0D, 1.0F);
    }

    public static HitResult getMouseOver(Player player, double reach, double threshold) {
        return getMouseOver(player, reach, threshold, 1.0F);
    }

    public static HitResult getMouseOver(Player player, double reach, double threshold, float partialTick) {
        BlockHitResult blockHit = rayTrace(player, reach, partialTick);
        Vec3 start = getPosition(player, partialTick).add(0.0D, player.getEyeHeight(), 0.0D);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 end = start.add(look.scale(reach));
        double closest = blockHit.getType() == HitResult.Type.MISS ? reach : start.distanceTo(blockHit.getLocation());
        Entity closestEntity = null;
        Vec3 closestHit = null;

        for (Entity entity : player.level().getEntities(player, player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0D),
                entity -> entity.isAlive() && entity.isPickable())) {
            double border = entity.getPickRadius() + threshold;
            java.util.Optional<Vec3> hit = entity.getBoundingBox().inflate(border).clip(start, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = start.distanceTo(hit.get());
            if (distance < closest || closest == 0.0D) {
                closestEntity = entity;
                closestHit = hit.get();
                closest = distance;
            }
        }

        return closestEntity == null ? blockHit : new EntityHitResult(closestEntity, closestHit);
    }

    public static Vec3 getPosition(Player player, float partialTick) {
        if (partialTick == 1.0F) {
            return player.position();
        }
        double x = player.xOld + (player.getX() - player.xOld) * partialTick;
        double y = player.yOld + (player.getY() - player.yOld) * partialTick;
        double z = player.zOld + (player.getZ() - player.zOld) * partialTick;
        return new Vec3(x, y, z);
    }
}
