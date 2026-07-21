package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.mob.EntityFBIDrone;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Exact legacy RenderDrone transform and no-cull OBJ presentation. */
public final class FBIDroneRenderer extends EntityRenderer<EntityFBIDrone> {
    public FBIDroneRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityFBIDrone entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (new Random(entity.getId()).nextDouble() * 360.0D));
        ObjEntityModels.QUADCOPTER.renderAll(ObjEntityModels.QUADCOPTER_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFBIDrone entity) {
        return ObjEntityModels.QUADCOPTER_TEXTURE;
    }
}
