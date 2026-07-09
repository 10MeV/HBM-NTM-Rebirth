package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.render.LegacyRenderRandom;
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

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class SmokePlumeParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_QUAD_COUNT = 6;
    private final SpriteSet sprites;
    private final int seed;
    private final float[] layerColor = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetX = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetY = new float[LEGACY_QUAD_COUNT];
    private final float[] layerOffsetZ = new float[LEGACY_QUAD_COUNT];
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    public SmokePlumeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.seed = NEXT_VISUAL_ID.incrementAndGet();
        this.lifetime = 80 + random.nextInt(20);
        this.quadSize = 0.25F;
        this.hasPhysics = true;
        this.friction = 0.925F;
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
            this.layerColor[i] = legacyRandom.nextFloat() * 0.75F + 0.1F;
            this.layerOffsetX[i] = (float) (legacyRandom.nextGaussian() * 0.5D);
            this.layerOffsetY[i] = (float) (legacyRandom.nextGaussian() * 0.5D);
            this.layerOffsetZ[i] = (float) (legacyRandom.nextGaussian() * 0.5D);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        float prevScale = this.quadSize;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0F - progress;
        this.quadSize = 0.25F + progress * 2.0F;
        this.move(this.xd, this.yd + (this.quadSize - prevScale), this.zd);
        if (this.onGround) {
            this.yd = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        }
        this.xd *= 0.925D;
        this.yd *= 0.925D;
        this.zd *= 0.925D;
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
        float scale = this.quadSize;
        var basis = HbmDeferredParticleRenderer.cameraBillboardBasis(camera, 1.0F);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float color = this.layerColor[i];
            float px = (float) baseX + this.layerOffsetX[i] * scale;
            float py = (float) baseY + this.layerOffsetY[i] * scale;
            float pz = (float) baseZ + this.layerOffsetZ[i] * scale;
            HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, basis[0], basis[1],
                    px, py, pz, scale, u0, u1, v0, v1, color, color, color, alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public SmokePlumeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new SmokePlumeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
