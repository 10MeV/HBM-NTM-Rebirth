package com.hbm.ntm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HbmSmokeParticle extends TextureSheetParticle {
    private static SpriteSet exSmokeSprites;
    protected final SpriteSet sprites;
    protected final float baseScale;

    protected HbmSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float baseScale, int lifetime) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.baseScale = baseScale;
        this.quadSize = baseScale;
        this.lifetime = lifetime;
        this.friction = 0.76F;
        this.hasPhysics = false;
        this.rCol = this.gCol = this.bCol = 0.35F + random.nextFloat() * 0.25F;
        this.alpha = 0.95F;
        this.setSpriteFromAge(sprites);
    }

    public static SpriteSet exSmokeSprites() {
        return exSmokeSprites;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            this.alpha = 1.0F - progress;
            this.quadSize = baseScale * (0.7F + progress * 1.6F);
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class ExSmokeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public ExSmokeProvider(SpriteSet sprites) {
            this.sprites = sprites;
            exSmokeSprites = sprites;
        }

        @Override
        public HbmSmokeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new HbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 1.0F, 100 + level.random.nextInt(40));
        }
    }

    public static class ContrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public ContrailProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public HbmSmokeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            HbmSmokeParticle particle = new HbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 1.25F, 100 + level.random.nextInt(40));
            particle.friction = 0.96F;
            return particle;
        }
    }

    public static class LaunchSmokeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public LaunchSmokeProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public HbmSmokeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            HbmSmokeParticle particle = new HbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 0.35F, 80 + level.random.nextInt(20));
            particle.friction = 0.925F;
            return particle;
        }
    }
}
