package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
import com.hbm.ntm.blockentity.DfcInjectorBlockEntity;
import com.hbm.ntm.blockentity.DfcReceiverBlockEntity;
import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DfcMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public DfcMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return blockEntity instanceof DfcEmitterBlockEntity emitter && emitter.getBeam() > 0
                || blockEntity instanceof DfcInjectorBlockEntity injector && injector.getBeam() > 0
                || blockEntity instanceof DfcStabilizerBlockEntity stabilizer && stabilizer.getBeam() > 0;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yawForPositiveZ(facing)));

        if (blockEntity instanceof DfcEmitterBlockEntity emitter) {
            renderModel(ObjMachineModels.DFC_EMITTER, poseStack, buffer, modelLight, packedOverlay);
            renderBeam(LegacyTileRenderPlans.dfcEmitterBeamPlan(emitter.getBeam(), gameTime(blockEntity)),
                    poseStack, buffer);
        } else if (blockEntity instanceof DfcReceiverBlockEntity) {
            renderModel(ObjMachineModels.DFC_RECEIVER, poseStack, buffer, modelLight, packedOverlay);
        } else if (blockEntity instanceof DfcInjectorBlockEntity injector) {
            renderModel(ObjMachineModels.DFC_INJECTOR, poseStack, buffer, modelLight, packedOverlay);
            renderBeam(LegacyTileRenderPlans.dfcInjectorBeamPlan(injector.getBeam(),
                    injector.getFuel1().getFill(), injector.getFuel1().getTankType().getColor(),
                    injector.getFuel2().getFill(), injector.getFuel2().getTankType().getColor(),
                    gameTime(blockEntity)), poseStack, buffer);
        } else if (blockEntity instanceof DfcStabilizerBlockEntity stabilizer) {
            renderModel(ObjMachineModels.DFC_INJECTOR, ObjMachineModels.DFC_STABILIZER_TEXTURE,
                    poseStack, buffer, modelLight, packedOverlay);
            renderBeam(LegacyTileRenderPlans.dfcStabilizerBeamPlan(stabilizer.getBeam(), gameTime(blockEntity)),
                    poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void renderModel(LegacyWavefrontModel model, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderModel(model, model.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderModel(LegacyWavefrontModel model, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
    }

    private static void renderBeam(LegacyTileRenderPlans.DfcBeamPlan plan, PoseStack poseStack,
            MultiBufferSource buffer) {
        for (LegacyTileRenderPlans.TranslatedBeamPlan beam : plan.beams()) {
            poseStack.pushPose();
            poseStack.translate(beam.translateX(), beam.translateY(), beam.translateZ());
            LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                    queuedPose -> LegacyBeamRenderer.beam(queuedPose, buffer, beam.beam()));
            poseStack.popPose();
        }
    }

    private static long gameTime(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        return level == null ? 0L : level.getGameTime();
    }

    private static float yawForPositiveZ(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case EAST -> 90.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
