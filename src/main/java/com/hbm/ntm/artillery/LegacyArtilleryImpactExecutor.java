package com.hbm.ntm.artillery;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.AmmoType;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ExplosionCreatorEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.GasMistEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ImpactEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ImpactKind;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ImpactProfile;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.MushroomEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.NukeEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.PhosphorusAreaEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.ShrapnelEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.SoundEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.StandardExplosionEffect;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog.VanillaExplosionEffect;
import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNukeSmall;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorDebris;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCross;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionSample;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class LegacyArtilleryImpactExecutor {
    private static final int MIST_DURATION = 150;

    public static void applyImpact(Level level, Vec3 hit, Vec3 motion, @Nullable Entity source, @Nullable AmmoType ammo) {
        if (ammo != null) {
            applyImpact(level, hit, motion, source, ammo.impactProfile(), null, null);
        }
    }

    public static void applyImpact(Level level, Vec3 hit, Vec3 motion, @Nullable Entity source,
            @Nullable AmmoType ammo, @Nullable Vec3 explosionCreatorHit) {
        if (ammo != null) {
            applyImpact(level, hit, motion, source, ammo.impactProfile(), explosionCreatorHit, null);
        }
    }

    public static void applyImpact(Level level, Vec3 hit, Vec3 motion, @Nullable Entity source,
            @Nullable AmmoType ammo, @Nullable Vec3 explosionCreatorHit, @Nullable BlockPos impactBlockPos) {
        if (ammo != null) {
            applyImpact(level, hit, motion, source, ammo.impactProfile(), explosionCreatorHit, impactBlockPos);
        }
    }

    public static void applyImpact(Level level, Vec3 hit, Vec3 motion, @Nullable Entity source,
            @Nullable ImpactProfile profile) {
        applyImpact(level, hit, motion, source, profile, null, null);
    }

    public static void applyImpact(Level level, Vec3 hit, Vec3 motion, @Nullable Entity source,
            @Nullable ImpactProfile profile, @Nullable Vec3 explosionCreatorHit) {
        applyImpact(level, hit, motion, source, profile, explosionCreatorHit, null);
    }

    public static void applyImpact(Level level, Vec3 hit, Vec3 motion, @Nullable Entity source,
            @Nullable ImpactProfile profile, @Nullable Vec3 explosionCreatorHit, @Nullable BlockPos impactBlockPos) {
        if (level == null || level.isClientSide() || hit == null || profile == null) {
            return;
        }

        Vec3 center = legacyImpactCenter(hit, motion);
        Vec3 visualHit = explosionCreatorHit == null ? hit : explosionCreatorHit;
        BlockPos legacyBlockPos = impactBlockPos == null ? BlockPos.containing(hit) : impactBlockPos;
        for (ImpactEffect effect : profile.effects()) {
            applyEffect(level, hit, center, visualHit, legacyBlockPos, source, profile.kind(), effect);
        }
    }

    public static Vec3 legacyImpactCenter(Vec3 hit, @Nullable Vec3 motion) {
        if (motion == null || motion.lengthSqr() < 1.0E-7D) {
            return hit;
        }
        return hit.subtract(motion.normalize());
    }

    private static void applyEffect(Level level, Vec3 hit, Vec3 center, Vec3 visualHit, BlockPos impactBlockPos,
            @Nullable Entity source, ImpactKind kind, ImpactEffect effect) {
        if (effect instanceof StandardExplosionEffect standard) {
            applyStandardExplosion(level, center, source, standard);
        } else if (effect instanceof VanillaExplosionEffect vanilla) {
            applyVanillaExplosion(level, center, source, vanilla);
        } else if (effect instanceof ExplosionCreatorEffect creator) {
            applyExplosionCreator(level, visualHit, creator);
        } else if (effect instanceof SoundEffect sound) {
            playLegacySound(level, sourcePositionOrHit(source, hit), sound);
        } else if (effect instanceof PhosphorusAreaEffect phosphorus) {
            applyPhosphorus(level, hit, source, phosphorus);
        } else if (effect instanceof GasMistEffect gas) {
            applyGasMist(level, center, impactBlockPos, gas);
        } else if (effect instanceof ShrapnelEffect shrapnel) {
            ExplosionLarge.spawnShrapnels(level, legacyInt(hit.x), legacyInt(hit.y), legacyInt(hit.z),
                    shrapnel.count(), 1.0F, source);
        } else if (effect instanceof MushroomEffect mushroom) {
            ParticleUtil.spawnRbmkMush(level, hit, mushroom.scale());
        } else if (effect instanceof NukeEffect nuke) {
            applyNuke(level, hit, center, kind, nuke);
        }
    }

    private static void applyStandardExplosion(Level level, Vec3 center, @Nullable Entity source,
            StandardExplosionEffect effect) {
        ExplosionVnt explosion = new ExplosionVnt(level, center.x, center.y, center.z, effect.size(), source);
        if (effect.breaksBlocks()) {
            BlockProcessorStandard processor = new BlockProcessorStandard().setNoDrop();
            BlockState debris = debrisState(effect.debrisBlock(), effect.debrisMeta());
            if (debris != null) {
                processor.withBlockEffect(new BlockMutatorDebris(debris));
            }
            explosion.setBlockAllocator(new BlockAllocatorStandard(48))
                    .setBlockProcessor(processor);
        }
        explosion.setEntityProcessor(new EntityProcessorCross(7.5D).withRangeMod(effect.rangeMod()))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .explode();
    }

    private static void applyVanillaExplosion(Level level, Vec3 center, @Nullable Entity source,
            VanillaExplosionEffect effect) {
        Level.ExplosionInteraction interaction = effect.breaksBlocks()
                ? Level.ExplosionInteraction.BLOCK
                : Level.ExplosionInteraction.NONE;
        level.explode(source, center.x, center.y, center.z, effect.size(), false, interaction);
    }

    private static void applyExplosionCreator(Level level, Vec3 hit, ExplosionCreatorEffect effect) {
        ParticleUtil.spawnExplosionLarge(level, hit.x, hit.y, hit.z, effect.size(), effect.cloudScale(),
                effect.cloudSpeed(), effect.flameScale(), effect.smokeCount(), effect.debrisCount(),
                effect.ashCount(), effect.lift(), effect.smokeScale(), effect.yMotion(), effect.lifetime());
    }

    private static void playLegacySound(Level level, Vec3 position, SoundEffect effect) {
        SoundEvent sound = legacySound(effect.legacySound());
        if (sound == null) {
            return;
        }
        level.playSound(null, position.x, position.y, position.z, sound, SoundSource.BLOCKS, effect.volume(),
                effect.pitchBase() + level.random.nextFloat() * effect.pitchRandom());
    }

    private static void applyPhosphorus(Level level, Vec3 hit, @Nullable Entity source,
            PhosphorusAreaEffect effect) {
        int x = legacyInt(hit.x);
        int y = legacyInt(hit.y);
        int z = legacyInt(hit.z);
        ExplosionLarge.spawnShrapnels(level, x, y, z, effect.shrapnelCount(), 1.0F, source);
        ExplosionChaos.igniteAllBlocks(level, x, y, z,
                effect.igniteRadius());

        Vec3 entityCenter = sourcePositionOrHit(source, hit);
        AABB bounds = new AABB(
                entityCenter.x - effect.entityRadius(), entityCenter.y - effect.entityRadius(),
                entityCenter.z - effect.entityRadius(),
                entityCenter.x + effect.entityRadius(), entityCenter.y + effect.entityRadius(),
                entityCenter.z + effect.entityRadius());
        for (Entity entity : level.getEntities(source, bounds, Entity::isAlive)) {
            entity.setSecondsOnFire(effect.entityFireSeconds());
            if (entity instanceof LivingEntity living) {
                HbmLivingProperties.ensurePhosphorus(living, effect.phosphorusTicks());
            }
        }

        ParticleUtil.spawnHazeCloud(level, hit, effect.hazeCount(), effect.hazeSpread());
        ParticleUtil.spawnRbmkMush(level, hit, effect.mushroomScale());
    }

    private static int legacyInt(double value) {
        return (int) value;
    }

    private static void applyGasMist(Level level, Vec3 center, BlockPos impactBlockPos, GasMistEffect effect) {
        FluidType fluid = HbmFluids.fromName(effect.fluidLegacyName());
        if (fluid != HbmFluids.NONE) {
            for (int i = 0; i < effect.count(); i++) {
                double x = center.x;
                double z = center.z;
                if (i > 0 && effect.scatter() > 0.0D) {
                    x += level.random.nextGaussian() * effect.scatter();
                    z += level.random.nextGaussian() * effect.scatter();
                }
                MistEntity mist = MistEntity.create(level, x, center.y + effect.yOffset(), z, fluid,
                        effect.width(), effect.height(), MIST_DURATION);
                level.addFreshEntity(mist);
            }
        }

        PollutionSample sample = new PollutionSample();
        for (Map.Entry<PollutionType, Float> entry : effect.pollution().entrySet()) {
            Float amount = entry.getValue();
            if (amount != null && Float.isFinite(amount) && amount != 0.0F) {
                sample.add(entry.getKey(), amount);
            }
        }
        PollutionManager.applyPollutionDelta(level, impactBlockPos, sample);
    }

    private static void applyNuke(Level level, Vec3 hit, Vec3 center, ImpactKind kind, NukeEffect effect) {
        if ("ExplosionNukeSmall.PARAMS_MEDIUM".equals(effect.legacyParameter())) {
            ExplosionNukeSmall.explode(level, center.x, center.y, center.z, ExplosionNukeSmall.PARAMS_MEDIUM);
        } else if ("BombConfig.missileRadius".equals(effect.legacyParameter()) || kind == ImpactKind.NUKE) {
            NuclearExplosionUtil.spawnNuclear(level, NuclearExplosionUtil.missileRadius(), hit.x, hit.y, hit.z);
        }
    }

    @Nullable
    private static BlockState debrisState(String legacyName, int legacyMeta) {
        if (legacyName == null || legacyName.isBlank()) {
            return null;
        }
        String mappedName = legacyName;
        if ("block_slag".equals(legacyName) && legacyMeta == 1) {
            mappedName = "block_slag_broken";
        }
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(mappedName);
        return block == null ? null : block.get().defaultBlockState();
    }

    @Nullable
    private static SoundEvent legacySound(String legacySound) {
        return LegacySoundPlayer.resolveEvent(legacySound);
    }

    private static Vec3 sourcePositionOrHit(@Nullable Entity source, Vec3 hit) {
        return source == null ? hit : source.position();
    }

    private LegacyArtilleryImpactExecutor() {
    }
}
