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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

@OnlyIn(Dist.CLIENT)
public class FoundryParticle extends Particle {
    private static final ResourceLocation LAVA_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/lava_gray.png");
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, net.minecraft.client.renderer.texture.TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
            RenderSystem.setShaderTexture(0, LAVA_TEXTURE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
        }

        @Override
        public String toString() {
            return "HBM_FOUNDRY";
        }
    };

    private final int red;
    private final int green;
    private final int blue;
    private final int dirX;
    private final int dirZ;
    private final double length;
    private final double base;
    private final double offset;

    public FoundryParticle(ClientLevel level, double x, double y, double z, int color, int direction, double length, double base, double offset) {
        super(level, x, y, z);
        Color bright = new Color(color).brighter();
        double brightener = 0.7D;
        this.red = (int) (255.0D - (255.0D - bright.getRed()) * brightener);
        this.green = (int) (255.0D - (255.0D - bright.getGreen()) * brightener);
        this.blue = (int) (255.0D - (255.0D - bright.getBlue()) * brightener);
        int dir = Mth.positiveModulo(direction, 6);
        this.dirX = dir == 4 ? -1 : dir == 5 ? 1 : 0;
        this.dirZ = dir == 2 ? -1 : dir == 3 ? 1 : 0;
        this.length = length;
        this.base = base;
        this.offset = offset;
        this.lifetime = 20;
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
        double pX = Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x();
        double pY = Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y();
        double pZ = Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z();
        Direction2 rot = rotateAroundUp(dirX, dirZ);
        double progress = (this.age + partialTick) / this.lifetime;
        double width = 0.0625D + progress * 0.0625D;
        double girth = 0.125D * (1.0D - progress);
        double dirXG = dirX * girth;
        double dirZG = dirZ * girth;
        double rotXW = rot.x * width;
        double rotZW = rot.z * width;
        double uMin = 0.5D - width;
        double uMax = 0.5D + width;
        double add = (System.currentTimeMillis() / 100L % 16L) / 16.0D;

        vertexBox(consumer, pX, pY, pZ, rotXW, rotZW, dirXG, dirZG, girth, uMin, uMax, add);
    }

    private void vertexBox(VertexConsumer consumer, double pX, double pY, double pZ, double rotXW, double rotZW,
            double dirXG, double dirZG, double girth, double uMin, double uMax, double add) {
        double dirOX = dirX * offset;
        double dirOZ = dirZ * offset;
        quad(consumer, pX, pY, pZ, rotXW, girth, rotZW, -rotXW, girth, -rotZW, -rotXW, -length, -rotZW, rotXW, -length, rotZW, uMax, length + add + girth, uMin, add);
        quad(consumer, pX, pY, pZ, dirXG + rotXW, 0, dirZG + rotZW, dirXG - rotXW, 0, dirZG - rotZW, dirXG - rotXW, -length, dirZG - rotZW, dirXG + rotXW, -length, dirZG + rotZW, uMax, length + add, uMin, add);
        quad(consumer, pX, pY, pZ, rotXW, girth, rotZW, dirXG + rotXW, 0, dirZG + rotZW, dirXG + rotXW, -length, dirZG + rotZW, rotXW, -length, rotZW, 0, length + add + girth, girth, add);
        quad(consumer, pX, pY, pZ, -rotXW, girth, -rotZW, dirXG - rotXW, 0, dirZG - rotZW, dirXG - rotXW, -length, dirZG - rotZW, -rotXW, -length, -rotZW, 0, length + add + girth, girth, add);
        quad(consumer, pX, pY, pZ, rotXW, 0, rotZW, -rotXW, 0, -rotZW, -rotXW - dirOX, base, -rotZW - dirOZ, rotXW - dirOX, base, rotZW - dirOZ, uMax, offset - add, uMin, -add);
        quad(consumer, pX, pY, pZ, rotXW, girth, rotZW, -rotXW, girth, -rotZW, -rotXW - dirOX, base + girth, -rotZW - dirOZ, rotXW - dirOX, base + girth, rotZW - dirOZ, uMax, offset - add + 0.25D, uMin, -add + 0.25D);
        quad(consumer, pX, pY, pZ, rotXW, 0, rotZW, rotXW, girth, rotZW, rotXW - dirOX, base + girth, rotZW - dirOZ, rotXW - dirOX, base, rotZW - dirOZ, girth, offset - add + 0.75D, 0, -add + 0.75D);
        quad(consumer, pX, pY, pZ, -rotXW, 0, -rotZW, -rotXW, girth, -rotZW, -rotXW - dirOX, base + girth, -rotZW - dirOZ, -rotXW - dirOX, base, -rotZW - dirOZ, girth, offset - add + 0.75D, 0, -add + 0.75D);
        quad(consumer, pX, pY, pZ, dirXG + rotXW, 0, dirZG + rotZW, dirXG - rotXW, 0, dirZG - rotZW, -rotXW, girth, -rotZW, rotXW, girth, rotZW, uMax, add + 0.75D, uMin, add + 0.875D);
    }

    private void quad(VertexConsumer consumer, double pX, double pY, double pZ,
            double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4,
            double uMax, double vMax, double uMin, double vMin) {
        vertex(consumer, pX + x1, pY + y1, pZ + z1, uMax, vMax);
        vertex(consumer, pX + x2, pY + y2, pZ + z2, uMin, vMax);
        vertex(consumer, pX + x3, pY + y3, pZ + z3, uMin, vMin);
        vertex(consumer, pX + x4, pY + y4, pZ + z4, uMax, vMin);
    }

    private void vertex(VertexConsumer consumer, double x, double y, double z, double u, double v) {
        consumer.vertex(x, y, z)
                .color(red, green, blue, 255)
                .uv((float) u, (float) v)
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

    private static Direction2 rotateAroundUp(int x, int z) {
        return new Direction2(-z, x);
    }

    private record Direction2(int x, int z) {
    }
}
