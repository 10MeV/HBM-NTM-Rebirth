package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.client.obj.ObjBlockEntityAnimation;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BasicMachineRenderer implements BlockEntityRenderer<BasicMachineBlockEntity> {
    private static final ObjBlockEntityAnimation<BasicMachineBlockEntity> PRESS_HEAD_ANIMATION = (blockEntity, partialTick, poseStack) -> {
        double press = Math.max(0.0D, Math.min(1.0D, blockEntity.getInterpolatedPress(partialTick) / (double) BasicMachineBlockEntity.MAX_PRESS));
        poseStack.translate(0.0D, (1.0D - press) * 0.875D, 0.0D);
    };

    public BasicMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(BasicMachineBlockEntity blockEntity) {
        return true;
    }

    @Override
    public void render(BasicMachineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double press = Math.max(0.0D, Math.min(1.0D, blockEntity.getInterpolatedPress(partialTick) / (double) BasicMachineBlockEntity.MAX_PRESS));
        BlockState state = blockEntity.getBlockState();
        float facingRotation = state.hasProperty(HorizontalMachineBlock.FACING)
                ? 180.0F - state.getValue(HorizontalMachineBlock.FACING).toYRot()
                : 0.0F;

        poseStack.pushPose();
        LegacyObjTransforms.rotateAroundBlockCenterY(poseStack, facingRotation);
        PRESS_HEAD_ANIMATION.apply(blockEntity, partialTick, poseStack);
        ObjModelLibrary.PRESS.renderPart("Head", new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay));
        poseStack.popPose();

        ItemStack stack = blockEntity.getRenderStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.896875D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }
}
