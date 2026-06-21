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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            float dark = 1.0F - Math.min(progress * 4.0F, 1.0F);
            float add = random.nextFloat() * 0.2F;
            this.rCol = dark + add;
            this.gCol = 0.6F * dark + add;
            this.bCol = add * 0.45F;
            this.alpha = (float) Math.pow(1.0F - progress, 0.5D) * 0.75F;
            this.quadSize = baseScale * (0.5F + progress * 2.0F);
            this.setSpriteFromAge(sprites);
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
        Vector3f[] corners = new Vector3f[] {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z();
        float progress = (float) this.age / (float) this.lifetime;
        float dark = 1.0F - Math.min(progress * 4.0F, 1.0F);
        float alpha = (float) Math.pow(1.0F - Math.min(progress, 1.0F), 0.5D) * 0.75F;
        float spread = ((float) Math.pow(progress * 4.0F, 1.5D) + 1.0F) * this.baseScale;
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        Random legacyRandom = new Random(this.visualId);

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            float add = legacyRandom.nextFloat() * 0.3F;
            float red = Mth.clamp(dark + add, 0.0F, 1.0F);
            float green = Mth.clamp(0.6F * dark + add, 0.0F, 1.0F);
            float blue = Mth.clamp(add, 0.0F, 1.0F);
            float scale = (legacyRandom.nextFloat() * 0.5F + 0.1F + progress * 2.0F) * this.baseScale;
            float x = (float) (baseX + (legacyRandom.nextGaussian() - 1.0D) * 0.2F * spread);
            float y = (float) (baseY + (legacyRandom.nextGaussian() - 1.0D) * 0.5F * spread);
            float z = (float) (baseZ + (legacyRandom.nextGaussian() - 1.0D) * 0.2F * spread);
            renderQuad(consumer, rotation, corners, x, y, z, scale, red, green, blue, alpha, u0, u1, v0, v1);
        }
    }

    private static void renderQuad(VertexConsumer consumer, Quaternionf rotation, Vector3f[] corners,
            float x, float y, float z, float size, float red, float green, float blue, float alpha,
            float u0, float u1, float v0, float v1) {
        Vector3f corner0 = new Vector3f(corners[0]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner1 = new Vector3f(corners[1]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner2 = new Vector3f(corners[2]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner3 = new Vector3f(corners[3]).rotate(rotation).mul(size).add(x, y, z);
        consumer.vertex(corner0.x(), corner0.y(), corner0.z()).uv(u1, v1).color(red, green, blue, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corner1.x(), corner1.y(), corner1.z()).uv(u1, v0).color(red, green, blue, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corner2.x(), corner2.y(), corner2.z()).uv(u0, v0).color(red, green, blue, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corner3.x(), corner3.y(), corner3.z()).uv(u0, v1).color(red, green, blue, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
