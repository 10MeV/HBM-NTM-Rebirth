package com.hbm.explosion;

import com.hbm.config.BombConfig;
import com.hbm.ntm.explosion.ExplosionNT.ExAttrib;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the small nuke helper.
 */
@Deprecated(forRemoval = false)
public class ExplosionNukeSmall {
    public static MukeParams PARAMS_SAFE = new MukeParams() {{
        safe = true;
        killRadius = 45.0F;
        radiationLevel = 2.0F;
    }};
    public static MukeParams PARAMS_TOTS = new MukeParams() {{
        blastRadius = 10.0F;
        killRadius = 30.0F;
        particle = "tinytot";
        shrapnelCount = 0;
        resolution = 32;
        radiationLevel = 1.0F;
    }};
    public static MukeParams PARAMS_LOW = new MukeParams() {{
        blastRadius = 15.0F;
        killRadius = 45.0F;
        radiationLevel = 2.0F;
    }};
    public static MukeParams PARAMS_MEDIUM = new MukeParams() {{
        blastRadius = 20.0F;
        killRadius = 55.0F;
        radiationLevel = 3.0F;
    }};
    public static MukeParams PARAMS_HIGH = new MukeParams() {{
        miniNuke = false;
        blastRadius = BombConfig.fatmanRadius();
        shrapnelCount = 0;
    }};

    public static void explode(Level level, double posX, double posY, double posZ, MukeParams params) {
        com.hbm.ntm.explosion.ExplosionNukeSmall.explode(level, posX, posY, posZ, params);
    }

    public static MukeParams configuredHighParams() {
        return PARAMS_HIGH.copy().blastRadius(BombConfig.fatmanRadius());
    }

    public static void explodeConfiguredHigh(Level level, double posX, double posY, double posZ) {
        explode(level, posX, posY, posZ, configuredHighParams());
    }

    public static class MukeParams extends com.hbm.ntm.explosion.ExplosionNukeSmall.MukeParams {
        @Override
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
            copy.explosionAttribs = java.util.Arrays.copyOf(explosionAttribs, explosionAttribs.length);
            return copy;
        }

        @Override
        public MukeParams largeNuke() {
            super.largeNuke();
            return this;
        }

        @Override
        public MukeParams safe() {
            super.safe();
            return this;
        }

        @Override
        public MukeParams blastRadius(float blastRadius) {
            super.blastRadius(blastRadius);
            return this;
        }

        @Override
        public MukeParams killRadius(float killRadius) {
            super.killRadius(killRadius);
            return this;
        }

        @Override
        public MukeParams radiationLevel(float radiationLevel) {
            super.radiationLevel(radiationLevel);
            return this;
        }

        @Override
        public MukeParams particle(String particle) {
            super.particle(particle);
            return this;
        }

        @Override
        public MukeParams shrapnelCount(int shrapnelCount) {
            super.shrapnelCount(shrapnelCount);
            return this;
        }

        @Override
        public MukeParams resolution(int resolution) {
            super.resolution(resolution);
            return this;
        }

        @Override
        public MukeParams explosionAttribs(ExAttrib... explosionAttribs) {
            super.explosionAttribs(explosionAttribs);
            return this;
        }
    }
}
