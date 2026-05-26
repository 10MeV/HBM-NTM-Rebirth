package com.hbm.ntm.client.particle;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class MukeFlashParticle extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/flare.png");
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.disableCull();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_MUKE_FLASH";
        }
    };

    private final boolean balefire;

    public MukeFlashParticle(ClientLevel level, double x, double y, double z, boolean balefire) {
        super(level, x, y, z);
        this.lifetime = 20;
        this.hasPhysics = false;
        this.balefire = balefire;
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
        if (this.age == 15) {
            spawnClouds();
        }
    }

    private void spawnClouds() {
        RandomSource random = this.random;

        for (double d = 0.0D; d <= 1.8D + 1.0E-9D; d += 0.1D) {
            MukeCloudParticle.add((ClientLevel) this.level, this.x, this.y, this.z,
                    random.nextGaussian() * 0.05D,
                    d + random.nextGaussian() * 0.02D,
                    random.nextGaussian() * 0.05D,
                    this.balefire);
        }

        for (int i = 0; i < 100; i++) {
            MukeCloudParticle.add((ClientLevel) this.level, this.x, this.y + 0.5D, this.z,
                    random.nextGaussian() * 0.5D,
                    random.nextInt(5) == 0 ? 0.02D : 0.0D,
                    random.nextGaussian() * 0.5D,
                    this.balefire);
        }

        for (int i = 0; i < 75; i++) {
            double motionX = random.nextGaussian() * 0.5D;
            double motionZ = random.nextGaussian() * 0.5D;
            double distanceSqr = motionX * motionX + motionZ * motionZ;

            if (distanceSqr > 1.5D) {
                motionX *= 0.5D;
                motionZ *= 0.5D;
                distanceSqr = motionX * motionX + motionZ * motionZ;
            }

            double motionY = 1.8D + (random.nextDouble() * 3.0D - 1.5D) * (0.75D - distanceSqr) * 0.5D;
            MukeCloudParticle.add((ClientLevel) this.level, this.x, this.y, this.z,
                    motionX,
                    motionY + random.nextGaussian() * 0.02D,
                    motionZ,
                    this.balefire);
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        float progressAge = this.age + partialTick;
        float alpha = Mth.clamp(1.0F - progressAge / (float) this.lifetime, 0.0F, 1.0F) * 0.5F;
        if (alpha <= 0.0F) {
            return;
        }
        float scale = progressAge * 3.0F + 1.0F;
        Vec3 cameraPos = camera.getPosition();
        double x = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double y = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double z = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        Quaternionf rotation = camera.rotation();
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(rotation).mul(scale);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(rotation).mul(scale);
        Random random = new Random();

        for (int i = 0; i < 24; i++) {
            random.setSeed(i * 31L + 1L);
            float px = (float) (x + random.nextDouble() * 15.0D - 7.5D);
            float py = (float) (y + random.nextDouble() * 7.5D - 3.75D);
            float pz = (float) (z + random.nextDouble() * 15.0D - 7.5D);

            putVertex(consumer, px - right.x() - up.x(), py - right.y() - up.y(), pz - right.z() - up.z(),
                    1.0F, 1.0F, alpha);
            putVertex(consumer, px - right.x() + up.x(), py - right.y() + up.y(), pz - right.z() + up.z(),
                    1.0F, 0.0F, alpha);
            putVertex(consumer, px + right.x() + up.x(), py + right.y() + up.y(), pz + right.z() + up.z(),
                    0.0F, 0.0F, alpha);
            putVertex(consumer, px + right.x() - up.x(), py + right.y() - up.y(), pz + right.z() - up.z(),
                    0.0F, 1.0F, alpha);
        }
    }

    private static void putVertex(VertexConsumer consumer, float x, float y, float z, float u, float v, float alpha) {
        consumer.vertex(x, y, z)
                .uv(u, v)
                .color(1.0F, 0.9F, 0.75F, alpha)
                .uv2(LightTexture.FULL_BRIGHT)
                .endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
