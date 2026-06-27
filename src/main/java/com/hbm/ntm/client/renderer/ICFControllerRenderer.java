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

public class ICFControllerRenderer implements BlockEntityRenderer<ICFControllerBlockEntity> {
    public ICFControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ICFControllerBlockEntity blockEntity) {
        return false;
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
        LegacyTileRenderPlans.IcfLaserBeamPlan plan = LegacyTileRenderPlans.icfLaserBeamPlan(laserLength);
        if (!plan.active()) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyBeamRenderer.BeamPlan beam = LegacyBeamRenderer.beamPlan(
                facing.getStepX() * plan.laserLength(), 0.0D,
                facing.getStepZ() * plan.laserLength(),
                plan.beam().wave(), plan.beam().beamType(),
                plan.beam().outerColor(), plan.beam().innerColor(),
                plan.beam().start(), plan.beam().segments(), plan.beam().size(),
                plan.beam().layers(), plan.beam().thickness());
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> LegacyBeamRenderer.beam(queuedPose, buffer, beam));
        poseStack.popPose();
    }
}
