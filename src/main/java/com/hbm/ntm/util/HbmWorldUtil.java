package com.hbm.ntm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class HbmWorldUtil {
    private HbmWorldUtil() {
    }

    public static boolean checkForHeld(Entity entity, Item item) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || item == null) {
            return false;
        }
        return player.getMainHandItem().is(item);
    }

    public static List<PathStep> getBlockPosInPath(BlockPos origin, int length, Vec3 direction) {
        List<PathStep> positions = new ArrayList<>();
        for (int i = 0; i <= length; i++) {
            BlockPos pos = new BlockPos(
                    (int) (origin.getX() + direction.x * i),
                    origin.getY(),
                    (int) (origin.getZ() + direction.z * i));
            positions.add(new PathStep(pos, i));
        }
        return positions;
    }

    public static boolean isObstructed(Level level, Vec3 start, Vec3 end) {
        return isObstructed(level, start, end, false);
    }

    public static boolean isObstructedOpaque(Level level, Vec3 start, Vec3 end) {
        return isObstructed(level, start, end, true);
    }

    public static boolean isObstructed(Level level, Vec3 start, Vec3 end, Entity owner) {
        return clip(level, start, end, owner, ClipContext.Block.COLLIDER) != null;
    }

    public static boolean isObstructedOpaque(Level level, Vec3 start, Vec3 end, Entity owner) {
        return clip(level, start, end, owner, ClipContext.Block.VISUAL) != null;
    }

    private static boolean isObstructed(Level level, Vec3 start, Vec3 end, boolean opaqueOnly) {
        if (level == null || start == null || end == null || start.equals(end)) {
            return false;
        }
        return BlockGetter.traverseBlocks(start, end, level, (blockGetter, pos) -> {
            if (!level.isLoaded(pos)) {
                return null;
            }
            var state = blockGetter.getBlockState(pos);
            return (opaqueOnly ? state.canOcclude() : !state.getCollisionShape(blockGetter, pos).isEmpty())
                    ? Boolean.TRUE
                    : null;
        }, blockGetter -> Boolean.FALSE);
    }

    private static HitResult clip(Level level, Vec3 start, Vec3 end, Entity owner, ClipContext.Block blockMode) {
        if (level == null || start == null || end == null || owner == null) {
            return null;
        }
        HitResult result = level.clip(new ClipContext(start, end, blockMode, ClipContext.Fluid.NONE, owner));
        return result.getType() == HitResult.Type.MISS ? null : result;
    }

    public record PathStep(BlockPos pos, int distance) {
    }
}
