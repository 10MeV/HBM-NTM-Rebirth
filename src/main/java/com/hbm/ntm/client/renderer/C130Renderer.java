package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.logic.C130Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Exact RenderC130 part order and propeller pivots, submitted through the shared OBJ backend. */
public class C130Renderer extends EntityRenderer<C130Entity> {
    public C130Renderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.0F; }
    @Override public void render(C130Entity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        ObjEntityModels.C130.renderPart("Plane", ObjEntityModels.C130_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        double spin = Util.getMillis() * 15.0D % 360.0D;
        renderPropeller("Prop1", 10.0D, 4.2D, -20.5D, spin, poseStack, buffer, packedLight);
        renderPropeller("Prop2", 10.0D, 4.2D, -11.16D, spin, poseStack, buffer, packedLight);
        renderPropeller("Prop3", 10.0D, 4.2D, 11.16D, spin, poseStack, buffer, packedLight);
        renderPropeller("Prop4", 10.0D, 4.2D, 20.5D, spin, poseStack, buffer, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }
    private static void renderPropeller(String part, double x, double y, double z, double spin, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose(); poseStack.translate(x, y, z); LegacyPoseRotations.rotateXDegrees(poseStack, (float) spin);
        poseStack.translate(-x, -y, -z);
        ObjEntityModels.C130.renderPart(part, ObjEntityModels.C130_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
    @Override public ResourceLocation getTextureLocation(C130Entity entity) { return ObjEntityModels.C130_TEXTURE; }
}
