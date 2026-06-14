package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class GasFlameParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;
    private final float colorMod;
    private final float baseScale;

    public GasFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float scale) {
        super(level, x, y, z, xSpeed, ySpeed * 1.5D, zSpeed);
        this.sprites = sprites;
        this.colorMod = 0.8F + random.nextFloat() * 0.2F;
        this.baseScale = scale;
        this.quadSize = scale;
        this.lifetime = 30 + random.nextInt(13);
        this.hasPhysics = false;
        this.friction = 1.0F;
        updateColor();
        this.setSpriteFromAge(sprites);
    }

    public static SpriteSet sharedSprites() {
        return sharedSprites;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float smokeScale = Mth.clamp(((float) this.age + partialTick) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
        return this.baseScale * smokeScale;
    }

    @Override
    public void tick() {
        double prevMotionY = this.yd;
        super.tick();
        if (!this.removed) {
            this.yd = prevMotionY;
            this.xd *= 0.75D;
            this.yd += 0.005D;
            this.zd *= 0.75D;
            updateColor();
            this.setSpriteFromAge(sprites);
        }
    }

    private void updateColor() {
        float time = lifetime <= 0 ? 1.0F : (float) age / (float) lifetime;
        Color color = Color.getHSBColor(Math.max((60.0F - time * 100.0F) / 360.0F, 0.0F), 1.0F - time * 0.25F, 1.0F - time * 0.5F);
        this.rCol = color.getRed() / 255.0F * colorMod;
        this.gCol = color.getGreen() / 255.0F * colorMod;
        this.bCol = color.getBlue() / 255.0F * colorMod;
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        super.render(buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite()), camera, partialTick);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            sharedSprites = sprites;
        }

        @Override
        public GasFlameParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new GasFlameParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 6.5F);
        }
    }
}
