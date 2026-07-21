package com.hbm.ntm.client.renderer;

import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.entity.mob.glyphid.EntityGlyphidNuclear;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GlyphidRenderer<T extends EntityGlyphid> extends EntityRenderer<T> {
    private static final LegacyWavefrontModel MODEL = ObjEntityModels.GLYPHID;
    private static final String[] BODY_PARTS = {
            "Body",
            "ArmLeftUpper", "ArmLeftMid", "ArmLeftLower",
            "ArmRightUpper", "ArmRightMid", "ArmRightLower",
            "JawTop", "JawLeft", "JawRight",
            "LegLeftUpper", "LegLeftLower",
            "LegRightUpper", "LegRightLower"
    };
    private static final String[] ARMOR_PARTS = {
            "ArmorFront", "ArmorLeft", "ArmorRight", "ArmLeftArmor", "ArmRightArmor"
    };

    public GlyphidRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ResourceLocation texture = getTextureLocation(entity);
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F - Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot));
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, -1.5D, 0.0D);
        double scale = entity.getScale();
        poseStack.scale((float) scale, (float) scale, (float) scale);
        int flashAlpha = 0;
        if (entity instanceof EntityGlyphidNuclear nuclear) {
            float swell = (nuclear.deathTicks + partialTick) / 95.0F;
            float flash = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
            swell = Mth.clamp(swell, 0.0F, 1.0F);
            float squared = swell * swell;
            squared *= squared;
            poseStack.scale((1.0F + squared * 0.4F) * flash, (1.0F + squared * 0.1F) / flash,
                    (1.0F + squared * 0.4F) * flash);
            float overlay = (nuclear.deathTicks + partialTick) / 20.0F;
            flashAlpha = (int) Mth.clamp(overlay * 0.2F * 255.0F * (((int) (overlay * 10.0F) % 4 < 2) ? 0.75F : 1.0F), 0.0F, 255.0F);
        }
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                255, 255, 255, 255, false, BODY_PARTS);
        if (flashAlpha > 0) {
            // RenderLiving's 1.7.10 color multiplier is a white translucent overlay,
            // not transparency applied to the base skin.
            MODEL.renderOnlyUntextured(poseStack, buffer, 255, 255, 255, flashAlpha,
                    LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, BODY_PARTS);
        }
        renderArmor(entity, texture, poseStack, buffer, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getSkin();
    }

    private static void renderArmor(EntityGlyphid entity, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        byte armor = entity.getArmorBits();
        for (int i = 0; i < ARMOR_PARTS.length; i++) {
            if ((armor & (1 << i)) == 0) {
                continue;
            }
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                    255, 255, 255, 255, false, ARMOR_PARTS[i]);
        }
    }
}
