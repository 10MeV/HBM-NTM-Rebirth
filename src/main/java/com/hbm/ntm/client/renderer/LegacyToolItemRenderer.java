package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.CrucibleWeaponItem;
import com.hbm.ntm.item.HbmFueledAbilityToolItem;
import com.hbm.ntm.item.LegacyBoltgunItem;
import com.hbm.ntm.item.LegacyChainsawItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Modern OBJ backend for ItemRenderBoltgun and ItemRenderChainsaw. */
public final class LegacyToolItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final LegacyToolItemRenderer INSTANCE = new LegacyToolItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LegacyToolItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (stack.getItem() instanceof LegacyBoltgunItem) {
            renderBoltgun(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (stack.getItem() instanceof LegacyChainsawItem saw) {
            renderChainsaw(stack, saw, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (stack.getItem() instanceof CrucibleWeaponItem crucible) {
            renderCrucible(stack, crucible, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderBoltgun(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.5D, 0.35D, -0.25D);
                LegacyPoseRotations.rotateZDegrees(poseStack, 15.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 80.0F);
                poseStack.scale(0.15F, 0.15F, 0.15F);
                ObjWeaponModels.BOLTGUN.renderPart("Gun", ObjWeaponModels.BOLTGUN_TEXTURE, poseStack, buffer, light, overlay);
                poseStack.pushPose();
                poseStack.translate(0.0D, 0.0D, -LegacyHbmAnimations.getRelevantTransformation("RECOIL")[0]);
                ObjWeaponModels.BOLTGUN.renderPart("Barrel", ObjWeaponModels.BOLTGUN_TEXTURE, poseStack, buffer, light, overlay);
                poseStack.popPose();
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.scale(0.25F, 0.25F, 0.25F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 10.0F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 10.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, 10.0F);
                poseStack.translate(1.5D, -0.25D, 1.0D);
                ObjWeaponModels.BOLTGUN.renderAll(ObjWeaponModels.BOLTGUN_TEXTURE, poseStack, buffer, light, overlay);
            }
            case GROUND -> {
                poseStack.scale(0.1F, 0.1F, 0.1F);
                ObjWeaponModels.BOLTGUN.renderAll(ObjWeaponModels.BOLTGUN_TEXTURE, poseStack, buffer, light, overlay);
            }
            case GUI -> {
                poseStack.translate(7.0D, 10.0D, 0.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, -135.0F);
                poseStack.scale(1.75F, 1.75F, -1.75F);
                ObjWeaponModels.BOLTGUN.renderAll(ObjWeaponModels.BOLTGUN_TEXTURE, poseStack, buffer, light, overlay);
            }
            default -> ObjWeaponModels.BOLTGUN.renderAll(ObjWeaponModels.BOLTGUN_TEXTURE, poseStack, buffer, light, overlay);
        }
        poseStack.popPose();
    }

    private static void renderChainsaw(ItemStack stack, HbmFueledAbilityToolItem saw, ItemDisplayContext context,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.5D, 0.25D, -0.25D);
                LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 80.0F);
                poseStack.scale(0.35F, 0.35F, 0.35F);
                double[] rotation = LegacyHbmAnimations.getRelevantTransformation("SWING_ROT");
                double[] translation = LegacyHbmAnimations.getRelevantTransformation("SWING_TRANS");
                poseStack.translate(translation[0], translation[1], translation[2]);
                LegacyPoseRotations.rotateZDegrees(poseStack, (float) rotation[2]);
                LegacyPoseRotations.rotateYDegrees(poseStack, (float) rotation[1]);
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) rotation[0]);
                renderChainsawModel(stack, saw, poseStack, buffer, light, overlay);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.scale(-0.375F, -0.375F, -0.375F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 85.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, 135.0F);
                poseStack.translate(-0.125D, -2.0D, 1.75D);
                renderChainsawModel(stack, saw, poseStack, buffer, light, overlay);
            }
            case GROUND -> {
                poseStack.scale(0.5F, 0.5F, 0.5F);
                renderChainsawModel(stack, saw, poseStack, buffer, light, overlay);
            }
            case GUI -> {
                poseStack.translate(8.0D, 10.0D, 0.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, -135.0F);
                poseStack.scale(4.0F, 4.0F, -4.0F);
                renderChainsawModel(stack, saw, poseStack, buffer, light, overlay);
            }
            default -> renderChainsawModel(stack, saw, poseStack, buffer, light, overlay);
        }
        poseStack.popPose();
    }

    private static void renderChainsawModel(ItemStack stack, HbmFueledAbilityToolItem saw, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        LegacyWavefrontModel model = ObjWeaponModels.CHAINSAW;
        model.renderPart("Saw", ObjWeaponModels.CHAINSAW_TEXTURE, poseStack, buffer, light, overlay);
        double run = saw.canOperate(stack) ? (System.currentTimeMillis() % 100.0D) * 0.25D / 100.0D : 0.0625D;
        for (int i = 0; i < 20; i++) {
            double forward = i * 0.25D + run - 2.0625D;
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.375D, 2.5D);
            double angle = Math.max(0.0D, Math.min(Math.PI * 0.25D, forward));
            LegacyPoseRotations.rotateXDegrees(poseStack, (float) (angle * 180.0D / (Math.PI * 0.25D)));
            poseStack.translate(0.0D, -0.375D, -0.5625D);
            if (forward < 0.0D) poseStack.translate(0.0D, 0.0D, forward);
            if (forward > Math.PI * 0.25D) poseStack.translate(0.0D, 0.0D, forward - Math.PI * 0.25D);
            model.renderPart("Tooth", ObjWeaponModels.CHAINSAW_TEXTURE, poseStack, buffer, light, overlay);
            poseStack.popPose();
        }
    }

    private static void renderCrucible(ItemStack stack, CrucibleWeaponItem crucible, ItemDisplayContext context,
            PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        boolean active = crucible.canOperate(stack);
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(1.5D, -0.3D, 0.0D);
                boolean blocking = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isBlocking();
                if (blocking) {
                    poseStack.translate(-0.125D, -0.25D, 0.0D);
                }
                poseStack.scale(0.3F, 0.3F, 0.3F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                double[] swingRotation = LegacyHbmAnimations.getRelevantTransformation("SWING_ROT");
                boolean swinging = !blocking && swingRotation[0] != 0.0D;
                if (!blocking) {
                    double[] swingTranslation = LegacyHbmAnimations.getRelevantTransformation("SWING_TRANS");
                    poseStack.translate(swingTranslation[0], swingTranslation[1], swingTranslation[2]);
                    LegacyPoseRotations.rotateXDegrees(poseStack, (float) swingRotation[0]);
                    LegacyPoseRotations.rotateZDegrees(poseStack, (float) swingRotation[2]);
                    LegacyPoseRotations.rotateYDegrees(poseStack, (float) swingRotation[1]);
                }
                double[] guardRotation = LegacyHbmAnimations.getRelevantTransformation("GUARD_ROT");
                renderCrucibleHiltAndGuards(poseStack, buffer, light, overlay,
                        !swinging && !active ? 90.0D : guardRotation[0]);
                if (guardRotation[2] == 0.0D && (swinging || active)) {
                    renderCrucibleBlade(poseStack, buffer, overlay, true);
                }
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
                poseStack.translate(0.75D, -0.4D, 0.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                poseStack.scale(0.15F, 0.15F, 0.15F);
                renderCrucibleHiltAndGuards(poseStack, buffer, light, overlay, active ? 0.0D : 90.0D);
                if (active) renderCrucibleBlade(poseStack, buffer, overlay, true);
            }
            case GROUND -> {
                poseStack.translate(-0.75D, 0.6D, 0.0D);
                LegacyPoseRotations.rotateZDegrees(poseStack, -45.0F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
                poseStack.translate(0.75D, -0.4D, 0.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                poseStack.scale(0.15F, 0.15F, 0.15F);
                renderCrucibleHiltAndGuards(poseStack, buffer, light, overlay, active ? 0.0D : 90.0D);
                if (active) renderCrucibleBlade(poseStack, buffer, overlay, true);
            }
            case GUI -> {
                poseStack.translate(2.0D, 14.0D, 0.0D);
                LegacyPoseRotations.rotateZDegrees(poseStack, -135.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
                poseStack.scale(1.5F, 1.5F, 1.5F);
                renderCrucibleHiltAndGuards(poseStack, buffer, light, overlay, active ? 0.0D : 90.0D);
                if (active) renderCrucibleBlade(poseStack, buffer, overlay, false);
            }
            default -> renderCrucibleHiltAndGuards(poseStack, buffer, light, overlay, active ? 0.0D : 90.0D);
        }
        poseStack.popPose();
    }

    private static void renderCrucibleHiltAndGuards(PoseStack poseStack, MultiBufferSource buffer, int light,
            int overlay, double guardRotation) {
        LegacyWavefrontModel model = ObjWeaponModels.CRUCIBLE;
        model.renderPart("Hilt", ObjWeaponModels.CRUCIBLE_HILT_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.0D, 0.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) -guardRotation);
        poseStack.translate(0.0D, -3.0D, -0.5D);
        model.renderPart("GuardLeft", ObjWeaponModels.CRUCIBLE_GUARD_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.0D, -0.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) guardRotation);
        poseStack.translate(0.0D, -3.0D, 0.5D);
        model.renderPart("GuardRight", ObjWeaponModels.CRUCIBLE_GUARD_TEXTURE, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    private static void renderCrucibleBlade(PoseStack poseStack, MultiBufferSource buffer, int overlay,
            boolean fullBright) {
        poseStack.pushPose();
        poseStack.translate(0.005D, 0.0D, 0.0D);
        ObjWeaponModels.CRUCIBLE.renderPart("Blade", ObjWeaponModels.CRUCIBLE_BLADE_TEXTURE, poseStack, buffer,
                fullBright ? LightTexture.FULL_BRIGHT : 0, overlay);
        poseStack.popPose();
    }
}
