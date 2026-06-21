package com.hbm.ntm.block;

import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyGasRadonBlock extends LegacyGasBlock {
    private final Kind kind;

    public LegacyGasRadonBlock(Properties properties, Kind kind) {
        super(properties, kind.red, kind.green, kind.blue);
        this.kind = kind;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (kind == Kind.TOMB) {
            living.removeEffect(ModEffects.RADAWAY.get());
            living.removeEffect(ModEffects.RADX.get());
            RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 0.5F);
            HbmLivingProperties.incrementAsbestos(living, 10);
            return;
        }
        if (ArmorUtil.hasAllProtectionAndDamageFilter(living, 3, 1, HazardClass.PARTICLE_FINE)) {
            return;
        }
        if (kind == Kind.DENSE) {
            RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F);
            RadiationUtil.addRadiationPoisoning(living, 15 * 20, 0);
            RadiationUtil.applyAsbestosGasExposure(living, 5);
        } else {
            RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 0.05F);
            RadiationUtil.applyAsbestosGasExposure(living, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.DENSE) {
            if (random.nextInt(20) == 0 && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pos.below(), ModBlocks.WASTE_EARTH.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (random.nextInt(30) == 0) {
                level.removeBlock(pos, false);
                if (ModBlocks.FALLOUT.get().defaultBlockState().canSurvive(level, pos)) {
                    level.setBlock(pos, ModBlocks.FALLOUT.get().defaultBlockState(), Block.UPDATE_ALL);
                }
                return;
            }
        } else if (kind == Kind.TOMB) {
            if (random.nextInt(10) == 0) {
                BlockState below = level.getBlockState(pos.below());
                if (below.is(Blocks.GRASS_BLOCK)) {
                    level.setBlock(pos.below(), random.nextInt(5) == 0
                            ? Blocks.COARSE_DIRT.defaultBlockState()
                            : ModBlocks.WASTE_EARTH.get().defaultBlockState(), Block.UPDATE_ALL);
                } else if ((below.is(Blocks.GRASS)
                        || below.is(Blocks.FERN)
                        || below.is(Blocks.LARGE_FERN)
                        || below.is(Blocks.VINE)
                        || below.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock)
                        && !below.isSolidRender(level, pos.below())) {
                    level.removeBlock(pos.below(), false);
                }
            }
            if (random.nextInt(600) == 0) {
                level.removeBlock(pos, false);
                return;
            }
        } else if (random.nextInt(50) == 0) {
            level.removeBlock(pos, false);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @Override
    protected Direction firstDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return random.nextInt(kind == Kind.TOMB ? 3 : 5) == 0 ? Direction.UP : Direction.DOWN;
    }

    @Override
    protected Direction secondDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return randomHorizontal(random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (kind == Kind.DENSE) {
            ParticleUtil.spawnRandomTownAuraInBlock(level, pos, random);
        }
    }

    public enum Kind {
        NORMAL(0.1F, 0.8F, 0.1F),
        DENSE(0.1F, 0.5F, 0.1F),
        TOMB(0.1F, 0.3F, 0.1F);

        private final float red;
        private final float green;
        private final float blue;

        Kind(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }
}

