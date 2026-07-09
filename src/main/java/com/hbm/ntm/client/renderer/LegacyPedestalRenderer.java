package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.LegacyPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class LegacyPedestalRenderer implements BlockEntityRenderer<LegacyPedestalBlockEntity> {
    public LegacyPedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRender(LegacyPedestalBlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity.hasItem()
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(LegacyPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getItem();
        if (stack.isEmpty() || !LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        double bob = Math.sin(((player == null ? 0 : player.tickCount) + partialTick) * 0.1D) * 0.0625D;

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.scale(1.5F, 1.5F, 1.5F);

        if (isLegacy3dBlockItem(stack)) {
            poseStack.translate(0.0D, bob + 0.0625D, 0.0D);
        } else {
            poseStack.translate(0.0D, 0.125D + bob, 0.0D);
            if (player != null) {
                float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());
                poseStack.mulPose(Axis.YN.rotationDegrees(yaw + 180.0F));
            }
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0);
        poseStack.popPose();
    }

    private static boolean isLegacy3dBlockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(blockItem.getBlock().defaultBlockState());
        return model.isGui3d();
    }
}
