package com.hbm.ntm.explosion;

import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.explosion.ExplosionNT.ExAttrib;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public final class ExplosionNukeSmall {
    public static final MukeParams PARAMS_SAFE = params().safe().killRadius(45.0F).radiationLevel(2.0F);
    public static final MukeParams PARAMS_TOTS = params().blastRadius(10.0F).killRadius(30.0F).particle("tinytot")
            .shrapnelCount(0).resolution(32).radiationLevel(1.0F);
    public static final MukeParams PARAMS_LOW = params().blastRadius(15.0F).killRadius(45.0F).radiationLevel(2.0F);
    public static final MukeParams PARAMS_MEDIUM = params().blastRadius(20.0F).killRadius(55.0F).radiationLevel(3.0F);
    public static final MukeParams PARAMS_HIGH = params().largeNuke().blastRadius(BombConfig.FATMAN_RADIUS_DEFAULT)
            .killRadius(75.0F).shrapnelCount(0).radiationLevel(5.0F);

    public static void explode(Level level, double x, double y, double z, MukeParams params) {
        if (level == null || params == null || level.isClientSide()) {
            return;
        }

        if (params.particle != null) {
            ParticleUtil.spawnNuclearBurstVisual(level, x, y + 0.5D, z, params.particle,
                    ParticleUtil.TYPE_MUKE.equals(params.particle) && level.random.nextInt(100) == 0);
        }

        level.playSound(null, x, y, z, ModSounds.WEAPON_MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

        if (params.shrapnelCount > 0) {
            ExplosionLarge.spawnShrapnels(level, x, y, z, params.shrapnelCount);
        }
        if (params.miniNuke && !params.safe) {
            new ExplosionNT(level, x, y, z, params.blastRadius)
                    .addAttrib(params.explosionAttribs)
                    .overrideResolution(params.resolution)
                    .explode();
        }
        if (params.killRadius > 0.0F) {
            ExplosionNukeGeneric.dealDamage(level, x, y, z, params.killRadius);
        }
        if (params.miniNuke) {
            irradiateMiniNukeFootprint(level, x, y, z, params.radiationLevel);
        } else {
            ExplosionLarge.spawnParticles(level, x, y, z, ExplosionLarge.cloudFunction(Math.round(params.blastRadius)));
            NuclearExplosionUtil.spawnNuclear(level, Math.round(params.blastRadius), x, y, z);
        }
    }

    private static void irradiateMiniNukeFootprint(Level level, double x, double y, double z, float radiationLevel) {
        float radMod = radiationLevel / 3.0F;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int distance = Math.abs(i) + Math.abs(j);
                if (distance < 4) {
                    float amount = 50.0F / (distance + 1.0F) * radMod;
                    ChunkRadiationManager.incrementRadiation(level,
                            BlockPos.containing(Math.floor(x + i * 16.0D), Math.floor(y), Math.floor(z + j * 16.0D)),
                            amount);
                }
            }
        }
    }

    public static MukeParams configuredHighParams() {
        return PARAMS_HIGH.copy().blastRadius(BombConfig.FATMAN_RADIUS.get());
    }

    public static void explodeConfiguredHigh(Level level, double x, double y, double z) {
        explode(level, x, y, z, configuredHighParams());
    }

    private static MukeParams params() {
        return new MukeParams();
    }

    public static class MukeParams {
        public boolean miniNuke = true;
        public boolean safe = false;
        public float blastRadius;
        public float killRadius;
        public float radiationLevel = 1.0F;
        public String particle = "muke";
        public int shrapnelCount = 25;
        public int resolution = 64;
        public ExAttrib[] explosionAttribs = new ExAttrib[] {
                ExAttrib.FIRE,
                ExAttrib.NOPARTICLE,
                ExAttrib.NOSOUND,
                ExAttrib.NODROP,
                ExAttrib.NOHURT
        };

        public MukeParams copy() {
            MukeParams copy = new MukeParams();
            copy.miniNuke = miniNuke;
            copy.safe = safe;
            copy.blastRadius = blastRadius;
            copy.killRadius = killRadius;
            copy.radiationLevel = radiationLevel;
            copy.particle = particle;
            copy.shrapnelCount = shrapnelCount;
            copy.resolution = resolution;
            copy.explosionAttribs = Arrays.copyOf(explosionAttribs, explosionAttribs.length);
            return copy;
        }

        public MukeParams largeNuke() {
            this.miniNuke = false;
            return this;
        }

        public MukeParams safe() {
            this.safe = true;
            return this;
        }

        public MukeParams blastRadius(float blastRadius) {
            this.blastRadius = blastRadius;
            return this;
        }

        public MukeParams killRadius(float killRadius) {
            this.killRadius = killRadius;
            return this;
        }

        public MukeParams radiationLevel(float radiationLevel) {
            this.radiationLevel = radiationLevel;
            return this;
        }

        public MukeParams particle(String particle) {
            this.particle = particle;
            return this;
        }

        public MukeParams shrapnelCount(int shrapnelCount) {
            this.shrapnelCount = shrapnelCount;
            return this;
        }

        public MukeParams resolution(int resolution) {
            this.resolution = resolution;
            return this;
        }

        public MukeParams explosionAttribs(ExAttrib... explosionAttribs) {
            this.explosionAttribs = Arrays.copyOf(explosionAttribs, explosionAttribs.length);
            return this;
        }
    }

    private ExplosionNukeSmall() {
    }
}
