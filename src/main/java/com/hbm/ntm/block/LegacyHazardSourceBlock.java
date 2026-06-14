package com.hbm.ntm.block;

import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyHazardSourceBlock extends RadiatingHazardBlock {
    private final Effect effect;

    public LegacyHazardSourceBlock(String legacyName, Properties properties, Effect effect) {
        super(legacyName, properties);
        this.effect = effect;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (effect == Effect.NONE) {
            return;
        }

        if (effect == Effect.RADFOG) {
            ParticleUtil.spawnTownAuraOnOpenFaces(level, pos, random);
        } else if (effect == Effect.SCHRAB) {
            ParticleUtil.spawnSchrabFogOnOpenFaces(level, pos, random);
        }
    }

    public enum Effect {
        NONE,
        RADFOG,
        SCHRAB
    }
}
