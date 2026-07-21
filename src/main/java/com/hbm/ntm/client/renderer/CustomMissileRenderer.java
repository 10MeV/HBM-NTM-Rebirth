package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.entity.missile.CustomMissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CustomMissileRenderer extends EntityRenderer<CustomMissileEntity> {
    public CustomMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(CustomMissileEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ObjMissilePartModels.LegacyMissilePart warhead =
                ObjMissilePartModels.part(entity.warheadLegacyName());
        ObjMissilePartModels.LegacyMissilePart fuselage =
                ObjMissilePartModels.part(entity.fuselageLegacyName());
        ObjMissilePartModels.LegacyMissilePart fins =
                ObjMissilePartModels.part(entity.finsLegacyName());
        ObjMissilePartModels.LegacyMissilePart thruster =
                ObjMissilePartModels.part(entity.thrusterLegacyName());

        poseStack.pushPose();
        float renderYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F;
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        LegacyPoseRotations.rotateYDegrees(poseStack, renderYaw);
        LegacyPoseRotations.rotateZDegrees(poseStack, pitch);
        LegacyPoseRotations.rotateYDegrees(poseStack, -renderYaw);
        ObjMissilePartModels.renderMissile(thruster, fins, fuselage, warhead, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMissileEntity entity) {
        ObjMissilePartModels.LegacyMissilePart warhead =
                ObjMissilePartModels.part(entity.warheadLegacyName());
        return warhead == null ? ObjMissilePartModels.UNIVERSAL_TEXTURE : warhead.texture();
    }
}
