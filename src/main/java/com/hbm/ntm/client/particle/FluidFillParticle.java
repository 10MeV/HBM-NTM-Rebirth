package com.hbm.ntm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FluidFillParticle extends TextureSheetParticle {
    private static SpriteSet sharedSprites;

    private FluidFillParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, int color) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.friction = 0.7F;
        this.gravity = 0.5F;
        this.xd *= 0.1D;
        this.yd *= 0.1D;
        this.zd *= 0.1D;
        this.xd += xSpeed * 0.4D;
        this.yd += ySpeed * 0.4D;
        this.zd += zSpeed * 0.4D;
        float shade = (float) (Math.random() * 0.3D + 0.6D);
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;
        this.quadSize *= 0.75F;
        this.lifetime = Math.max((int) (6.0D / (Math.random() * 0.8D + 0.6D)), 1);
        this.hasPhysics = false;
        this.tick();
        this.pickSprite(sprites);
        if (color >= 0) {
            this.rCol = ((color >> 16) & 255) / 255.0F;
            this.gCol = ((color >> 8) & 255) / 255.0F;
            this.bCol = (color & 255) / 255.0F;
        }
    }

    public static FluidFillParticle create(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int color) {
        return sharedSprites == null ? null : new FluidFillParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sharedSprites, color);
    }

    @Override
    public void tick() {
        super.tick();
        this.gCol *= 0.96F;
        this.bCol *= 0.9F;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * Mth.clamp(((float) this.age + partialTick) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
