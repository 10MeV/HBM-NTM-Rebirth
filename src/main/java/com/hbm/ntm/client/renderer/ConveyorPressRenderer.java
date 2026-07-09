package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.ConveyorPressBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ConveyorPressRenderer implements BlockEntityRenderer<ConveyorPressBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle PRESS =
            ObjMachineModels.CONVEYOR_PRESS.prepareRenderOnlyInCallOrder("Press");
    private static final LegacyWavefrontModel.SelectionHandle PISTON =
            ObjMachineModels.CONVEYOR_PRESS.prepareRenderOnlyInCallOrder("Piston");
    private static final LegacyWavefrontModel.SelectionHandle BELT =
            ObjMachineModels.CONVEYOR_PRESS.prepareRenderOnlyInCallOrder("Belt");
    private static final int BELT_PERIOD_TICKS = LegacyTileRenderPlans.CONVEYOR_PRESS_BELT_PERIOD_TICKS;
    private static final int BELT_TICK_OFFSET = LegacyTileRenderPlans.CONVEYOR_PRESS_BELT_TICK_OFFSET;
    private static final LegacyWavefrontModel.UvTransform[] BELT_UV_TRANSFORMS = createBeltUvTransforms();

    public ConveyorPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ConveyorPressBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(ConveyorPressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        float yRotation = 270.0F - facing.toYRot();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        long worldTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        boolean hasStamp = !blockEntity.getStamp().isEmpty();
        double pistonTranslateY = -blockEntity.getInterpolatedPress(partialTick)
                * LegacyTileRenderPlans.CONVEYOR_PRESS_PISTON_TRAVEL;
        LegacyWavefrontModel.UvTransform beltTransform = beltTransform(worldTime);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity);
                var ignored = LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.0D, 0.5D);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
                if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                    ObjMachineModels.CONVEYOR_PRESS.renderOnlyInCallOrder(ObjMachineModels.CONVEYOR_PRESS_TEXTURE,
                            poseStack, buffer, modelLight, OverlayTexture.NO_OVERLAY, PRESS,
                            LegacyTexturedRenderMode.CUTOUT_CULL);
                }

                if (hasStamp) {
                    poseStack.pushPose();
                    poseStack.translate(0.0D, pistonTranslateY, 0.0D);
                    ObjMachineModels.CONVEYOR_PRESS.renderOnlyInCallOrder(ObjMachineModels.CONVEYOR_PRESS_TEXTURE,
                            poseStack, buffer, modelLight, OverlayTexture.NO_OVERLAY, PISTON,
                            LegacyTexturedRenderMode.CUTOUT_CULL);
                    poseStack.popPose();
                }

                ObjMachineModels.CONVEYOR_PRESS.renderOnlyInCallOrder(
                        ObjMachineModels.CONVEYOR_PRESS_BELT_TEXTURE, poseStack, buffer, modelLight,
                        OverlayTexture.NO_OVERLAY, 255, 255, 255, 255, false,
                        LegacyTexturedRenderMode.CUTOUT_CULL, beltTransform, BELT);
                poseStack.popPose();
            }
        }
    }

    private static LegacyWavefrontModel.UvTransform beltTransform(long worldTime) {
        return BELT_UV_TRANSFORMS[(int) (worldTime % BELT_PERIOD_TICKS)];
    }

    private static LegacyWavefrontModel.UvTransform[] createBeltUvTransforms() {
        LegacyWavefrontModel.UvTransform[] transforms =
                new LegacyWavefrontModel.UvTransform[BELT_PERIOD_TICKS];
        for (int tick = 0; tick < transforms.length; tick++) {
            int legacyTicks = tick + BELT_TICK_OFFSET;
            float beltTranslateV = (float) ((double) legacyTicks / BELT_PERIOD_TICKS);
            transforms[tick] = LegacyWavefrontModel.UvTransform.dynamic(
                    1.0F, 0.0F, 0.0F, 1.0F,
                    0.0F,
                    beltTranslateV,
                    0.0F);
        }
        return transforms;
    }
}
