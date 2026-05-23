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
public class SchrabFogParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private SchrabFogParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.lifetime = 16 + this.random.nextInt(10);
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        float tint = 0.85F + this.random.nextFloat() * 0.15F;
        this.rCol = 0.0F;
        this.gCol = tint;
        this.bCol = tint;
        this.alpha = 1.0F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            this.alpha = 1.0F - progress;
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
        public SchrabFogParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            SchrabFogParticle particle = new SchrabFogParticle(level, x, y, z, sprites);
            particle.xd += xSpeed * 0.1D;
            particle.yd += ySpeed * 0.1D;
            particle.zd += zSpeed * 0.1D;
            return particle;
        }
    }
}
