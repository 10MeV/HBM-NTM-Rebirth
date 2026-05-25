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
public class FluidFillParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;

    private FluidFillParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, int color) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        if (color >= 0) {
            this.rCol = ((color >> 16) & 255) / 255.0F;
            this.gCol = ((color >> 8) & 255) / 255.0F;
            this.bCol = (color & 255) / 255.0F;
        }
        this.quadSize = 0.2F;
        this.lifetime = 16 + this.random.nextInt(8);
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
    }

    public static FluidFillParticle create(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int color) {
        return sharedSprites == null ? null : new FluidFillParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sharedSprites, color);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public FluidFillParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new FluidFillParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, -1);
        }
    }
}
