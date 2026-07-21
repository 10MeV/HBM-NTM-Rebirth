package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjVehicleModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.train.CargoTramEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** OBJ renderer matching legacy RenderTrainCargoTram's bogie-centre offset and rotations. */
public final class CargoTramRenderer extends EntityRenderer<CargoTramEntity> {
    public CargoTramRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(CargoTramEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        double positionX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double positionY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double positionZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        poseStack.translate(Mth.lerp(partialTick, entity.getLastRenderX(), entity.getRenderX()) - positionX,
                Mth.lerp(partialTick, entity.getLastRenderY(), entity.getRenderY()) - positionY,
                Mth.lerp(partialTick, entity.getLastRenderZ(), entity.getRenderZ()) - positionZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, -Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()));
        LegacyPoseRotations.rotateXDegrees(poseStack, -Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        ObjVehicleModels.TRAM.renderAll(ObjVehicleModels.TRAM_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override public ResourceLocation getTextureLocation(CargoTramEntity entity) { return ObjVehicleModels.TRAM_TEXTURE; }
}
