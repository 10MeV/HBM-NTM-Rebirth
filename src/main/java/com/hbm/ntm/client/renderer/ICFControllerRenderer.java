package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ICFControllerBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ICFControllerRenderer implements BlockEntityRenderer<ICFControllerBlockEntity> {
    public ICFControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ICFControllerBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(ICFControllerBlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity.getLaserLength() > 0
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(ICFControllerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int laserLength = blockEntity.getLaserLength();
        if (laserLength <= 0) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            BlockState state = blockEntity.getBlockState();
            Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                    ? state.getValue(HorizontalMachineBlock.FACING)
                    : Direction.NORTH;
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            double beamX = facing.getStepX() * (double) laserLength;
            double beamZ = facing.getStepZ() * (double) laserLength;
            LegacyMachineEffectPresenter.enqueueSolidBeam(PresentStage.AFTER_LEVEL, poseStack,
                    buffer, false, beamX, 0.0D, beamZ,
                    LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyTileRenderPlans.ICF_LASER_OUTER_COLOR,
                    LegacyTileRenderPlans.ICF_LASER_INNER_COLOR,
                    0, 1, 0.0F,
                    LegacyTileRenderPlans.ICF_LASER_LAYERS,
                    LegacyTileRenderPlans.ICF_LASER_THICKNESS);
            poseStack.popPose();
        }
    }
}
