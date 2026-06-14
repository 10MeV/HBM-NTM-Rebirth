package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.entity.missile.CustomMissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CustomMissileRenderer extends EntityRenderer<CustomMissileEntity> {
    public CustomMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.75F;
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
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
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
