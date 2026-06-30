package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ExposureChamberBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ExposureChamberRenderer implements BlockEntityRenderer<ExposureChamberBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_EXPOSURE_CHAMBER;
    private static final LegacyWavefrontModel.SelectionHandle CHAMBER =
            MODEL.prepareRenderOnlyInCallOrder("Chamber");
    private static final LegacyWavefrontModel.SelectionHandle MAGNETS =
            MODEL.prepareRenderOnlyInCallOrder("Magnets");
    private static final LegacyWavefrontModel.SelectionHandle CORE =
            MODEL.prepareRenderOnlyInCallOrder("Core");

    public ExposureChamberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ExposureChamberBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ExposureChamberBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(blockEntity);
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        Level level = blockEntity.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        LegacyTileRenderPlans.ExposureChamberPlan plan = LegacyTileRenderPlans.exposureChamberPlan(
                blockEntity.isOn(), blockEntity.getPrevRotation(), blockEntity.getRawRotation(),
                gameTime, System.currentTimeMillis(), partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                CHAMBER);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.rotationDegrees()));
        MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                MAGNETS);
        poseStack.popPose();

        if (plan.on()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) plan.coreRotationDegrees()));
            poseStack.translate(0.0D, plan.coreBobY(), 0.0D);
            MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, LightTexture.FULL_BRIGHT,
                    packedOverlay, CORE);
            poseStack.popPose();

            for (LegacyTileRenderPlans.TranslatedBeamPlan beam : plan.beams()) {
                LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
                    queuedPose.translate(beam.translateX(), beam.translateY(), beam.translateZ());
                    LegacyBeamRenderer.beam(queuedPose, buffer, beam.beam());
                });
            }
        }

        poseStack.popPose();
    }
}
