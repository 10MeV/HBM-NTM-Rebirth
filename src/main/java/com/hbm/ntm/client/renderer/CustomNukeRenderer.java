package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CustomNukeRenderer implements BlockEntityRenderer<CustomNukeBlockEntity> {
    public CustomNukeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CustomNukeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(NuclearDeviceBlock.FACING)
                ? state.getValue(NuclearDeviceBlock.FACING)
                : Direction.SOUTH;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(NuclearDeviceBlock.legacyRenderYaw(NuclearDeviceBlock.Kind.BOY, facing)));
        NuclearDeviceRenderer.applyCustomNukeLegacyCommon(poseStack);
        NuclearDeviceRenderer.renderCustomNuke(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CustomNukeBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }
}
