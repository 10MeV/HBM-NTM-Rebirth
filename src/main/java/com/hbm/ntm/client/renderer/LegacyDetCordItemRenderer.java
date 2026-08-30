package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyDetCordBlock;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LegacyDetCordBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Source-backed item equivalent of {@code RenderDetCord#renderInventoryBlock}. */
public final class LegacyDetCordItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation DET_CORD_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/det_cord.png");
    private static final LegacyWavefrontModel.SelectionHandle CZ =
            ObjBlockModels.CABLE_NEO.prepareRenderOnlyInCallOrder("CZ");

    public static final LegacyDetCordItemRenderer INSTANCE = new LegacyDetCordItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LegacyDetCordItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LegacyDetCordBlockItem item)
                || !(item.getBlock() instanceof LegacyDetCordBlock)) {
            return;
        }

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            // Modern equivalent of the RenderBlocks inventory camera, followed by old renderer transforms.
            poseStack.translate(0.5D, 0.5D, 0.5D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            poseStack.scale(1.25F, 1.25F, 1.25F);
        } else {
            poseStack.translate(0.5D, 0.25D, 0.0D);
            poseStack.scale(0.25F, 0.25F, 0.25F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        }
        ObjBlockModels.CABLE_NEO.renderOnlyInCallOrder(DET_CORD_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay, CZ, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }
}
