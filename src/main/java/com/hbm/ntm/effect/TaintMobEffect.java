package com.hbm.ntm.effect;

import com.hbm.ntm.block.LegacyTaintBlock;
import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TaintMobEffect extends MobEffect {
    public TaintMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x800080);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (!isLegacyTaintMob(entity) && entity.getRandom().nextInt(40) == 0) {
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.taint(entity.level()), amplifier + 1.0F);
        }
        if (ServerConfig.taintTrailsEnabled()) {
            placeTaintTrail(entity);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 2 == 0;
    }

    private static void placeTaintTrail(LivingEntity entity) {
        BlockPos below = entity.blockPosition().below();
        if (entity.level().isOutsideBuildHeight(below) || below.getY() <= entity.level().getMinBuildHeight()) {
            return;
        }
        BlockState state = entity.level().getBlockState(below);
        if (!state.isAir() && state.isCollisionShapeFullBlock(entity.level(), below)) {
            entity.level().setBlock(below, LegacyTaintBlock.stateForLevel(14), Block.UPDATE_ALL);
        }
    }

    private static boolean isLegacyTaintMob(LivingEntity entity) {
        String simpleName = entity.getClass().getSimpleName();
        return "EntityCreeperTainted".equals(simpleName) || "EntityTaintCrab".equals(simpleName);
    }
}
