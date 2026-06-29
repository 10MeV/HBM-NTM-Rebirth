package com.hbm.ntm.worldgen;

import java.util.Random;
import net.minecraft.core.BlockPos;

public final class ColtanDepositUtil {
    public static final int LEGACY_RANGE = 750;

    public static BlockPos center(long worldSeed) {
        Random random = new Random(worldSeed + 5L);
        int x = (int) (random.nextGaussian() * 1500.0D);
        int z = (int) (random.nextGaussian() * 1500.0D);
        return new BlockPos(x, 0, z);
    }

    public static boolean isInsideDepositRing(long worldSeed, int x, int z, int ring) {
        BlockPos center = center(worldSeed);
        int range = LEGACY_RANGE / Math.max(1, ring);
        return x <= center.getX() + range && x >= center.getX() - range
                && z <= center.getZ() + range && z >= center.getZ() - range;
    }

    public static boolean isInsideDeposit(long worldSeed, int x, int z) {
        return isInsideDepositRing(worldSeed, x, z, 1);
    }

    private ColtanDepositUtil() {
    }
}
