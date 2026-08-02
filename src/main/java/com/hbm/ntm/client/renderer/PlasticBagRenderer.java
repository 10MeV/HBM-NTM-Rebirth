package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.mob.EntityPlasticBag;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** OBJ/no-cull migration of 1.7.10 {@code RenderPlasticBag}. */
public final class PlasticBagRenderer extends EntityRenderer<EntityPlasticBag> {
    public PlasticBagRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.0F; }

    @Override
    public void render(EntityPlasticBag bag, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.rotLerp(partialTick, bag.yRotO, bag.getYRot()) + 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, Mth.lerp(partialTick, bag.xRotO, bag.getXRot()) - 90.0F);
        ObjEntityModels.PLASTIC_BAG.renderAll(ObjEntityModels.PLASTIC_BAG_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
        super.render(bag, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override public ResourceLocation getTextureLocation(EntityPlasticBag bag) { return ObjEntityModels.PLASTIC_BAG_TEXTURE; }
}
