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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugLineParticle extends Particle {
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "HBM_DEBUG_LINE";
        }
    };

    private final double lineX;
    private final double lineY;
    private final double lineZ;
    private final int color;

    public DebugLineParticle(ClientLevel level, double x, double y, double z, double lineX, double lineY, double lineZ, int color) {
        super(level, x, y, z);
        this.lineX = lineX;
        this.lineY = lineY;
        this.lineZ = lineZ;
        this.color = color == 0 ? 0xFFFFFF : color;
        this.lifetime = 60;
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
        Vec3 cameraPos = camera.getPosition();
        double startX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double startY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double startZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        double endX = startX + this.lineX;
        double endY = startY + this.lineY;
        double endZ = startZ + this.lineZ;
        int alpha = Mth.clamp((int) (255.0F * (1.0F - (this.age + partialTick) / (float) this.lifetime)), 0, 255);
        int red = (this.color >> 16) & 255;
        int green = (this.color >> 8) & 255;
        int blue = this.color & 255;
        consumer.vertex(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        consumer.vertex(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
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
