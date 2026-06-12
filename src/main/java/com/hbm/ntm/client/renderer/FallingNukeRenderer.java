package com.hbm.ntm.client.renderer;

import com.hbm.ntm.entity.projectile.FallingNukeEntity;
import com.hbm.ntm.client.obj.ObjNukeModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FallingNukeRenderer extends EntityRenderer<FallingNukeEntity> {
    public FallingNukeRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
    }

    @Override
    public void render(FallingNukeEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(entity.legacyFacingMeta())));
        poseStack.translate(-2.0D, 0.0D, 0.0D);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        if (pitch < -80.0F) {
            pitch = 0.0F;
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        NuclearDeviceRenderer.renderCustomNuke(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static float legacyYaw(byte meta) {
        return switch (meta) {
            case 2 -> 90.0F;
            case 3 -> 270.0F;
            case 4 -> 180.0F;
            case 5 -> 0.0F;
            default -> 0.0F;
        };
    }

    @Override
    public ResourceLocation getTextureLocation(FallingNukeEntity entity) {
        return ObjNukeModels.CUSTOM_NUKE_TEXTURE;
    }
}
