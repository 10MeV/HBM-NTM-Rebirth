package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyPoleSatelliteReceiverBlock;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.PoleSatelliteReceiverBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Direct modern carrier for {@code ItemRenderSatelliteReceiver}. */
public final class PoleSatelliteReceiverItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/pole_satellite_receiver.png");
    private static final ResourceLocation FALLBACK_ICON = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/block/deco_satellite_receiver.png");
    public static final PoleSatelliteReceiverItemRenderer INSTANCE = new PoleSatelliteReceiverItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private PoleSatelliteReceiverItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof PoleSatelliteReceiverBlockItem item)
                || !(item.getBlock() instanceof LegacyPoleSatelliteReceiverBlock)) {
            return;
        }

        poseStack.pushPose();
        if (applyLegacyCustomDisplay(displayContext, poseStack)) {
            applyBakedWorldRootInverse(poseStack);
            ObjBlockModels.POLE_SATELLITE_RECEIVER.renderAll(TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        } else {
            renderLegacyBlockIcon(poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static boolean applyLegacyCustomDisplay(ItemDisplayContext context, PoseStack poseStack) {
        // ItemRenderSatelliteReceiver explicitly handled ENTITY, EQUIPPED and
        // EQUIPPED_FIRST_PERSON. FIXED is the 1.20 carrier for old item-frame ENTITY.
        if (context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            return true;
        }
        if (context.firstPerson()) {
            LegacyPoseRotations.rotateZDegrees(poseStack, -135.0F);
            poseStack.translate(-0.6D, -0.6D, -0.1D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            return true;
        }
        if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.8D, -0.3D, 0.2D);
            LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
            return true;
        }
        return false;
    }

    private static void applyBakedWorldRootInverse(PoseStack poseStack) {
        // The shared OBJ has RenderPoleSatelliteReceiver's T(.5, 1.5, .5) * Rz(180)
        // world root baked into its vertices. ItemRenderSatelliteReceiver rendered raw
        // Techne geometry, so custom legacy item poses need its inverse before this OBJ.
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.translate(-0.5D, -1.5D, -0.5D);
    }

    private static void renderLegacyBlockIcon(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        // The legacy handler rejected INVENTORY. DecoPoleSatelliteReceiver's -1 render
        // type therefore selected RenderItem's ordinary 16x16 block-atlas icon path.
        // HEAD/NONE have no 1.7.10 IItemRenderer equivalent, so they use that same
        // rejected-handler fallback rather than borrowing a custom OBJ pose.
        poseStack.translate(0.0D, 0.0D, 0.5D);
        LegacyTexturedQuadRenderer.quadDirect(FALLBACK_ICON, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, 0.0F, 1.0F,
                0.0D, 1.0D, 0.0D, 0.0D, 1.0D,
                1.0D, 1.0D, 0.0D, 1.0D, 1.0D,
                1.0D, 0.0D, 0.0D, 1.0D, 0.0D,
                0.0D, 0.0D, 0.0D, 0.0D, 0.0D,
                0xFFFFFF, 255);
    }
}
