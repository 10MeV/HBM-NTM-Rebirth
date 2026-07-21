package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjArmorModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Source-shaped carrier for ModEventHandlerClient's four UUID/name-gated accessories.
 * Geometry remains on the shared LegacyWavefrontModel and untextured-effect backends.
 */
public final class LegacySpecialAccessoryRenderer {
    // The enclosing modern pose has already applied the legacy 0.0625 ModelBiped scale.
    // These values therefore remain in the old model's pixel space.
    private static final double PIXEL = 1.0D;
    private static final double FLAME_BASE_HEIGHT = 2.0D;
    private static final double FLAME_TIP_HEIGHT = 6.0D;
    private static final int FLAME_BASE = 0x808080;
    private static final int FLAME_MID = 0x004040;
    private static final int FLAME_TIP = 0x000000;

    public static void render(Player player, HumanoidModel<?> humanoid, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, float partialTick) {
        if (player == null || humanoid == null || !player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()
                // Old code tests Potion.invisibility specifically; do not widen this
                // UUID-gated accessory suppression to other modern invisibility states.
                || player.hasEffect(MobEffects.INVISIBILITY)) {
            return;
        }
        LegacyAccessoryRenderHelper.specialAccessoryPlan(player, partialTick).ifPresent(plan -> {
            poseStack.pushPose();
            humanoid.body.translateAndRotate(poseStack);
            poseStack.scale(LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                    LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                    LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE);
            switch (plan.kind()) {
                case WINGS -> renderWings(player, poseStack, buffer, packedLight, plan.wingMode());
                case AXE_PACK -> renderAxePack(player, poseStack, buffer, packedLight);
                case TAIL -> ObjArmorModels.renderPart(ObjArmorModels.TAIL, "FaggyAssFuckingTailThing",
                        ObjArmorModels.TAIL_PEEP_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
                case NONE -> {
                }
            }
            poseStack.popPose();
        });
    }

    private static void renderWings(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int mode) {
        double rot = Math.sin(player.tickCount * 0.2D) * 20.0D;
        double rot2 = Math.sin(player.tickCount * 0.2D - Math.PI * 0.5D) * 50.0D + 30.0D;
        if (mode != 1 && player.onGround()) {
            rot = 20.0D;
            rot2 = 160.0D;
        }
        if (mode == 1) {
            if (player.onGround()) {
                rot = 30.0D;
                rot2 = -30.0D;
            } else if (player.getDeltaMovement().y < -0.1D) {
                rot = 0.0D;
                rot2 = 10.0D;
            } else {
                rot = 30.0D;
                rot2 = 20.0D;
            }
        }
        var texture = mode == 2 ? ObjArmorModels.WINGS_BOB_TEXTURE
                : mode == 3 ? ObjArmorModels.WINGS_BLACK_TEXTURE : ObjArmorModels.WINGS_MURK_TEXTURE;
        poseStack.translate(0.0D, -2.0D, 0.0D);
        renderLeftWing(poseStack, buffer, packedLight, texture, rot, rot2);
        renderRightWing(poseStack, buffer, packedLight, texture, rot, rot2);
    }

    private static void renderLeftWing(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            net.minecraft.resources.ResourceLocation texture, double rot, double rot2) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, -10.0F);
        poseStack.translate(1.0D, 5.0D, 3.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (rot * 0.5D));
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (rot + 5.0D));
        LegacyPoseRotations.rotateXDegrees(poseStack, 45.0F);
        poseStack.translate(-1.0D, -5.0D, -3.0D);
        poseStack.translate(1.0D, 5.0D, 3.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) rot);
        poseStack.translate(-1.0D, -5.0D, -3.0D);
        ObjArmorModels.renderPartCull(ObjArmorModels.WINGS, "LeftBase", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.translate(16.0D, 5.0D, 2.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) rot2);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (rot2 * 0.25D + 5.0D));
        poseStack.translate(-16.0D, -5.0D, -2.0D);
        ObjArmorModels.renderPartCull(ObjArmorModels.WINGS, "LeftTip", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderRightWing(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            net.minecraft.resources.ResourceLocation texture, double rot, double rot2) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, 10.0F);
        poseStack.translate(-1.0D, 5.0D, 3.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (-rot * 0.5D));
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-rot - 5.0D));
        LegacyPoseRotations.rotateXDegrees(poseStack, 45.0F);
        poseStack.translate(1.0D, -5.0D, -3.0D);
        poseStack.translate(-1.0D, 5.0D, 3.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -rot);
        poseStack.translate(1.0D, -5.0D, -3.0D);
        ObjArmorModels.renderPartCull(ObjArmorModels.WINGS, "RightBase", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.translate(-16.0D, 5.0D, 2.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) -rot2);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-rot2 * 0.25D - 5.0D));
        poseStack.translate(16.0D, -5.0D, -2.0D);
        ObjArmorModels.renderPartCull(ObjArmorModels.WINGS, "RightTip", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderAxePack(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ObjArmorModels.renderPart(ObjArmorModels.AXEPACK, "Wings", ObjArmorModels.WINGS_PHEO_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.pushPose();
        if (player.isShiftKeyDown()) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 28.6479F);
        }
        poseStack.translate(0.0D, PIXEL * 15.0D, PIXEL * 5.5D);
        renderFlame(poseStack, buffer);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, PIXEL * 3.0D, PIXEL * 5.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -25.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, -90.0F);
        poseStack.translate(0.0D, PIXEL * 5.0D, 0.0D);
        renderFlameCluster(poseStack, buffer);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, PIXEL * 15.0D, PIXEL * 5.5D);
        renderFlame(poseStack, buffer);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, PIXEL * 3.0D, PIXEL * 5.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 25.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        poseStack.translate(0.0D, PIXEL * 5.0D, 0.0D);
        renderFlameCluster(poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderFlameCluster(PoseStack poseStack, MultiBufferSource buffer) {
        renderFlame(poseStack, buffer);
        poseStack.pushPose();
        poseStack.translate(0.0D, -PIXEL * 5.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
        poseStack.translate(-PIXEL, PIXEL * 5.5D, 0.0D);
        renderFlame(poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, -PIXEL * 5.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, -45.0F);
        poseStack.translate(PIXEL, PIXEL * 5.5D, 0.0D);
        renderFlame(poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderFlame(PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 255,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES));
        double width = PIXEL;
        double diagonal = Math.sqrt(2.0D) * 0.5D * width;
        double base = FLAME_BASE_HEIGHT;
        double tip = FLAME_TIP_HEIGHT;
        double[][] ring = {{width, 0.0D}, {diagonal, diagonal}, {0.0D, width}, {-diagonal, diagonal},
                {-width, 0.0D}, {-diagonal, -diagonal}, {0.0D, -width}, {diagonal, -diagonal}};
        PoseStack.Pose pose = poseStack.last();
        for (int index = 0; index < ring.length; index++) {
            double[] left = ring[index];
            double[] right = ring[(index + 1) % ring.length];
            triangle(consumer, pose, 0.0D, 0.0D, 0.0D, FLAME_BASE,
                    left[0], base, left[1], FLAME_MID, right[0], base, right[1], FLAME_MID);
            triangle(consumer, pose, 0.0D, tip, 0.0D, FLAME_TIP,
                    left[0], base, left[1], FLAME_MID, right[0], base, right[1], FLAME_MID);
        }
    }

    private static void triangle(VertexConsumer consumer, PoseStack.Pose pose,
            double x0, double y0, double z0, int color0, double x1, double y1, double z1, int color1,
            double x2, double y2, double z2, int color2) {
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x0, y0, z0, color0, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x1, y1, z1, color1, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x2, y2, z2, color2, 255);
    }

    private LegacySpecialAccessoryRenderer() {
    }
}
