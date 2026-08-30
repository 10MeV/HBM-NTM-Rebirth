package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKPanelBlock;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 RBMKMiniPanelBase inventory rendering plus the seven subclasses'
 * fixed OBJ overlays. Runtime data, labels, display panels, and blank displays
 * were not part of the legacy inventory path and are intentionally excluded.
 */
public final class RBMKPanelItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float ITEM_FIT_SCALE = 0.58F;

    public static final RBMKPanelItemRenderer INSTANCE = new RBMKPanelItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private RBMKPanelItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof RBMKPanelBlock panelBlock)
                || panelBlock.panelType() == RBMKPanelPlanner.PanelType.DISPLAY) {
            return;
        }

        poseStack.pushPose();
        applyItemTransform(displayContext, poseStack);
        renderPanelBase(panelBlock, poseStack, buffer, packedLight);

        // Exact local transforms from RBMKMiniPanelBase and each subclass'
        // renderInventoryBlock implementation.
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.5D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        switch (panelBlock.panelType()) {
            case GAUGE -> renderGaugeInventory(poseStack, buffer, packedLight, packedOverlay);
            case GRAPH, NUMITRON -> renderNumitronInventory(poseStack, buffer, packedLight, packedOverlay);
            case INDICATOR -> renderIndicatorInventory(poseStack, buffer, packedLight, packedOverlay);
            case KEYPAD -> renderKeypadInventory(poseStack, buffer, packedLight, packedOverlay);
            case LEVER -> renderLeverInventory(poseStack, buffer, packedLight, packedOverlay);
            case TERMINAL -> ObjRbmkModels.TERMINAL.renderAll(ObjRbmkModels.TERMINAL_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            case DISPLAY -> {
                // Explicitly excluded above; retained for exhaustive enum handling.
            }
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderPanelBase(RBMKPanelBlock panel, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        // The old inventory renderer drew the 0.25..1 X shell; WEST materializes that unrotated state.
        BlockState shell = panel.defaultBlockState().setValue(RBMKPanelBlock.FACING, Direction.WEST);
        HbmClientRenderUtil.renderSingleBlock(Minecraft.getInstance().getBlockRenderer(), shell, poseStack,
                buffer, packedLight);
        poseStack.popPose();
    }

    private static void renderGaugeInventory(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        for (int i = 0; i < LegacyRbmkPanelRenderer.GAUGE_COUNT; i++) {
            poseStack.pushPose();
            poseStack.translate(0.25D, (i / 2) * -0.5D + 0.25D, (i % 2) * -0.5D + 0.25D);
            ObjRbmkModels.GAUGE.renderPart("Gauge", ObjRbmkModels.GAUGE_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            poseStack.translate(0.0D, LegacyRbmkPanelRenderer.GAUGE_PIVOT_Y,
                    LegacyRbmkPanelRenderer.GAUGE_PIVOT_Z);
            LegacyPoseRotations.rotateXDegrees(poseStack, 85.0F);
            poseStack.translate(0.0D, -LegacyRbmkPanelRenderer.GAUGE_PIVOT_Y,
                    -LegacyRbmkPanelRenderer.GAUGE_PIVOT_Z);
            ObjRbmkModels.GAUGE.renderPart("Needle", ObjRbmkModels.GAUGE_TEXTURE,
                    poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay, 128, 0, 0, 255);
            poseStack.popPose();
        }
    }

    private static void renderNumitronInventory(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        for (int i = 0; i < LegacyRbmkPanelRenderer.NUMITRON_COUNT; i++) {
            poseStack.pushPose();
            poseStack.translate(0.25D, i * -0.5D + 0.25D, 0.0D);
            ObjRbmkModels.NUMITRON.renderAll(ObjRbmkModels.NUMITRON_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderIndicatorInventory(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        for (int i = 0; i < LegacyRbmkPanelRenderer.INDICATOR_COUNT; i++) {
            poseStack.pushPose();
            poseStack.translate(0.25D, (i / 2) * -0.3125D + 0.3125D, (i % 2) * 0.5D - 0.25D);
            ObjRbmkModels.INDICATOR.renderAll(ObjRbmkModels.INDICATOR_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderKeypadInventory(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        for (int i = 0; i < LegacyRbmkPanelRenderer.KEY_COUNT; i++) {
            poseStack.pushPose();
            poseStack.translate(0.25D, (i / 2) * -0.5D + 0.25D, (i % 2) * -0.5D + 0.25D);
            ObjRbmkModels.BUTTON.renderPart("Socket", ObjRbmkModels.KEYPAD_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            ObjRbmkModels.BUTTON.renderPart("Button", ObjRbmkModels.KEYPAD_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay, 166, 0, 0, 255);
            poseStack.popPose();
        }
    }

    private static void renderLeverInventory(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        for (int i = 0; i < LegacyRbmkPanelRenderer.LEVER_COUNT; i++) {
            poseStack.pushPose();
            poseStack.translate(0.25D, 0.0D, i * -0.5D + 0.25D);
            ObjRbmkModels.LEVER.renderPart("Base", ObjRbmkModels.LEVER_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            ObjRbmkModels.LEVER.renderPart("Lever", ObjRbmkModels.LEVER_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void applyItemTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(ITEM_FIT_SCALE, ITEM_FIT_SCALE, ITEM_FIT_SCALE);
            return;
        }

        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        float worldScale = ITEM_FIT_SCALE * 0.82F;
        poseStack.scale(worldScale, worldScale, worldScale);
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 0.1D, 0.0D);
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }
    }
}
