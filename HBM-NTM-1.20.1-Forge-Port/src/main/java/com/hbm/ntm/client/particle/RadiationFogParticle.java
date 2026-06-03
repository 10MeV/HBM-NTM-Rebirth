package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RadiationFogParticle extends TextureSheetParticle {
    private static final int LEGACY_QUAD_COUNT = 25;
    private static final int LEGACY_RANDOM_SEED = 50;
    private static final float LEGACY_SCALE = 7.5F;
    private static final float LEGACY_ALPHA = 0.125F;
    private static final float LEGACY_RED = 0.85F;
    private static final float LEGACY_GREEN = 0.9F;
    private static final float LEGACY_BLUE = 0.5F;
    private static final ParticleRenderType LEGACY_RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_RADIATION_FOG";
        }
    };

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
        this.xd *= 0.9599999785423279D;
        this.yd *= 0.9599999785423279D;
        this.zd *= 0.9599999785423279D;
        this.alpha = (float) Math.sin(this.age * Math.PI / 400.0D) * LEGACY_ALPHA;
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
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        int light = getLightColor(partialTick);
        double baseX = Mth.lerp(partialTick, this.xo, this.x) - camera.getPosition().x();
        double baseY = Mth.lerp(partialTick, this.yo, this.y) - camera.getPosition().y();
        double baseZ = Mth.lerp(partialTick, this.zo, this.z) - camera.getPosition().z();
        Random legacyRandom = new Random(LEGACY_RANDOM_SEED);
        float cumulativeX = 0.0F;
        float cumulativeY = 0.0F;
        float cumulativeZ = 0.0F;

        for (int i = 0; i < LEGACY_QUAD_COUNT; i++) {
            cumulativeX += (float) ((legacyRandom.nextGaussian() - 1.0D) * 2.5D);
            cumulativeY += (float) ((legacyRandom.nextGaussian() - 1.0D) * 0.15D);
            cumulativeZ += (float) ((legacyRandom.nextGaussian() - 1.0D) * 2.5D);
            float size = (float) (legacyRandom.nextDouble() * this.quadSize);
            float jitterX = (float) (legacyRandom.nextGaussian() * 0.5D);
            float jitterY = (float) (legacyRandom.nextGaussian() * 0.5D);
            float jitterZ = (float) (legacyRandom.nextGaussian() * 0.5D);
            float x = (float) baseX + cumulativeX + jitterX;
            float y = (float) baseY + cumulativeY + jitterY;
            float z = (float) baseZ + cumulativeZ + jitterZ;
            renderQuad(consumer, rotation, corners, x, y, z, size, u0, u1, v0, v1, light);
        }
    }

    private void renderQuad(VertexConsumer consumer, Quaternionf rotation, Vector3f[] corners,
            float x, float y, float z, float size, float u0, float u1, float v0, float v1, int light) {
        Vector3f corner0 = new Vector3f(corners[0]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner1 = new Vector3f(corners[1]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner2 = new Vector3f(corners[2]).rotate(rotation).mul(size).add(x, y, z);
        Vector3f corner3 = new Vector3f(corners[3]).rotate(rotation).mul(size).add(x, y, z);
        consumer.vertex(corner0.x(), corner0.y(), corner0.z()).uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        consumer.vertex(corner1.x(), corner1.y(), corner1.z()).uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        consumer.vertex(corner2.x(), corner2.y(), corner2.z()).uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        consumer.vertex(corner3.x(), corner3.y(), corner3.z()).uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return LEGACY_RENDER_TYPE;
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
