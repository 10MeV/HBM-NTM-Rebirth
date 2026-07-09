package com.hbm.ntm.client.renderer;

import com.hbm.ntm.entity.item.LegacyFallingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyFallingBlockRenderer extends EntityRenderer<LegacyFallingBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public LegacyFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LegacyFallingBlockEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.blockState();
        if (!state.isAir() && entity.level().getBlockState(entity.blockPosition()).getBlock() != state.getBlock()) {
            poseStack.pushPose();
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            HbmClientRenderUtil.renderSingleBlock(blockRenderer, state, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyFallingBlockEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
