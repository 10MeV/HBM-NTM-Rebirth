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
public class FoamParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private FoamParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, Math.max(ySpeed, 1.5D + level.random.nextDouble()), zSpeed);
        this.sprites = sprites;
        this.lifetime = 60 + level.random.nextInt(60);
        this.quadSize = 0.3F + level.random.nextFloat() * 0.7F;
        this.gravity = 0.01F;
        this.friction = 0.96F;
        this.rCol = this.gCol = this.bCol = 0.95F;
        this.alpha = 0.8F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            this.alpha = 0.8F * (1.0F - progress * progress);
            this.quadSize *= progress < 0.35F ? 1.03F : 0.985F;
            this.xd += (random.nextDouble() - 0.5D) * 0.03D;
            this.zd += (random.nextDouble() - 0.5D) * 0.03D;
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
        public FoamParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new FoamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
