package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ICFControllerBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ICFControllerRenderer implements BlockEntityRenderer<ICFControllerBlockEntity> {
    public ICFControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ICFControllerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ICFControllerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int laserLength = blockEntity.getLaserLength();
        if (laserLength <= 0) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyBeamRenderer.beam(poseStack, buffer, LegacyBeamRenderer.beamPlan(
                facing.getStepX() * (double) laserLength, 0.0D,
                facing.getStepZ() * (double) laserLength,
                LegacyBeamRenderer.WaveType.SPIRAL, LegacyBeamRenderer.BeamType.SOLID,
                LegacyTileRenderPlans.ICF_LASER_OUTER_COLOR,
                LegacyTileRenderPlans.ICF_LASER_INNER_COLOR,
                0, 1, 0.0F,
                LegacyTileRenderPlans.ICF_LASER_LAYERS,
                LegacyTileRenderPlans.ICF_LASER_THICKNESS));
        poseStack.popPose();
    }
}
