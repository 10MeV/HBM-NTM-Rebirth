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
public class TownAuraParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseScale;

    private TownAuraParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.baseScale = 0.55F + random.nextFloat() * 0.35F;
        float color = 0.5F + random.nextFloat() * 0.5F;
        this.rCol = 0.8F * color;
        this.gCol = 0.9F * color;
        this.bCol = color;
        this.quadSize = baseScale;
        this.lifetime = 35 + random.nextInt(20);
        this.friction = 0.96F;
        this.gravity = -0.01F;
        this.hasPhysics = false;
        this.alpha = 0.55F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            this.alpha = 0.55F * (1.0F - progress);
            this.quadSize = baseScale * (1.0F + progress * 0.75F);
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public TownAuraParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new TownAuraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
