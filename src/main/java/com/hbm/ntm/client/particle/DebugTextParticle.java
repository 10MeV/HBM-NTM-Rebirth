package com.hbm.ntm.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class DebugTextParticle extends Particle {
    private final int color;
    private final String text;
    private final float textScale;

    public DebugTextParticle(ClientLevel level, double x, double y, double z, int color, String text, float scale) {
        super(level, x, y, z);
        this.color = color;
        this.text = text == null ? "" : text;
        this.textScale = scale;
        this.lifetime = 100;
        this.yd = 0.01D;
        this.hasPhysics = false;
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.VertexConsumer consumer, Camera camera, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y, z);
        poseStack.mulPose(new Quaternionf(camera.rotation()));
        float scale = -0.01F * this.textScale;
        poseStack.scale(scale, scale, 0.01F * this.textScale);
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        font.drawInBatch(this.text, -font.width(this.text) * 0.5F, -font.lineHeight * 0.5F, this.color, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        buffer.endBatch();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }
}
