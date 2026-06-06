package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

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
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderCrateCube(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight);
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MovingPackageEntity entity) {
        return TEXTURE;
    }

    private static void renderCrateCube(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        face(consumer, matrix, normal, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 1.0F, packedLight);
        face(consumer, matrix, normal, 0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.0F, 0.0F, -1.0F, packedLight);
        face(consumer, matrix, normal, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -1.0F, 0.0F, 0.0F, packedLight);
        face(consumer, matrix, normal, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 1.0F, 0.0F, 0.0F, packedLight);
        face(consumer, matrix, normal, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.0F, 1.0F, 0.0F, packedLight);
        face(consumer, matrix, normal, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.0F, -1.0F, 0.0F, packedLight);
    }

    private static void face(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz, int packedLight) {
        vertex(consumer, matrix, normal, x1, y1, z1, 0.0F, 1.0F, nx, ny, nz, packedLight);
        vertex(consumer, matrix, normal, x2, y2, z2, 1.0F, 1.0F, nx, ny, nz, packedLight);
        vertex(consumer, matrix, normal, x3, y3, z3, 1.0F, 0.0F, nx, ny, nz, packedLight);
        vertex(consumer, matrix, normal, x4, y4, z4, 0.0F, 0.0F, nx, ny, nz, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
            float x, float y, float z, float u, float v, float nx, float ny, float nz, int packedLight) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
