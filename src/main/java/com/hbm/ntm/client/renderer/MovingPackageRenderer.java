package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MovingPackageRenderer extends EntityRenderer<MovingPackageEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/block/crate.png");

    public MovingPackageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(MovingPackageEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.0125D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderCrateCube(poseStack, buffer, packedLight);
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingPackageEntity entity) {
        return TEXTURE;
    }

    private static void renderCrateCube(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer consumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(TEXTURE, buffer,
                LegacyTexturedRenderMode.CUTOUT_CULL);
        PoseStack.Pose pose = poseStack.last();
        face(consumer, pose, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 1.0F, packedLight);
        face(consumer, pose, 0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.0F, 0.0F, -1.0F, packedLight);
        face(consumer, pose, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -1.0F, 0.0F, 0.0F, packedLight);
        face(consumer, pose, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 1.0F, 0.0F, 0.0F, packedLight);
        face(consumer, pose, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.0F, 1.0F, 0.0F, packedLight);
        face(consumer, pose, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.0F, -1.0F, 0.0F, packedLight);
    }

    private static void face(VertexConsumer consumer, PoseStack.Pose pose,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz, int packedLight) {
        LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, packedLight, OverlayTexture.NO_OVERLAY,
                nx, ny, nz,
                x1, y1, z1, 0.0F, 1.0F, 255,
                x2, y2, z2, 1.0F, 1.0F, 255,
                x3, y3, z3, 1.0F, 0.0F, 255,
                x4, y4, z4, 0.0F, 0.0F, 255,
                0xFFFFFF);
    }
}
