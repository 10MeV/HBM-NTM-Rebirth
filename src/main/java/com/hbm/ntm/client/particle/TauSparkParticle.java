package com.hbm.ntm.client.particle;

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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TauSparkParticle extends Particle {
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(3.0F);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.lineWidth(1.0F);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_TAU_SPARK";
        }
    };

    private final List<Vec3> steps = new ArrayList<>();
    private int threshold;

    public TauSparkParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, boolean small) {
        super(level, x, y, z);
        this.xd = motionX;
        this.yd = small ? -Math.abs(motionY) : motionY;
        this.zd = motionZ;
        this.threshold = small ? 3 : 4 + this.random.nextInt(3);
        this.lifetime = small ? 2 + this.random.nextInt(3) : 20 + this.random.nextInt(10);
        this.gravity = 0.5F;
        this.hasPhysics = true;
        this.steps.add(new Vec3(this.xd, this.yd, this.zd));
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
        this.steps.add(new Vec3(this.xd, this.yd, this.zd));
        while (this.steps.size() > this.threshold) {
            this.steps.remove(0);
        }
        this.yd -= 0.04D * this.gravity;
        double previousY = this.yd;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround) {
            this.onGround = false;
            this.yd = -previousY * 0.8D;
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (this.steps.size() < 2) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        double currentX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double currentY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double currentZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        consumer.vertex(currentX, currentY, currentZ).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
        for (int i = this.steps.size() - 1; i >= 1; i--) {
            Vec3 step = this.steps.get(i);
            currentX -= step.x;
            currentY -= step.y;
            currentZ -= step.z;
            consumer.vertex(currentX, currentY, currentZ).color(255, 255, 255, 255).uv2(LightTexture.FULL_BRIGHT).endVertex();
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
