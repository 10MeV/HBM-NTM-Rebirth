package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class LegacyContrailParticle extends TextureSheetParticle {
    private static final AtomicInteger NEXT_VISUAL_ID = new AtomicInteger();
    private static final int LEGACY_QUAD_COUNT = 6;
    private static SpriteSet contrailSprites;
    private final SpriteSet sprites;
    private final int seed;
    private final float baseRed;
    private final float baseGreen;
    private final float baseBlue;
    private final float baseScale;

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
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (this.alpha <= 0.0F) {
            return;
        }
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
            float mod = legacyRandom.nextFloat() * 0.2F + 0.2F;
            float scale = (this.alpha + 0.5F) * this.baseScale;
            float px = (float) (baseX + legacyRandom.nextGaussian() * 0.5D * this.baseScale);
            float py = (float) (baseY + legacyRandom.nextGaussian() * 0.5D * this.baseScale);
            float pz = (float) (baseZ + legacyRandom.nextGaussian() * 0.5D * this.baseScale);
            renderQuad(consumer, rotation, corners, px, py, pz, scale,
                    clampColor(this.baseRed + mod), clampColor(this.baseGreen + mod), clampColor(this.baseBlue + mod),
                    this.alpha, u0, u1, v0, v1);
        }
    }

    private static float clampColor(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }

    private void renderQuad(VertexConsumer consumer, Quaternionf rotation, Vector3f[] corners, float x, float y, float z,
            float size, float red, float green, float blue, float alpha, float u0, float u1, float v0, float v1) {
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
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
