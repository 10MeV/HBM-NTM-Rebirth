package com.hbm.ntm.client.particle;

import com.hbm.ntm.world.WorldUtil;
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

@OnlyIn(Dist.CLIENT)
public class HazeParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final int LEGACY_QUAD_COUNT = 25;
    private static final long LEGACY_RANDOM_SEED = 50L;
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

    private static SpriteSet sharedSprites;
    private final SpriteSet sprites;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private HazeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = 600 + this.random.nextInt(100);
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.quadSize = 10.0F;
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
        Random fixed = new Random(LEGACY_RANDOM_SEED);
        float cumulativeX = 0.0F;
        float cumulativeY = 0.0F;
        float cumulativeZ = 0.0F;
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            cumulativeX += (float) (fixed.nextGaussian() * 2.5D);
            cumulativeY += (float) (fixed.nextGaussian() * 0.15D);
            cumulativeZ += (float) (fixed.nextGaussian() * 2.5D);
            LAYER_OFFSET_X[i] = cumulativeX;
            LAYER_OFFSET_Y[i] = cumulativeY;
            LAYER_OFFSET_Z[i] = cumulativeZ;
            LAYER_SIZE_FACTOR[i] = (float) (fixed.nextDouble() * 0.25D + 0.75D);
            LAYER_JITTER_X[i] = (float) (fixed.nextGaussian() * 0.5D);
            LAYER_JITTER_Y[i] = (float) (fixed.nextGaussian() * 0.5D);
            LAYER_JITTER_Z[i] = (float) (fixed.nextGaussian() * 0.5D);
        }
    }

    public static HazeParticle create(ClientLevel level, double x, double y, double z) {
        return sharedSprites == null ? null : new HazeParticle(level, x, y, z, sharedSprites);
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
        this.xd *= 0.96D;
        this.yd *= 0.96D;
        this.zd *= 0.96D;
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
        this.move(this.xd, this.yd, this.zd);
        int x = Mth.floor(this.x) + this.random.nextInt(15) - 7;
        int z = Mth.floor(this.z) + this.random.nextInt(15) - 7;
        int y = WorldUtil.legacyGetHeightValue(this.level, x, z);
        this.level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                x + this.random.nextDouble(), y + 0.1D, z + this.random.nextDouble(), 0.0D, 0.0D, 0.0D);
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        float alpha = (float) Math.sin(this.age * Math.PI / 400.0D) * 0.025F;
        if (alpha <= 0.0F) {
            return;
        }
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetDepthWriteConsumer(buffer);
        Vec3 cameraPos = camera.getPosition();
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        var basis = HbmDeferredParticleRenderer.cameraBillboardBasis(camera, 1.0F);
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float size = LAYER_SIZE_FACTOR[i] * this.quadSize;
            float x = (float) baseX + LAYER_OFFSET_X[i] + LAYER_JITTER_X[i];
            float y = (float) baseY + LAYER_OFFSET_Y[i] + LAYER_JITTER_Y[i];
            float z = (float) baseZ + LAYER_OFFSET_Z[i] + LAYER_JITTER_Z[i];
            HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, basis[0], basis[1],
                    x, y, z, size, this.cachedU0, this.cachedU1, this.cachedV0, this.cachedV1,
                    this.rCol, this.gCol, this.bCol, alpha);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        public Provider(SpriteSet sprites) {
            sharedSprites = sprites;
        }

        @Override
        public HazeParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return create(level, x, y, z);
        }
    }
}
