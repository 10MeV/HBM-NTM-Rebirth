package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class ChaosCloudParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    public enum Mode {
        ORANGE,
        GREEN,
        PINK
    }

    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_QUAD_COUNT = 5;
    private static final float LEGACY_RENDER_SCALE = 3.75F;
    private static final long LEGACY_OFFSET_RANDOM_SEED = 100L;
    private static final float[] LAYER_OFFSET_X = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_OFFSET_Y = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_OFFSET_Z = new float[LEGACY_QUAD_COUNT];
    private static final float[] LAYER_SIZE = new float[LEGACY_QUAD_COUNT];

    static {
        precomputeStaticOffsetLayers();
    }

    private final SpriteSet sprites;
    private final Mode mode;
    private final int visualSeed;
    private final float[] layerShade = new float[LEGACY_QUAD_COUNT];
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private ChaosCloudParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, Mode mode) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.mode = mode;
        this.visualSeed = NEXT_VISUAL_ID.incrementAndGet();
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = 900 + this.random.nextInt(301);
        this.hasPhysics = false;
        this.quadSize = 1.0F;
        this.alpha = 1.0F;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
        this.precomputeShadeLayers();
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
    }

    private static void precomputeStaticOffsetLayers() {
        Random offsetRandom = new Random(LEGACY_OFFSET_RANDOM_SEED);
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            LAYER_OFFSET_X[i] = (float) ((offsetRandom.nextGaussian() - 1.0D) * 0.15D);
            LAYER_OFFSET_Y[i] = (float) ((offsetRandom.nextGaussian() - 1.0D) * 0.15D);
            LAYER_OFFSET_Z[i] = (float) ((offsetRandom.nextGaussian() - 1.0D) * 0.15D);
            LAYER_SIZE[i] = (float) (offsetRandom.nextDouble() * 0.5D + 0.25D) * LEGACY_RENDER_SCALE;
        }
    }

    private void precomputeShadeLayers() {
        Random shadeRandom = new Random(this.visualSeed);
        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            this.layerShade[i] = 1.0F - shadeRandom.nextInt(10) * 0.05F;
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

        if (this.mode == Mode.ORANGE) {
            this.xd *= 0.86D;
            this.yd = this.yd * 0.86D - 0.1D;
            this.zd *= 0.86D;
            moveOrange();
        } else {
            this.xd *= 0.7599999785423279D;
            this.yd *= 0.7599999785423279D;
            this.zd *= 0.7599999785423279D;
            if (this.onGround) {
                this.xd *= 0.699999988079071D;
                this.zd *= 0.699999988079071D;
            }
            if (this.level.isRainingAt(this.blockSamplePos.set(Mth.floor(this.x), Mth.floor(this.y),
                    Mth.floor(this.z)))) {
                this.yd -= 0.01D;
            }
            moveGreenOrPink();
        }
        this.setSpriteFromAge(this.sprites);
        this.cacheSpriteUv();
    }

    private void moveGreenOrPink() {
        double stepX = this.xd / 4.0D;
        double stepY = this.yd / 4.0D;
        double stepZ = this.zd / 4.0D;
        for (int i = 0; i < 4; i++) {
            this.x += stepX;
            this.y += stepY;
            this.z += stepZ;
            BlockPos.MutableBlockPos pos = this.blockSamplePos.set(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
            BlockState state = this.level.getBlockState(pos);
            if (state.isCollisionShapeFullBlock(this.level, pos)) {
                if (this.mode == Mode.PINK && this.random.nextInt(5) != 0) {
                    this.remove();
                    return;
                }
                this.x -= stepX;
                this.y -= stepY;
                this.z -= stepZ;
                this.xd = 0.0D;
                this.yd = 0.0D;
                this.zd = 0.0D;
                return;
            }
        }
    }

    private void moveOrange() {
        double stepX = this.xd / 4.0D;
        double stepY = this.yd / 4.0D;
        double stepZ = this.zd / 4.0D;
        for (int i = 0; i < 4; i++) {
            this.x += stepX;
            this.y += stepY;
            this.z += stepZ;
            if (!this.level.getBlockState(this.blockSamplePos.set(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z)))
                    .isAir()) {
                this.remove();
                return;
            }
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        HbmDeferredParticleRenderer.enqueue(this, camera, this.x, this.y, this.z);
    }

    @Override
    public void renderDeferred(MultiBufferSource.BufferSource buffer, Camera camera, float partialTick) {
        VertexConsumer consumer = HbmDeferredParticleRenderer.particleSheetDepthWriteConsumer(buffer);
        Quaternionf rotation = camera.rotation();
        var cameraPos = camera.getPosition();
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float px = (float) baseX + LAYER_OFFSET_X[i];
            float py = (float) baseY + LAYER_OFFSET_Y[i];
            float pz = (float) baseZ + LAYER_OFFSET_Z[i];
            renderQuad(consumer, rotation, px, py, pz, LAYER_SIZE[i], this.layerShade[i],
                    this.cachedU0, this.cachedU1, this.cachedV0, this.cachedV1);
        }
    }

    private static void renderQuad(VertexConsumer consumer, Quaternionf rotation,
            float x, float y, float z, float size, float shade, float u0, float u1, float v0, float v1) {
        HbmDeferredParticleRenderer.emitLocalParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT, rotation, x, y, z,
                -0.5F * size, -0.25F * size, 0.0F, u0, v1,
                0.5F * size, -0.25F * size, 0.0F, u1, v1,
                0.5F * size, 0.75F * size, 0.0F, u1, v0,
                -0.5F * size, 0.75F * size, 0.0F, u0, v0,
                shade, shade, shade, 1.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return HbmDeferredParticleRenderer.DEFERRED_RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Mode mode;

        public Provider(SpriteSet sprites, Mode mode) {
            this.sprites = sprites;
            this.mode = mode;
        }

        @Override
        public ChaosCloudParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new ChaosCloudParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.mode);
        }
    }
}
