package com.hbm.ntm.client.renderer;

import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class LegacyPrimedExplosiveRenderer extends EntityRenderer<LegacyPrimedExplosiveEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public LegacyPrimedExplosiveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(LegacyPrimedExplosiveEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        int fuse = entity.fuse();
        if ((float) fuse - partialTick + 1.0F < 10.0F) {
            float pulse = 1.0F - ((float) fuse - partialTick + 1.0F) / 10.0F;
            pulse = Mth.clamp(pulse, 0.0F, 1.0F);
            pulse *= pulse;
            pulse *= pulse;
            float scale = 1.0F + pulse * 0.3F;
            poseStack.scale(scale, scale, scale);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        blockRenderer.renderSingleBlock(entity.blockState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyPrimedExplosiveEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
