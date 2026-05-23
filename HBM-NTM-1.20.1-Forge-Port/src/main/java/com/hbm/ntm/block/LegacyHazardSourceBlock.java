package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyHazardSourceBlock extends RadiatingHazardBlock {
    private final Effect effect;
    private final boolean beaconable;

    public LegacyHazardSourceBlock(String legacyName, Properties properties, Effect effect, boolean beaconable) {
        super(legacyName, properties);
        this.effect = effect;
        this.beaconable = beaconable;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (effect == Effect.NONE) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (!level.isEmptyBlock(target)) {
                continue;
            }

            double ix = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() * 3.0D - 1.5D;
            double iy = pos.getY() + 0.5D + direction.getStepY() + random.nextDouble() * 3.0D - 1.5D;
            double iz = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() * 3.0D - 1.5D;
            if (direction.getStepX() != 0) {
                ix = pos.getX() + 0.5D + direction.getStepX() * 0.5D + random.nextDouble() * direction.getStepX();
            }
            if (direction.getStepY() != 0) {
                iy = pos.getY() + 0.5D + direction.getStepY() * 0.5D + random.nextDouble() * direction.getStepY();
            }
            if (direction.getStepZ() != 0) {
                iz = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D + random.nextDouble() * direction.getStepZ();
            }

            if (effect == Effect.RADFOG) {
                level.addParticle(ModParticleTypes.TOWN_AURA.get(), ix, iy, iz, 0.0D, 0.0D, 0.0D);
            } else if (effect == Effect.SCHRAB) {
                level.addParticle(ModParticleTypes.SCHRAB_FOG.get(), ix, iy, iz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public enum Effect {
        NONE,
        RADFOG,
        SCHRAB
    }
}
