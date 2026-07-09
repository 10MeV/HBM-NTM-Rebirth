package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class RocketFlameParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_QUAD_COUNT = 10;
    private static SpriteSet rocketFlameSprites;
    private final SpriteSet sprites;
    private final int visualId;
    private final float baseScale;
    private final float[] layerAdd = new float[LEGACY_QUAD_COUNT];
    private final float[] layerScaleRand = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetX = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetY = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetZ = new float[LEGACY_QUAD_COUNT];
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;
    private float cachedDark;
    private float cachedProgressScale;
    private float cachedSpreadBase;
    private float cachedRenderAlpha;

    public RocketFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float baseScale, int lifetime) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.baseScale = baseScale;
        this.quadSize = baseScale;
        this.lifetime = lifetime;
        this.friction = 0.91F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
        this.precomputeRenderLayers();
        this.updateRenderCaches();
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private void precomputeRenderLayers() {
        Random legacyRandom = LegacyRenderRandom.seeded(this.visualId);
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            this.layerAdd[i] = legacyRandom.nextFloat() * 0.3F;
            this.layerScaleRand[i] = legacyRandom.nextFloat();
            this.layerOffsetX[i] = (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.2F);
            this.layerOffsetY[i] = (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.5F);
            this.layerOffsetZ[i] = (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.2F);
        }
    }

    private void updateRenderCaches() {
        float progress = (float) this.age / (float) this.lifetime;
        this.cachedDark = 1.0F - Math.min(progress * 4.0F, 1.0F);
        this.cachedProgressScale = progress * 2.0F;
        this.cachedRenderAlpha = (float) Math.pow(1.0F - Math.min(progress, 1.0F), 0.5D) * 0.75F;
        this.cachedSpreadBase = ((float) Math.pow(progress * 4.0F, 1.5D) + 1.0F) * this.baseScale;
        this.alpha = this.cachedRenderAlpha;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            float add = random.nextFloat() * 0.2F;
            this.quadSize = baseScale * (0.5F + progress * 2.0F);
            this.setSpriteFromAge(sprites);
            this.cacheSpriteUv();
            this.updateRenderCaches();
            this.rCol = this.cachedDark + add;
            this.gCol = 0.6F * this.cachedDark + add;
            this.bCol = add * 0.45F;
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetDepthWriteConsumer(buffer);
        var cameraPos = camera.getPosition();
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        float dark = this.cachedDark;
        float alpha = this.cachedRenderAlpha;
        float spread = this.cachedSpreadBase;
        float u0 = this.cachedU0;
        float u1 = this.cachedU1;
        float v0 = this.cachedV0;
        float v1 = this.cachedV1;
        var basis = HbmDeferredParticleRenderer.cameraBillboardBasis(camera, 1.0F);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float add = this.layerAdd[i];
            float red = Mth.clamp(dark + add, 0.0F, 1.0F);
            float green = Mth.clamp(0.6F * dark + add, 0.0F, 1.0F);
            float blue = Mth.clamp(add, 0.0F, 1.0F);
            float scale = (this.layerScaleRand[i] * 0.5F + 0.1F + this.cachedProgressScale) * this.baseScale;
            float x = (float) baseX + this.layerOffsetX[i] * spread;
            float y = (float) baseY + this.layerOffsetY[i] * spread;
            float z = (float) baseZ + this.layerOffsetZ[i] * spread;
            HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, basis[0], basis[1],
                    x, y, z, scale, u0, u1, v0, v1, red, green, blue, alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
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
