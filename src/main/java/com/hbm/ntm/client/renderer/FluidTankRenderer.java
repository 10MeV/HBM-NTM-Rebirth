package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.Bat9000BlockEntity;
import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.blockentity.OrbusBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FluidTankRenderer<T extends FluidTankBlockEntity> implements BlockEntityRenderer<T> {
    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(blockEntity);
        BlockState state = blockEntity.getBlockState();
        LegacyMachineDefinition definition = state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block
                ? block.definition()
                : null;
        float rotation = definition != null
                ? definition.yRotation(state)
                : state.hasProperty(HorizontalMachineBlock.FACING)
                ? (360.0F - state.getValue(HorizontalMachineBlock.FACING).toYRot()) % 360.0F
                : 180.0F;
        int modelLight = definition != null
                ? LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight)
                : LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (blockEntity instanceof BigAssTankBlockEntity bigAssTank && bigAssTank.isTilted()) {
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(5.0F));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        if (definition != null) {
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));
        }

        if (blockEntity instanceof OrbusBlockEntity orbus) {
            renderOrbus(orbus, partialTick, poseStack, buffer, modelLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        LegacyTileRenderPlans.FluidTankModelPlan plan = LegacyTileRenderPlans.fluidTankModelPlan(
                blockEntity instanceof Bat9000BlockEntity,
                blockEntity instanceof BigAssTankBlockEntity,
                blockEntity.isExploded());
        switch (plan.kind()) {
            case BAT9000 -> {
                ObjModelLibrary.MACHINE_BAT9000.renderAll(poseStack, buffer, modelLight, packedOverlay);
                if (plan.renderFluidBody()) {
                    LegacyFluidTankRenderHelper.renderBat9000Fluid(blockEntity.getTank(), state, poseStack, buffer,
                            modelLight, packedOverlay);
                }
                if (plan.renderDangerDiamonds()) {
                    var tankType = blockEntity.getTank().getTankType();
                    LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                            queuedPose -> LegacyFluidTankRenderHelper.renderBat9000Diamonds(tankType,
                                    queuedPose, buffer, modelLight, packedOverlay));
                }
            }
            case BIG_ASS_TANK -> {
                ObjModelLibrary.MACHINE_BIGASSTANK.renderAll(poseStack, buffer, modelLight, packedOverlay);
                if (plan.renderFluidBody()) {
                    LegacyFluidTankRenderHelper.renderBigAssTankFluid(blockEntity.getTank(), state, poseStack, buffer,
                            modelLight, packedOverlay, blockEntity.getLevel() == null ? partialTick
                                    : blockEntity.getLevel().getGameTime() + partialTick);
                }
                if (plan.renderDangerDiamonds()) {
                    var tankType = blockEntity.getTank().getTankType();
                    LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                            queuedPose -> LegacyFluidTankRenderHelper.renderBigAssTankDiamonds(tankType,
                                    queuedPose, buffer, modelLight, packedOverlay));
                }
            }
            case SMALL_TANK -> {
                LegacyFluidTankRenderHelper.renderSmallTankBody(ObjModelLibrary.MACHINE_FLUIDTANK,
                        ObjModelLibrary.MACHINE_FLUIDTANK_EXPLODED, blockEntity.getTank(), plan.exploded(),
                        poseStack, buffer, modelLight, packedOverlay);
                var tankType = blockEntity.getTank().getTankType();
                LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                        queuedPose -> LegacyFluidTankRenderHelper.renderSmallTankDiamonds(tankType,
                                queuedPose, buffer, modelLight, packedOverlay));
            }
        }
        poseStack.popPose();
    }

    private static void renderOrbus(OrbusBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int fill = blockEntity.getTank().getFill();
        int maxFill = blockEntity.getTank().getMaxFill();
        double scale = maxFill <= 0 ? 0.0D : (double) fill / (double) maxFill;
        if (fill > 0 && scale > 0.0D) {
            int color = blockEntity.getTank().getTankType().getColor();
            poseStack.pushPose();
            double worldTime = blockEntity.getLevel() == null
                    ? partialTick
                    : blockEntity.getLevel().getGameTime() + partialTick;
            poseStack.translate(0.0D, 2.5D + Math.sin(worldTime * 0.1D) * 0.125D * scale, 0.0D);
            poseStack.scale((float) scale, (float) scale, (float) scale);
            ObjModelLibrary.EFFECT_SPHERE_UV.renderAllUntextured(poseStack, buffer,
                    color >> 16 & 255, color >> 8 & 255, color & 255, 255);
            poseStack.popPose();
        }

        ObjModelLibrary.MACHINE_ORBUS.renderAll(ObjModelLibrary.MACHINE_ORBUS.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);

        if (fill <= 0 || scale <= 0.0D) {
            return;
        }
        long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        LegacyBeamRenderer.BeamPlan spiralBeam = LegacyBeamRenderer.beamPlan(
                0.0D, 3.0D, 0.0D, LegacyBeamRenderer.WaveType.SPIRAL, LegacyBeamRenderer.BeamType.SOLID,
                0x101020, 0x101020, 0, 1, 0.0F, 6, (float) scale * 0.5F);
        LegacyBeamRenderer.BeamPlan randomBeamA = LegacyBeamRenderer.beamPlan(
                0.0D, 3.0D, 0.0D, LegacyBeamRenderer.WaveType.RANDOM, LegacyBeamRenderer.BeamType.SOLID,
                0x202060, 0x202060, (int) (gameTime / 2L % 1000L), 6, (float) scale, 2,
                0.0625F * (float) scale);
        LegacyBeamRenderer.BeamPlan randomBeamB = LegacyBeamRenderer.beamPlan(
                0.0D, 3.0D, 0.0D, LegacyBeamRenderer.WaveType.RANDOM, LegacyBeamRenderer.BeamType.SOLID,
                0x202060, 0x202060, (int) (gameTime / 4L % 1000L), 6, (float) scale, 2,
                0.0625F * (float) scale);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
            LegacyBeamRenderer.beam(queuedPose, buffer, spiralBeam);
            LegacyBeamRenderer.beam(queuedPose, buffer, randomBeamA);
            LegacyBeamRenderer.beam(queuedPose, buffer, randomBeamB);
        });
        poseStack.popPose();
    }
}
