package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.BigAssTankBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.Bat9000BlockEntity;
import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.blockentity.OrbusBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.fluid.HbmFluids;
import com.mojang.blaze3d.vertex.PoseStack;
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
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && needsBlockEntityRenderer(blockEntity)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
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
        boolean bigAssTankTilted = isBigAssTankTilted(blockEntity, state);
        if (bigAssTankTilted) {
            poseStack.translate(0.0D, -1.0D, 0.0D);
            LegacyPoseRotations.rotateZDegrees(poseStack, 10.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 5.0F);
        }
        LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
        if (definition != null) {
            Vec3 translation = definition.modelTranslation(state);
            poseStack.translate(translation.x, translation.y, translation.z);
            LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));
        }

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            if (blockEntity instanceof OrbusBlockEntity orbus) {
                renderOrbus(orbus, partialTick, poseStack, buffer, modelLight, packedOverlay);
                poseStack.popPose();
                return;
            }

            if (blockEntity instanceof Bat9000BlockEntity) {
                if (!usesBakedBat9000Body(blockEntity)) {
                    ObjModelLibrary.MACHINE_BAT9000.renderAll(poseStack, buffer, modelLight, packedOverlay);
                }
                LegacyFluidTankRenderHelper.renderBat9000Fluid(blockEntity.getTank(), state, poseStack, buffer,
                        modelLight, packedOverlay);
                if (hasRenderableTankType(blockEntity)) {
                    var tankType = blockEntity.getTank().getTankType();
                    LegacyFluidTankRenderHelper.enqueueBat9000Diamonds(PresentStage.AFTER_BLOCK_ENTITIES,
                            tankType, poseStack, buffer, modelLight, packedOverlay);
                }
            } else if (blockEntity instanceof BigAssTankBlockEntity) {
                if (!usesBakedBigAssTankBody(blockEntity, state)) {
                    ObjModelLibrary.MACHINE_BIGASSTANK.renderAll(poseStack, buffer, modelLight, packedOverlay);
                }
                LegacyFluidTankRenderHelper.renderBigAssTankFluid(blockEntity.getTank(), state, poseStack,
                        buffer, modelLight, packedOverlay, blockEntity.getLevel() == null ? partialTick
                                : blockEntity.getLevel().getGameTime() + partialTick);
                if (hasRenderableTankType(blockEntity)) {
                    var tankType = blockEntity.getTank().getTankType();
                    LegacyFluidTankRenderHelper.enqueueBigAssTankDiamonds(PresentStage.AFTER_BLOCK_ENTITIES,
                            tankType, poseStack, buffer, modelLight, packedOverlay);
                }
            } else {
                if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                        || !blockEntity.usesSmallTankBakedModel()) {
                    LegacyFluidTankRenderHelper.renderSmallTankBody(ObjModelLibrary.MACHINE_FLUIDTANK,
                            ObjModelLibrary.MACHINE_FLUIDTANK_EXPLODED, blockEntity.getTank(),
                            blockEntity.isExploded(), poseStack, buffer, modelLight, packedOverlay);
                }
                if (hasRenderableTankType(blockEntity)) {
                    var tankType = blockEntity.getTank().getTankType();
                    LegacyFluidTankRenderHelper.enqueueSmallTankDiamonds(PresentStage.AFTER_BLOCK_ENTITIES,
                            tankType, poseStack, buffer, modelLight, packedOverlay);
                }
            }
        }
        poseStack.popPose();
    }

    private static boolean needsBlockEntityRenderer(FluidTankBlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        if (usesBakedBigAssTankBody(blockEntity, state)) {
            return hasRenderableTankType(blockEntity);
        }
        if (usesBakedBat9000Body(blockEntity)) {
            return hasRenderableTankType(blockEntity);
        }
        if (blockEntity.usesSmallTankBakedModel()
                && !LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return hasRenderableTankType(blockEntity);
        }
        return true;
    }

    private static boolean usesBakedBat9000Body(FluidTankBlockEntity blockEntity) {
        return blockEntity instanceof Bat9000BlockEntity
                && !LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
    }

    private static boolean usesBakedBigAssTankBody(FluidTankBlockEntity blockEntity, BlockState state) {
        return blockEntity instanceof BigAssTankBlockEntity
                && !LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                && !isBigAssTankTilted(blockEntity, state);
    }

    private static boolean isBigAssTankTilted(FluidTankBlockEntity blockEntity, BlockState state) {
        if (!(blockEntity instanceof BigAssTankBlockEntity bigAssTank)) {
            return false;
        }
        return state.hasProperty(BigAssTankBlock.TILTED) ? state.getValue(BigAssTankBlock.TILTED) : bigAssTank.isTilted();
    }

    private static boolean hasRenderableTankType(FluidTankBlockEntity blockEntity) {
        var tankType = blockEntity.getTank().getTankType();
        return tankType != null && tankType != HbmFluids.NONE;
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
            ObjEffectModels.renderSphereUvDynamicUntextured(poseStack, buffer,
                    color >> 16 & 255, color >> 8 & 255, color & 255, 255, false);
            poseStack.popPose();
        }

        ObjModelLibrary.MACHINE_ORBUS.renderAll(ObjModelLibrary.MACHINE_ORBUS.textureLocation(), poseStack, buffer,
                packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);

        if (fill <= 0 || scale <= 0.0D) {
            return;
        }
        long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        float beamScale = (float) scale;
        int randomStartA = (int) (gameTime / 2L % 1000L);
        int randomStartB = (int) (gameTime / 4L % 1000L);
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyMachineEffectPresenter.enqueueOrbusBeams(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                beamScale, randomStartA, randomStartB);
        poseStack.popPose();
    }
}
