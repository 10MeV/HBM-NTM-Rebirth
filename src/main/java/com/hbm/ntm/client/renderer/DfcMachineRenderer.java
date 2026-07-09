package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.DfcMachineBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
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
import net.minecraft.world.phys.Vec3;

public class DfcMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public DfcMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return hasVisibleBeam(blockEntity);
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return hasBerVisuals(blockEntity)
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        boolean renderStaticBody = LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
        boolean renderBeam = hasVisibleBeam(blockEntity);
        if (!renderStaticBody && !renderBeam) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(DfcMachineBlock.FACING)
                ? state.getValue(DfcMachineBlock.FACING)
                : Direction.SOUTH;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        applyLegacyDfcFacing(poseStack, facing);

        if (renderStaticBody) {
            int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
                if (blockEntity instanceof DfcEmitterBlockEntity) {
                    renderModel(ObjMachineModels.DFC_EMITTER, poseStack, buffer, modelLight, packedOverlay);
                } else if (blockEntity instanceof DfcReceiverBlockEntity) {
                    renderModel(ObjMachineModels.DFC_RECEIVER, poseStack, buffer, modelLight, packedOverlay);
                } else if (blockEntity instanceof DfcInjectorBlockEntity) {
                    renderModel(ObjMachineModels.DFC_INJECTOR, poseStack, buffer, modelLight, packedOverlay);
                } else if (blockEntity instanceof DfcStabilizerBlockEntity) {
                    renderModel(ObjMachineModels.DFC_INJECTOR, ObjMachineModels.DFC_STABILIZER_TEXTURE,
                            poseStack, buffer, modelLight, packedOverlay);
                }
            }
        }
        if (blockEntity instanceof DfcEmitterBlockEntity emitter && emitter.getBeam() > 0) {
            renderEmitterBeam(emitter.getBeam(), gameTime(blockEntity), poseStack, buffer);
        } else if (blockEntity instanceof DfcInjectorBlockEntity injector && hasVisibleInjectorBeam(injector)) {
            renderInjectorBeam(injector.getBeam(),
                    injector.getFuel1().getFill(), injector.getFuel1().getTankType().getColor(),
                    injector.getFuel2().getFill(), injector.getFuel2().getTankType().getColor(),
                    gameTime(blockEntity), poseStack, buffer);
        } else if (blockEntity instanceof DfcStabilizerBlockEntity stabilizer && stabilizer.getBeam() > 0) {
            renderStabilizerBeam(stabilizer.getBeam(), gameTime(blockEntity), poseStack, buffer);
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

    private static void renderEmitterBeam(int range, long worldTime, PoseStack poseStack, MultiBufferSource buffer) {
        if (range <= 0) {
            return;
        }
        int randomStart = (int) (worldTime % 1000L);
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.DFC_BEAM_TRANSLATE_Y, 0.0D);
        LegacyMachineEffectPresenter.enqueueSolidBeamGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                true, beams -> {
            beams.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_EMITTER_DEPTH_COLOR,
                    LegacyTileRenderPlans.DFC_EMITTER_DEPTH_COLOR, 0, 1, 0.0F, 2, 0.0625F);
            beams.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.RANDOM, LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR,
                    LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR, randomStart, range * 2, 0.125F, 4, 0.0625F);
            beams.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.RANDOM, LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR,
                    LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR, randomStart + 1, range * 2, 0.125F, 4, 0.0625F);
        });
        poseStack.popPose();
    }

    private static void renderInjectorBeam(int range, int tank0Fill, int tank0Color, int tank1Fill,
            int tank1Color, long worldTime, PoseStack poseStack, MultiBufferSource buffer) {
        boolean renderTank0 = range > 0 && tank0Fill > 0;
        boolean renderTank1 = range > 0 && tank1Fill > 0;
        if (!renderTank0 && !renderTank1) {
            return;
        }
        int randomStart = (int) (worldTime % 1000L);
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.DFC_BEAM_TRANSLATE_Y, 0.0D);
        LegacyMachineEffectPresenter.enqueueLineBeamGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                beams -> {
            if (renderTank0) {
                beams.add(0.0D, 0.0D, range,
                        LegacyBeamRenderer.WaveType.RANDOM, tank0Color,
                        LegacyTileRenderPlans.DFC_INJECTOR_INNER_COLOR, randomStart, range, 0.0625F);
            }
            if (renderTank1) {
                beams.add(0.0D, 0.0D, range,
                        LegacyBeamRenderer.WaveType.RANDOM, tank1Color,
                        LegacyTileRenderPlans.DFC_INJECTOR_INNER_COLOR, randomStart + 1, range, 0.0625F);
            }
        });
        poseStack.popPose();
    }

    private static void renderStabilizerBeam(int range, long worldTime, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (range <= 0) {
            return;
        }
        int fastStart = (int) (worldTime * -25L % 360L);
        int midStart = (int) (worldTime * -15L % 360L) + 180;
        int slowStart = (int) (worldTime * -5L % 360L) + 180;
        int segments = range * 3;
        poseStack.pushPose();
        poseStack.translate(0.0D, LegacyTileRenderPlans.DFC_BEAM_TRANSLATE_Y, 0.0D);
        LegacyMachineEffectPresenter.enqueueLineBeamGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                beams -> {
            beams.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_STABILIZER_OUTER_COLOR,
                    LegacyTileRenderPlans.DFC_STABILIZER_INNER_COLOR, fastStart, segments, 0.125F);
            beams.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_STABILIZER_OUTER_COLOR,
                    LegacyTileRenderPlans.DFC_STABILIZER_INNER_COLOR, midStart, segments, 0.125F);
            beams.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_STABILIZER_OUTER_COLOR,
                    LegacyTileRenderPlans.DFC_STABILIZER_INNER_COLOR, slowStart, segments, 0.125F);
        });
        poseStack.popPose();
    }

    private static long gameTime(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        return level == null ? 0L : level.getGameTime();
    }

    private static boolean hasBerVisuals(BlockEntity blockEntity) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer() || hasVisibleBeam(blockEntity);
    }

    private static boolean hasVisibleBeam(BlockEntity blockEntity) {
        return blockEntity instanceof DfcEmitterBlockEntity emitter && emitter.getBeam() > 0
                || blockEntity instanceof DfcInjectorBlockEntity injector && hasVisibleInjectorBeam(injector)
                || blockEntity instanceof DfcStabilizerBlockEntity stabilizer && stabilizer.getBeam() > 0;
    }

    private static boolean hasVisibleInjectorBeam(DfcInjectorBlockEntity injector) {
        return injector.getBeam() > 0
                && (injector.getFuel1().getFill() > 0 || injector.getFuel2().getFill() > 0);
    }

    private static void applyLegacyDfcFacing(PoseStack poseStack, Direction facing) {
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0.0D, 0.5D, -0.5D);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
            case UP -> {
                poseStack.translate(0.0D, 0.5D, 0.5D);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            }
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            case EAST -> {
            }
        }
    }
}
