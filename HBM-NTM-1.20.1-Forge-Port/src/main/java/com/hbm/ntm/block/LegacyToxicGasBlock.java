package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
        super(properties);
        this.kind = kind;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }

        switch (kind) {
            case ASBESTOS -> {
                if (ArmorUtil.hasFineParticleProtection(living)) {
                    ArmorUtil.damageGasMaskFilter(living, 1);
                } else {
                    RadiationData.incrementAsbestos(living, 1);
                }
            }
            case COAL -> {
                if (ArmorUtil.hasCoarseParticleProtection(living)) {
                    ArmorUtil.damageGasMaskFilter(living, 1);
                } else {
                    RadiationData.incrementBlackLung(living, 10);
                }
            }
            case MONOXIDE -> {
                if (ArmorUtil.hasMonoxideGasProtection(living)) {
                    ArmorUtil.damageGasMaskFilter(living, 1);
                } else {
                    living.hurt(ModDamageSources.monoxide(level), 1.0F);
                }
            }
            case CHLORINE -> {
                if (ArmorUtil.hasLungGasProtection(living)) {
                    ArmorUtil.damageGasMaskFilter(living, 1);
                } else {
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
            level.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        } else if (kind == Kind.COAL) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    public enum Kind {
        ASBESTOS,
        COAL,
        MONOXIDE,
        CHLORINE
    }
}

