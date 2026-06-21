package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyEmitterBlock;
import com.hbm.ntm.blockentity.LegacyEmitterBlockEntity;
import com.hbm.ntm.client.obj.LegacyEmitterBeamRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class LegacyEmitterBlockEntityRenderer implements BlockEntityRenderer<LegacyEmitterBlockEntity> {
    public LegacyEmitterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LegacyEmitterBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(LegacyEmitterBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyEmitterBlock) || !state.hasProperty(LegacyEmitterBlock.FACING)) {
            return;
        }
        Level level = blockEntity.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        List<LegacyEmitterBeamRenderer.EmitterBeamPlan> plans = LegacyEmitterBeamRenderer.beamPlans(
                blockEntity.getBeam(), blockEntity.getGirth(), blockEntity.getEffect(), blockEntity.getColor(),
                gameTime, partialTick);
        if (plans.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(LegacyEmitterBeamRenderer.MODEL_CENTER_X, 0.0D,
                LegacyEmitterBeamRenderer.MODEL_CENTER_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(LegacyEmitterBeamRenderer.BASE_YAW_DEGREES));
        applyLegacyFacingTransform(poseStack, state.getValue(LegacyEmitterBlock.FACING));
        poseStack.translate(0.0D, LegacyEmitterBeamRenderer.FINAL_BEAM_OFFSET_Y,
                LegacyEmitterBeamRenderer.FINAL_BEAM_OFFSET_Z);
        LegacyEmitterBeamRenderer.renderPlans(plans, poseStack, buffer);
        poseStack.popPose();
    }

    private static void applyLegacyFacingTransform(PoseStack poseStack, Direction direction) {
        LegacyEmitterBeamRenderer.EmitterTransform transform =
                LegacyEmitterBeamRenderer.transformForMetadata(direction.get3DDataValue());
        poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
        if (transform.hasRotation()) {
            poseStack.mulPose(rotation(transform));
        }
    }

    private static Quaternionf rotation(LegacyEmitterBeamRenderer.EmitterTransform transform) {
        if (transform.axisX() != 0.0F) {
            return (transform.axisX() > 0.0F ? Axis.XP : Axis.XN).rotationDegrees(transform.angleDegrees());
        }
        if (transform.axisY() != 0.0F) {
            return (transform.axisY() > 0.0F ? Axis.YP : Axis.YN).rotationDegrees(transform.angleDegrees());
        }
        return (transform.axisZ() > 0.0F ? Axis.ZP : Axis.ZN).rotationDegrees(transform.angleDegrees());
    }
}
