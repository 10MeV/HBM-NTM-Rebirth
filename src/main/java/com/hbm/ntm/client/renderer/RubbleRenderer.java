package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class RubbleRenderer extends EntityRenderer<RubbleEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public RubbleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.shadowRadius = 0.2F;
    }

    @Override
    public void render(RubbleEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.45F, 0.45F, 0.45F);
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (entity.tickCount + partialTick) * 10.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, (entity.tickCount + partialTick) * 10.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, (entity.tickCount + partialTick) * 10.0F);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        HbmClientRenderUtil.renderSingleBlock(blockRenderer, entity.blockState(), poseStack, buffer, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RubbleEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
