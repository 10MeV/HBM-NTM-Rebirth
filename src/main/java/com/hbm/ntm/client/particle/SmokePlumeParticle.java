package com.hbm.ntm.client.particle;

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
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class SmokePlumeParticle extends TextureSheetParticle implements HbmDeferredParticleRenderer.DeferredParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_QUAD_COUNT = 6;
    private final SpriteSet sprites;
    private final int seed;

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
        VertexConsumer consumer = buffer.getBuffer(HbmDeferredParticleRenderer.particleSheetDepthWrite());
        Quaternionf rotation = camera.rotation();
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z();
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        Random legacyRandom = new Random(seed);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float color = legacyRandom.nextFloat() * 0.75F + 0.1F;
            float scale = this.quadSize;
            float px = (float) (baseX + legacyRandom.nextGaussian() * 0.5D * scale);
            float py = (float) (baseY + legacyRandom.nextGaussian() * 0.5D * scale);
            float pz = (float) (baseZ + legacyRandom.nextGaussian() * 0.5D * scale);
            renderQuad(consumer, rotation, corners, px, py, pz, scale, color, u0, u1, v0, v1);
        }
    }

    private void renderQuad(VertexConsumer consumer, Quaternionf rotation, Vector3f[] corners,
            float x, float y, float z, float size, float color, float u0, float u1, float v0, float v1) {
        Vector3f corner0 = new Vector3f(corners[0]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner1 = new Vector3f(corners[1]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner2 = new Vector3f(corners[2]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner3 = new Vector3f(corners[3]).rotate(rotation).mul(size).add(x, y, z);
        HbmDeferredParticleRenderer.emitParticleSheetQuad(consumer, LightTexture.FULL_BRIGHT,
                corner0, u1, v1,
                corner1, u1, v0,
                corner2, u0, v0,
                corner3, u0, v1,
                color, color, color, alpha);
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
