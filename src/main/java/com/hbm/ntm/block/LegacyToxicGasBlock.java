package com.hbm.ntm.block;

import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyToxicGasBlock extends LegacyGasBlock {
    private final Kind kind;

    public LegacyToxicGasBlock(Properties properties, Kind kind) {
        super(properties, kind.red, kind.green, kind.blue);
        this.kind = kind;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }

        switch (kind) {
            case ASBESTOS -> RadiationUtil.applyAsbestosGasExposure(living, 1);
            case COAL -> RadiationUtil.applyCoalGasExposure(living, 10);
            case MONOXIDE -> {
                if (!ArmorUtil.hasProtectionAndDamageFilter(living, 3, HazardClass.GAS_MONOXIDE, 1)) {
                    EntityDamageUtil.attackEntityFromNt(living, ModDamageSources.monoxide(level), 1.0F);
                }
            }
            case CHLORINE -> {
                if (!ArmorUtil.hasAllProtectionAndDamageFilter(living, 3, 1, HazardClass.GAS_LUNG)) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0));
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 20, 2));
                    living.addEffect(new MobEffectInstance(MobEffects.WITHER, 1 * 20, 1));
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30 * 20, 1));
                    living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30 * 20, 2));
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int dissipationChance = switch (kind) {
            case ASBESTOS -> 50;
            case COAL -> 20;
            case MONOXIDE -> 100;
            case CHLORINE -> 10;
        };
        if (random.nextInt(dissipationChance) == 0) {
            level.removeBlock(pos, false);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @Override
    protected Direction firstDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return switch (kind) {
            case ASBESTOS, COAL -> random.nextInt(5) == 0
                    ? Direction.DOWN
                    : Direction.values()[random.nextInt(6)];
            case MONOXIDE -> Direction.DOWN;
            case CHLORINE -> random.nextInt(5) == 0 ? Direction.UP : Direction.DOWN;
        };
    }

    @Override
    protected Direction secondDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return randomHorizontal(random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (kind == Kind.ASBESTOS && random.nextInt(5) == 0) {
            ParticleUtil.spawnRandomTownAuraInBlock(level, pos, random);
        } else if (kind == Kind.COAL) {
            ParticleUtil.spawnRandomSmokeInBlock(level, pos, random);
        }
    }

    public enum Kind {
        ASBESTOS(0.6F, 0.6F, 0.5F),
        COAL(0.2F, 0.2F, 0.2F),
        MONOXIDE(0.1F, 0.1F, 0.1F),
        CHLORINE(0.7F, 0.8F, 0.6F);

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

