package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.logic.AirstrikeBomberEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Exact legacy style-to-Dornier/B-29 model and texture selection. */
public class AirstrikeBomberRenderer extends EntityRenderer<AirstrikeBomberEntity> {
    public AirstrikeBomberRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(AirstrikeBomberEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        int style = entity.style();
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) Math.sin((entity.tickCount + partialTick) * 0.05F) * 10.0F);
        if (style <= 4) {
            poseStack.scale(5.0F, 5.0F, 5.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
            ObjEntityModels.DORNIER.renderAll(dornierTexture(style), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        } else {
            poseStack.scale(30.0F / 3.1F, 30.0F / 3.1F, 30.0F / 3.1F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            ObjEntityModels.B29.renderAll(b29Texture(style), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AirstrikeBomberEntity entity) {
        return entity.style() <= 4 ? dornierTexture(entity.style()) : b29Texture(entity.style());
    }

    private static ResourceLocation dornierTexture(int style) {
        return switch (style) {
            case 2 -> ObjEntityModels.DORNIER_2_TEXTURE;
            case 4 -> ObjEntityModels.DORNIER_4_TEXTURE;
            default -> ObjEntityModels.DORNIER_1_TEXTURE;
        };
    }

    private static ResourceLocation b29Texture(int style) {
        return switch (style) {
            case 6 -> ObjEntityModels.B29_1_TEXTURE;
            case 7 -> ObjEntityModels.B29_2_TEXTURE;
            case 8 -> ObjEntityModels.B29_3_TEXTURE;
            default -> ObjEntityModels.B29_0_TEXTURE;
        };
    }
}
