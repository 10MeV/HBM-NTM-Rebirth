package com.hbm.ntm.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
