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
public class RocketFlameParticle extends TextureSheetParticle {
    private static SpriteSet rocketFlameSprites;
    private final SpriteSet sprites;
    private final float baseScale;

    public RocketFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float baseScale, int lifetime) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.baseScale = baseScale;
        this.quadSize = baseScale;
        this.lifetime = lifetime;
        this.friction = 0.91F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            float dark = 1.0F - Math.min(progress * 4.0F, 1.0F);
            float add = random.nextFloat() * 0.2F;
            this.rCol = dark + add;
            this.gCol = 0.6F * dark + add;
            this.bCol = add * 0.45F;
            this.alpha = (float) Math.pow(1.0F - progress, 0.5D) * 0.75F;
            this.quadSize = baseScale * (0.5F + progress * 2.0F);
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static RocketFlameParticle createLegacy(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, float scale, int lifetime) {
        if (rocketFlameSprites == null) {
            return null;
        }
        return new RocketFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, rocketFlameSprites, scale, lifetime);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            rocketFlameSprites = sprites;
        }

        @Override
        public RocketFlameParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new RocketFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 1.0F, 300 + level.random.nextInt(50));
        }
    }
}
