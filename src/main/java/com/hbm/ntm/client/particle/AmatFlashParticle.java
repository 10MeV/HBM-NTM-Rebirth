package com.hbm.ntm.client.particle;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class AmatFlashParticle extends Particle {
    private static final int BEAM_COUNT = 100;
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, net.minecraft.client.renderer.texture.TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_AMAT_FLASH";
        }
    };

    private final float flashScale;

    public AmatFlashParticle(ClientLevel level, double x, double y, double z, float scale) {
        super(level, x, y, z);
        this.lifetime = 10;
        this.flashScale = scale;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        double intensity = (this.age + partialTick) / (double) this.lifetime;
        float alpha = (float) (1.0D - intensity);
        if (alpha <= 0.0F) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (this.xo + (this.x - this.xo) * partialTick - cameraPos.x());
        float y = (float) (this.yo + (this.y - this.yo) * partialTick - cameraPos.y());
        float z = (float) (this.zo + (this.z - this.zo) * partialTick - cameraPos.z());
        float globalScale = 0.2F * this.flashScale;
        Random random = new Random(432L);
        Quaternionf rotation = new Quaternionf();
        int centerAlpha = (int) (alpha * 255.0F);

        for (int i = 0; i < BEAM_COUNT; i++) {
            rotation.rotateX(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateY(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateZ(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateX(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateY(random.nextFloat() * ((float) Math.PI * 2.0F));
            float length = (random.nextFloat() * 20.0F + 15.0F) * (float) (intensity * 0.5D) * globalScale;
            float width = (random.nextFloat() * 2.0F + 3.0F) * (float) (intensity * 0.5D) * globalScale;
            Vector3f left = new Vector3f(-0.866F * width, length, -0.5F * width).rotate(rotation).add(x, y, z);
            Vector3f right = new Vector3f(0.866F * width, length, -0.5F * width).rotate(rotation).add(x, y, z);
            Vector3f back = new Vector3f(0.0F, length, width).rotate(rotation).add(x, y, z);
            LegacyWavefrontModel.emitUntexturedVertexColorTriangleIdentity(consumer,
                    x, y, z, 0xFFFFFF, centerAlpha,
                    left.x(), left.y(), left.z(), 0xFFFFFF, 0,
                    right.x(), right.y(), right.z(), 0xFFFFFF, 0);
            LegacyWavefrontModel.emitUntexturedVertexColorTriangleIdentity(consumer,
                    x, y, z, 0xFFFFFF, centerAlpha,
                    right.x(), right.y(), right.z(), 0xFFFFFF, 0,
                    back.x(), back.y(), back.z(), 0xFFFFFF, 0);
            LegacyWavefrontModel.emitUntexturedVertexColorTriangleIdentity(consumer,
                    x, y, z, 0xFFFFFF, centerAlpha,
                    back.x(), back.y(), back.z(), 0xFFFFFF, 0,
                    left.x(), left.y(), left.z(), 0xFFFFFF, 0);
        }
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
