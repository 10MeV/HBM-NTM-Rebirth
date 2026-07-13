package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RadiationFogParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final int LEGACY_QUAD_COUNT = 25;
    private static final long LEGACY_RANDOM_SEED = 50L;
    private static final float LEGACY_SCALE = 7.5F;
    private static final float LEGACY_ALPHA = 0.125F;
    private static final float LEGACY_RED = 0.85F;
    private static final float LEGACY_GREEN = 0.9F;
    private static final float LEGACY_BLUE = 0.5F;
    private static final float[] LAYER_OFFSET_X = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_OFFSET_Y = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_OFFSET_Z = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_JITTER_X = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_JITTER_Y = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_JITTER_Z = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_SIZE_FACTOR = new float[LEGACY_QUAD_COUNT];

    static {
        precomputeStaticRenderLayers();
    }

    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private RadiationFogParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.lifetime = 400;
        this.quadSize = LEGACY_SCALE;
        this.rCol = LEGACY_RED;
        this.gCol = LEGACY_GREEN;
        this.bCol = LEGACY_BLUE;
        this.alpha = 0.0F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private static void precomputeStaticRenderLayers() {
        Random legacyRandom = new Random(LEGACY_RANDOM_SEED);
        float cumulativeX = 0.0F;
        float cumulativeY = 0.0F;
        float cumulativeZ = 0.0F;
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            cumulativeX += (float) ((legacyRandom.nextGaussian() - 1.0D) * 2.5D);
            cumulativeY += (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.15D);
            cumulativeZ += (float) ((legacyRandom.nextGaussian() - 1.0D) * 2.5D);
            LAYER_OFFSET_X[i] = cumulativeX;
            LAYER_OFFSET_Y[i] = cumulativeY;
            LAYER_OFFSET_Z[i] = cumulativeZ;
            LAYER_SIZE_FACTOR[i] = (float) legacyRandom.nextDouble();
            LAYER_JITTER_X[i] = (float) (legacyRandom.nextGaussian() * 0.5D);
            LAYER_JITTER_Y[i] = (float) (legacyRandom.nextGaussian() * 0.5D);
            LAYER_JITTER_Z[i] = (float) (legacyRandom.nextGaussian() * 0.5D);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }
        this.xd *= 0.9599999785423279D;
        this.yd *= 0.9599999785423279D;
        this.zd *= 0.9599999785423279D;
        this.alpha = (float) Math.sin(this.age * Math.PI / 400.0D) * LEGACY_ALPHA;
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

        int light = getLightColor(partialTick);
        var cameraPos = camera.getPosition();
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        var basis = HbmDeferredParticleRenderer.cameraBillboardBasis(camera, 1.0F);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float size = LAYER_SIZE_FACTOR[i] * this.quadSize;
            float x = (float) baseX + LAYER_OFFSET_X[i] + LAYER_JITTER_X[i];
            float y = (float) baseY + LAYER_OFFSET_Y[i] + LAYER_JITTER_Y[i];
            float z = (float) baseZ + LAYER_OFFSET_Z[i] + LAYER_JITTER_Z[i];
            HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, light, basis[0], basis[1],
                    x, y, z, size, this.cachedU0, this.cachedU1, this.cachedV0, this.cachedV1,
                    rCol, gCol, bCol, alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements net.minecraft.client.particle.ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public RadiationFogParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RadiationFogParticle(level, x, y, z, sprites);
        }
    }
}
