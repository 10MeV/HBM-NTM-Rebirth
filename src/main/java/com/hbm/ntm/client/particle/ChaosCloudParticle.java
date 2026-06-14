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
import org.joml.Vector3f;

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

    private final SpriteSet sprites;
    private final Mode mode;
    private final int visualSeed;

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
            if (this.level.isRainingAt(BlockPos.containing(this.x, this.y, this.z))) {
                this.yd -= 0.01D;
            }
            moveGreenOrPink();
        }
        this.setSpriteFromAge(this.sprites);
    }

    private void moveGreenOrPink() {
        double stepX = this.xd / 4.0D;
        double stepY = this.yd / 4.0D;
        double stepZ = this.zd / 4.0D;
        for (int i = 0; i < 4; i++) {
            this.x += stepX;
            this.y += stepY;
            this.z += stepZ;
            BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
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
            if (!this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).isAir()) {
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
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite());
        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-0.5F, -0.25F, 0.0F),
                new Vector3f(0.5F, -0.25F, 0.0F),
                new Vector3f(0.5F, 0.75F, 0.0F),
                new Vector3f(-0.5F, 0.75F, 0.0F)
        };
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z();
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        Random shadeRandom = new Random(this.visualSeed);
        Random offsetRandom = new Random(100L);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float shade = 1.0F - shadeRandom.nextInt(10) * 0.05F;
            float px = (float) (baseX + (offsetRandom.nextGaussian() - 1.0D) * 0.15D);
            float py = (float) (baseY + (offsetRandom.nextGaussian() - 1.0D) * 0.15D);
            float pz = (float) (baseZ + (offsetRandom.nextGaussian() - 1.0D) * 0.15D);
            float size = (float) (offsetRandom.nextDouble() * 0.5D + 0.25D) * LEGACY_RENDER_SCALE;
            renderQuad(consumer, rotation, corners, px, py, pz, size, shade, u0, u1, v0, v1);
        }
    }

    private static void renderQuad(VertexConsumer consumer, Quaternionf rotation, Vector3f[] corners,
            float x, float y, float z, float size, float shade, float u0, float u1, float v0, float v1) {
        Vector3f corner0 = new Vector3f(corners[0]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner1 = new Vector3f(corners[1]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner2 = new Vector3f(corners[2]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner3 = new Vector3f(corners[3]).rotate(rotation).mul(size).add(x, y, z);
        consumer.vertex(corner0.x(), corner0.y(), corner0.z()).uv(u0, v1).color(shade, shade, shade, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corner1.x(), corner1.y(), corner1.z()).uv(u1, v1).color(shade, shade, shade, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corner2.x(), corner2.y(), corner2.z()).uv(u1, v0).color(shade, shade, shade, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corner3.x(), corner3.y(), corner3.z()).uv(u0, v0).color(shade, shade, shade, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
