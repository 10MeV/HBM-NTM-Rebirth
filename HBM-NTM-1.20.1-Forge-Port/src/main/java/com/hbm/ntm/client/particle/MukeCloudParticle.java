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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MukeCloudParticle extends Particle {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/explosion.png");
    private static final ResourceLocation BALEFIRE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/explosion_bf.png");
    private static final float FRAME_SIZE = 1.0F / 5.0F;
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_MUKE_CLOUD";
        }
    };
    private static final ParticleRenderType BALEFIRE_RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, BALEFIRE_TEXTURE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_MUKE_CLOUD_BALEFIRE";
        }
    };

    private final boolean balefire;
    private final float friction;
    private float quadSize;

    public MukeCloudParticle(ClientLevel level, double x, double y, double z,
            double motionX, double motionY, double motionZ, boolean balefire) {
        super(level, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.balefire = balefire;
        this.quadSize = 3.0F;
        this.hasPhysics = true;

        if (motionY > 0.0D) {
            this.friction = 0.9F;
            this.lifetime = motionY > 0.1D
                    ? 92 + random.nextInt(11) + (int) (motionY * 20.0D)
                    : 72 + random.nextInt(11);
        } else if (motionY == 0.0D) {
            this.friction = 0.95F;
            this.lifetime = 52 + random.nextInt(11);
        } else {
            this.friction = 0.85F;
            this.lifetime = 122 + random.nextInt(31);
            this.age = 80;
        }
    }

    @Override
    public void tick() {
        this.hasPhysics = this.age > 2;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime - 2) {
            this.remove();
            return;
        }
        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        int frame = Mth.clamp((int) ((this.age + partialTick) * 25.0F / Math.max(1, this.lifetime)), 0, 24);
        float uMin = (frame % 5) * FRAME_SIZE;
        float uMax = uMin + FRAME_SIZE;
        float vMin = (frame / 5) * FRAME_SIZE;
        float vMax = vMin + FRAME_SIZE;
        Vec3 cameraPos = camera.getPosition();
        double x = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double y = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double z = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        float scale = this.quadSize;
        org.joml.Quaternionf rotation = camera.rotation();
        org.joml.Vector3f[] corners = new org.joml.Vector3f[] {
                new org.joml.Vector3f(-scale, -scale, 0.0F),
                new org.joml.Vector3f(-scale, scale, 0.0F),
                new org.joml.Vector3f(scale, scale, 0.0F),
                new org.joml.Vector3f(scale, -scale, 0.0F)
        };
        for (org.joml.Vector3f corner : corners) {
            corner.rotate(rotation).add((float) x, (float) y, (float) z);
        }
        consumer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(uMax, vMax).color(1.0F, 1.0F, 1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(uMax, vMin).color(1.0F, 1.0F, 1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(uMin, vMin).color(1.0F, 1.0F, 1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        consumer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(uMin, vMax).color(1.0F, 1.0F, 1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return balefire ? BALEFIRE_RENDER_TYPE : RENDER_TYPE;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
