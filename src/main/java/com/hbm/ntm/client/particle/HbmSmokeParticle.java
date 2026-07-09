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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class HbmSmokeParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_EX_QUAD_COUNT = 6;
    private static SpriteSet exSmokeSprites;
    protected final SpriteSet sprites;
    protected final float baseScale;
    private final int visualId;
    private final boolean legacyExSmoke;
    private final float[] legacyLayerColor;
    private final float[] legacyLayerScale;
    private final float[] legacyLayerOffsetX;
    private final float[] legacyLayerOffsetY;
    private final float[] legacyLayerOffsetZ;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    protected HbmSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float baseScale, int lifetime) {
        this(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, baseScale, lifetime, false);
    }

    protected HbmSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites, float baseScale, int lifetime, boolean legacyExSmoke) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.baseScale = baseScale;
        this.visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.legacyExSmoke = legacyExSmoke;
        this.legacyLayerColor = legacyExSmoke ? new float[LEGACY_EX_QUAD_COUNT] : null;
        this.legacyLayerScale = legacyExSmoke ? new float[LEGACY_EX_QUAD_COUNT] : null;
        this.legacyLayerOffsetX = legacyExSmoke ? new float[LEGACY_EX_QUAD_COUNT] : null;
        this.legacyLayerOffsetY = legacyExSmoke ? new float[LEGACY_EX_QUAD_COUNT] : null;
        this.legacyLayerOffsetZ = legacyExSmoke ? new float[LEGACY_EX_QUAD_COUNT] : null;
        this.quadSize = baseScale;
        this.lifetime = lifetime;
        this.friction = 0.76F;
        this.hasPhysics = false;
        this.rCol = this.gCol = this.bCol = 0.35F + random.nextFloat() * 0.25F;
        this.alpha = 0.95F;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
        if (legacyExSmoke) {
            this.precomputeLegacyExLayers();
        }
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private void precomputeLegacyExLayers() {
        Random legacyRandom = LegacyRenderRandom.seeded(this.visualId);
        for (int i = 0; i < LEGACY_EX_QUAD_COUNT; i++) {
            this.legacyLayerColor[i] = legacyRandom.nextFloat() * 0.25F + 0.25F;
            this.legacyLayerScale[i] = (legacyRandom.nextFloat() + 0.5F) * this.baseScale;
            this.legacyLayerOffsetX[i] = (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.75D * this.baseScale);
            this.legacyLayerOffsetY[i] = (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.75D * this.baseScale);
            this.legacyLayerOffsetZ[i] = (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.75D * this.baseScale);
        }
    }

    public static SpriteSet exSmokeSprites() {
        return exSmokeSprites;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            this.alpha = 1.0F - progress;
            if (!this.legacyExSmoke) {
                this.quadSize = baseScale * (0.7F + progress * 1.6F);
            }
            this.setSpriteFromAge(sprites);
            this.cacheSpriteUv();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetDepthWriteConsumer(buffer);
        if (!this.legacyExSmoke) {
            HbmDeferredParticleRenderer.emitTextureSheetParticleQuad(consumer, camera, partialTick,
                    this.xo, this.yo, this.zo, this.x, this.y, this.z,
                    this.oRoll, this.roll, this.getQuadSize(partialTick),
                    this.cachedU0, this.cachedU1, this.cachedV0, this.cachedV1,
                    this.rCol, this.gCol, this.bCol, this.alpha, this.getLightColor(partialTick));
            return;
        }
        if (this.alpha <= 0.0F) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        float u0 = this.cachedU0;
        float u1 = this.cachedU1;
        float v0 = this.cachedV0;
        float v1 = this.cachedV1;
        float[] layerColor = this.legacyLayerColor;
        float[] layerScale = this.legacyLayerScale;
        float[] layerOffsetX = this.legacyLayerOffsetX;
        float[] layerOffsetY = this.legacyLayerOffsetY;
        float[] layerOffsetZ = this.legacyLayerOffsetZ;
        if (layerColor == null || layerScale == null || layerOffsetX == null || layerOffsetY == null || layerOffsetZ == null) {
            return;
        }
        var basis = HbmDeferredParticleRenderer.cameraBillboardBasis(camera, 1.0F);
        for (int i = 0; i < LEGACY_EX_QUAD_COUNT; i++) {
            float color = layerColor[i];
            float scale = layerScale[i];
            float x = (float) baseX + layerOffsetX[i];
            float y = (float) baseY + layerOffsetY[i];
            float z = (float) baseZ + layerOffsetZ[i];
            HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, basis[0], basis[1],
                    x, y, z, scale, u0, u1, v0, v1, color, color, color, this.alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    public static class ExSmokeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public ExSmokeProvider(SpriteSet sprites) {
            this.sprites = sprites;
            exSmokeSprites = sprites;
        }

        @Override
        public HbmSmokeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new HbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 1.0F, 100 + level.random.nextInt(40), true);
        }
    }

    public static class ContrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public ContrailProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public HbmSmokeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            HbmSmokeParticle particle = new HbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 1.25F, 100 + level.random.nextInt(40));
            particle.friction = 0.96F;
            return particle;
        }
    }

    public static class LaunchSmokeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public LaunchSmokeProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public HbmSmokeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            HbmSmokeParticle particle = new HbmSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 0.35F, 80 + level.random.nextInt(20));
            particle.friction = 0.925F;
            return particle;
        }
    }
}
