package com.hbm.ntm.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Source-equivalent carrier for the old {@code BlockAshes}, constructed only as
 * {@code ash_digamma}.  Its local ash-obscuration counter is intentionally
 * client-only: legacy never synchronized it and its two renderer call sites
 * were commented out.
 */
public class LegacyAshesBlock extends FallingBlock {
    public LegacyAshesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, net.minecraft.core.BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.overlay.LegacyAshExposureOverlay.accumulate(random));
    }
}
