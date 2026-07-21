package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.ClientMuzzleFlashEffects;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.SednaGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Consumer for the old remote {@code ItemRenderWeaponBase.flashMap} contract. It mirrors vanilla's main-hand
 * {@code ItemInHandLayer} hand matrix, then delegates the source-local muzzle pose to the Sedna OBJ renderer.
 */
public final class SednaRemoteMuzzleFlashLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public SednaRemoteMuzzleFlashLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch) {
        long shotMillis = ClientMuzzleFlashEffects.lastFlashMillis(player);
        ItemStack stack = player.getMainHandItem();
        if (shotMillis <= 0L || !(stack.getItem() instanceof SednaGunItem)) {
            return;
        }

        HumanoidArm mainArm = player.getMainArm();
        if (SednaGunItemRenderer.isAkimbo(stack)) {
            renderReceiver(poseStack, buffer, player, HumanoidArm.LEFT, shotMillis, stack);
            renderReceiver(poseStack, buffer, player, HumanoidArm.RIGHT, shotMillis, stack);
        } else {
            renderReceiver(poseStack, buffer, player, mainArm, shotMillis, stack);
        }
    }

    private void renderReceiver(PoseStack poseStack, MultiBufferSource buffer, AbstractClientPlayer player, HumanoidArm arm,
            long shotMillis, ItemStack stack) {
        boolean leftHand = arm == HumanoidArm.LEFT;
        ItemDisplayContext displayContext = leftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        poseStack.pushPose();
        getParentModel().translateToHand(arm, poseStack);
        LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(leftHand ? -1.0D / 16.0D : 1.0D / 16.0D, 0.125D, -0.625D);
        SednaGunItemRenderer.renderRemoteMuzzleFlash(stack, displayContext, shotMillis, poseStack, buffer);
        poseStack.popPose();
    }
}
