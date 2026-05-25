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
public class LegacySplashParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;

    private LegacySplashParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, int color) {
        super(level, x, y, z);
        this.sprites = sprites;
        if (color >= 0) {
            float shade = 1.0F - level.random.nextFloat() * 0.2F;
            this.rCol = ((color >> 16) & 255) / 255.0F * shade;
            this.gCol = ((color >> 8) & 255) / 255.0F * shade;
            this.bCol = (color & 255) / 255.0F * shade;
        } else {
            this.rCol = this.gCol = this.bCol = 1.0F - level.random.nextFloat() * 0.2F;
        }
        this.alpha = 0.5F;
        this.quadSize = 0.4F;
        this.lifetime = 200 + level.random.nextInt(50);
        this.gravity = 0.4F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    public static LegacySplashParticle create(ClientLevel level, double x, double y, double z, int color) {
        return sharedSprites == null ? null : new LegacySplashParticle(level, x, y, z, sharedSprites, color);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        if (!this.onGround) {
            this.xd += this.random.nextGaussian() * 0.002D;
            this.zd += this.random.nextGaussian() * 0.002D;
            if (this.yd < -0.5D) {
                this.yd = -0.5D;
            }
            this.yd -= 0.04D * this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98D;
            this.yd *= 0.98D;
            this.zd *= 0.98D;
        } else {
            this.remove();
        }
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
            sharedSprites = sprites;
        }

        @Override
        public LegacySplashParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return create(level, x, y, z, -1);
        }
    }
}
