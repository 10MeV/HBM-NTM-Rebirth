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
public class LegacyContrailParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_QUAD_COUNT = 6;
    private static SpriteSet contrailSprites;
    private final SpriteSet sprites;
    private final int seed;
    private final float baseRed;
    private final float baseGreen;
    private final float baseBlue;
    private final float baseScale;
    private final float[] layerRed = new float[LEGACY_QUAD_COUNT];
    private final float[] layerGreen = new float[LEGACY_QUAD_COUNT];
    private final float[] layerBlue = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetX = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetY = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetZ = new float[LEGACY_QUAD_COUNT];
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    public LegacyContrailParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float red, float green, float blue, float scale) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.seed = NEXT_VISUAL_ID.incrementAndGet();
        this.baseRed = red;
        this.baseGreen = green;
        this.baseBlue = blue;
        this.baseScale = scale;
        this.lifetime = 100 + random.nextInt(40);
        this.quadSize = scale;
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.alpha = 1.0F;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
        this.precomputeRenderLayers();
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private void precomputeRenderLayers() {
        Random legacyRandom = LegacyRenderRandom.seeded(seed);
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float mod = legacyRandom.nextFloat() * 0.2F + 0.2F;
            this.layerRed[i] = clampColor(this.baseRed + mod);
            this.layerGreen[i] = clampColor(this.baseGreen + mod);
            this.layerBlue[i] = clampColor(this.baseBlue + mod);
            this.layerOffsetX[i] = (float) (legacyRandom.nextGaussian() * 0.5D * this.baseScale);
            this.layerOffsetY[i] = (float) (legacyRandom.nextGaussian() * 0.5D * this.baseScale);
            this.layerOffsetZ[i] = (float) (legacyRandom.nextGaussian() * 0.5D * this.baseScale);
        }
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
        this.alpha = 1.0F - (float) this.age / (float) this.lifetime;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        if (this.alpha <= 0.0F) {
            return;
        }
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetDepthWriteConsumer(buffer);
        var cameraPos = camera.getPosition();
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        float u0 = this.cachedU0;
        float u1 = this.cachedU1;
        float v0 = this.cachedV0;
        float v1 = this.cachedV1;
        float scale = (this.alpha + 0.5F) * this.baseScale;
        var basis = HbmDeferredParticleRenderer.cameraBillboardBasis(camera, 1.0F);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float px = (float) baseX + this.layerOffsetX[i];
            float py = (float) baseY + this.layerOffsetY[i];
            float pz = (float) baseZ + this.layerOffsetZ[i];
            HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, basis[0], basis[1],
                    px, py, pz, scale, u0, u1, v0, v1,
                    this.layerRed[i], this.layerGreen[i], this.layerBlue[i], this.alpha);
        }
    }

    private static float clampColor(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static LegacyContrailParticle create(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, float red, float green, float blue, float scale) {
        if (contrailSprites == null) {
            return null;
        }
        return new LegacyContrailParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, contrailSprites, red, green, blue, scale);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
            contrailSprites = sprites;
        }

        @Override
        public LegacyContrailParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new LegacyContrailParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 0.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
