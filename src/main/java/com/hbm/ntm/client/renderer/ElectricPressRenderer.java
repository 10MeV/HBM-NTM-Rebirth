package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ElectricPressBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricPressRenderer implements BlockEntityRenderer<ElectricPressBlockEntity> {
    private static final double LEGACY_IDLE_HEAD_Y = 1.875D;

    public ElectricPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ElectricPressBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ElectricPressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        float yRotation = 270.0F - facing.toYRot();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        ObjMachineModels.EPRESS_BODY.renderAll(ObjMachineModels.EPRESS_BODY_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, LEGACY_IDLE_HEAD_Y, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        ObjMachineModels.EPRESS_HEAD.renderAll(ObjMachineModels.EPRESS_HEAD_TEXTURE,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }
}
