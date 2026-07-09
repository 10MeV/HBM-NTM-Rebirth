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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class DeadLeafParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private final SpriteSet sprites;
    private final boolean flipU;
    private final boolean flipV;
    private float cachedU0;
    private float cachedU1;
    private float cachedV0;
    private float cachedV1;

    private DeadLeafParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        int visualId = NEXT_VISUAL_ID.incrementAndGet();
        this.flipU = visualId % 2 == 0;
        this.flipV = visualId % 4 < 2;
        float color = 1.0F - this.random.nextFloat() * 0.2F;
        this.rCol = color;
        this.gCol = color;
        this.bCol = color;
        this.quadSize = 0.1F;
        this.lifetime = 200 + this.random.nextInt(50);
        this.gravity = 0.2F;
        this.hasPhysics = true;
        this.setSpriteFromAge(sprites);
        this.cacheSpriteUv();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            if (!this.onGround) {
                this.xd += this.random.nextGaussian() * 0.002D;
                this.zd += this.random.nextGaussian() * 0.002D;
                if (this.yd < -0.025D) {
                    this.yd = -0.025D;
                }
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
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        float scale = this.getQuadSize(partialTick);
        float minU = this.flipU ? this.cachedU1 : this.cachedU0;
        float maxU = this.flipU ? this.cachedU0 : this.cachedU1;
        float minV = this.flipV ? this.cachedV1 : this.cachedV0;
        float maxV = this.flipV ? this.cachedV0 : this.cachedV1;
        int light = this.getLightColor(partialTick);
        HbmDeferredParticleRenderer.emitCameraUnitParticleSheetQuad(consumer, camera, light,
                x, y, z, scale, minU, maxU, minV, maxV,
                this.rCol, this.gCol, this.bCol, this.alpha);
    }

    private void cacheSpriteUv() {
        this.cachedU0 = this.getU0();
        this.cachedU1 = this.getU1();
        this.cachedV0 = this.getV0();
        this.cachedV1 = this.getV1();
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
        public DeadLeafParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new DeadLeafParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
