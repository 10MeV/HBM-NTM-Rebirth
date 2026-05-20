package com.hbm.ntm.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
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
