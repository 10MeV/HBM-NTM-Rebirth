package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.blockentity.LegacyFanBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyFanRenderer implements BlockEntityRenderer<LegacyFanBlockEntity> {
    public LegacyFanRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(LegacyFanBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyFanBlock) || !state.hasProperty(LegacyFanBlock.FACING)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        applyLegacyFacingTransform(poseStack, state.getValue(LegacyFanBlock.FACING));
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL);
        ObjMachineModels.FAN_LEGACY.renderPart("Frame", ObjMachineModels.FAN_TEXTURE, context);
        poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.spin(partialTick)));
        ObjMachineModels.FAN_LEGACY.renderPart("Blades", ObjMachineModels.FAN_TEXTURE, context);
        poseStack.popPose();
    }

    public static void renderItemModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, BlockState state) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withRenderMode(LegacyTexturedRenderMode.CUTOUT_CULL);
        ObjMachineModels.FAN_LEGACY.renderAll(ObjMachineModels.FAN_TEXTURE, context);
    }

    private static void applyLegacyFacingTransform(PoseStack poseStack, Direction direction) {
        poseStack.translate(0.0D, 0.5D, 0.0D);
        switch (direction) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            default -> {
            }
        }
        poseStack.translate(0.0D, -0.5D, 0.0D);
    }
}
