package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.CloudTomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Direct buffered conversion of RenderCloudTom's five translucent 16-sided curtains. */
public final class CloudTomRenderer extends EntityRenderer<CloudTomEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/explosion/tomblast.png");
    private static final int SEGMENTS = 16;
    private static final int LAYERS = 5;

    public CloudTomRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(CloudTomEntity cloud, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        float scale = cloud.age() + partialTick;
        float textureMovement = -((Minecraft.getInstance().player == null
                ? 0.0F : Minecraft.getInstance().player.tickCount) + partialTick) * 0.05F;
        float segmentAngle = (float) (Math.PI * 2.0D / SEGMENTS);

        poseStack.pushPose();
        for (int segment = 0; segment < SEGMENTS; segment++) {
            for (int layer = 0; layer < LAYERS; layer++) {
                float modifier = 1.0F - layer * 0.025F;
                float height = 20.0F + layer * 10.0F;
                float offset = 1.0F / layer; // RenderCloudTom deliberately emits infinity for layer zero.
                float angle0 = segmentAngle * segment;
                float angle1 = angle0 + segmentAngle;
                float x0 = (float) Math.cos(angle0) * scale * modifier;
                float z0 = (float) -Math.sin(angle0) * scale * modifier;
                float x1 = (float) Math.cos(angle1) * scale * modifier;
                float z1 = (float) -Math.sin(angle1) * scale * modifier;
                PoseStack.Pose pose = poseStack.last();
                vertex(vertices, pose, x0, height, z0, 0.0F, 1.0F + offset + textureMovement, 0);
                vertex(vertices, pose, x0, -20.0F, z0, 0.0F, offset + textureMovement, 255);
                vertex(vertices, pose, x1, -20.0F, z1, 1.0F, offset + textureMovement, 255);
                vertex(vertices, pose, x1, height, z1, 1.0F, 1.0F + offset + textureMovement, 0);
            }
        }
        poseStack.popPose();
        super.render(cloud, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
            float u, float v, int alpha) {
        consumer.vertex(pose.pose(), x, y, z).color(255, 255, 255, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(CloudTomEntity cloud) {
        return TEXTURE;
    }
}
