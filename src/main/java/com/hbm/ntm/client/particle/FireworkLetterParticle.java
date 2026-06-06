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
public class FireworkLetterParticle extends Particle {
    private final int color;
    private final char letter;

    public FireworkLetterParticle(ClientLevel level, double x, double y, double z, int color, char letter) {
        super(level, x, y, z);
        this.color = color;
        this.letter = letter;
        this.lifetime = 30;
        this.hasPhysics = false;
    }

    @Override
    public void render(com.mojang.blaze3d.vertex.VertexConsumer consumer, Camera camera, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String text = String.valueOf(this.letter);
        float renderAge = this.age + partialTick;
        float alpha = Mth.clamp(1.0F - renderAge / (float) this.lifetime, 0.0F, 1.0F);
        int alphaByte = Mth.clamp((int) (alpha * 255.0F), 10, 255);
        int argb = (this.color & 0xFFFFFF) | (alphaByte << 24);
        double scale = 1.0D - Math.exp(renderAge * -4.0D / this.lifetime);
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y, z);
        poseStack.mulPose(new Quaternionf(camera.rotation()));
        poseStack.scale((float) -scale, (float) -scale, (float) scale);
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        font.drawInBatch(text, -font.width(text) * 0.5F, -font.lineHeight * 0.5F, argb, false,
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
