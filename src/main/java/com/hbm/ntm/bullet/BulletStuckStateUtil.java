package com.hbm.ntm.bullet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BulletStuckStateUtil {
    public static final int LEGACY_DEFAULT_STUCK_SIDE = 0;
    public static final int LEGACY_NOT_STUCK_SIDE = -1;
    public static final int GROUND_DESPAWN_TICKS = 1200;
    public static final float RELEASE_RANDOM_MOTION_SCALE = 0.2F;

    public static int legacySide(@Nullable Direction side) {
        return side == null ? LEGACY_DEFAULT_STUCK_SIDE : side.get3DDataValue();
    }

    public static boolean sameLegacyStuckBlock(Level level, @Nullable BlockPos pos,
            @Nullable BlockState stuckState) {
        if (level == null || pos == null || stuckState == null) {
            return false;
        }
        return level.getBlockState(pos).getBlock() == stuckState.getBlock();
    }

    public static boolean shouldDespawnInGround(int ticksInGround) {
        return GROUND_DESPAWN_TICKS > 0 && ticksInGround == GROUND_DESPAWN_TICKS;
    }

    public static Vec3 releasedMotion(Vec3 motion, RandomSource random) {
        if (motion == null) {
            return Vec3.ZERO;
        }
        RandomSource roll = random == null ? RandomSource.create() : random;
        return new Vec3(
                motion.x * roll.nextFloat() * RELEASE_RANDOM_MOTION_SCALE,
                motion.y * roll.nextFloat() * RELEASE_RANDOM_MOTION_SCALE,
                motion.z * roll.nextFloat() * RELEASE_RANDOM_MOTION_SCALE);
    }

    private BulletStuckStateUtil() {
    }
}
