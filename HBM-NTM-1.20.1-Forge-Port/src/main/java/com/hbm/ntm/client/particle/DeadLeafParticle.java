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
public class DeadLeafParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private DeadLeafParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        float color = 1.0F - this.random.nextFloat() * 0.2F;
        this.rCol = color;
        this.gCol = color;
        this.bCol = color;
        this.quadSize = 0.1F;
        this.lifetime = 200 + this.random.nextInt(50);
        this.gravity = 0.2F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            if (!this.onGround) {
                this.xd += this.random.nextGaussian() * 0.002D;
                this.zd += this.random.nextGaussian() * 0.002D;
                if (this.yd < -0.025D) {
                    this.yd = -0.025D;
                }
            }
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
        public DeadLeafParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new DeadLeafParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
