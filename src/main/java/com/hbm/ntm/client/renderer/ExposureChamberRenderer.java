package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.ExposureChamberBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ExposureChamberRenderer implements BlockEntityRenderer<ExposureChamberBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_EXPOSURE_CHAMBER;
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
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ExposureChamberBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(ExposureChamberBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        Level level = blockEntity.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        long currentMillis = System.currentTimeMillis();
        boolean on = blockEntity.isOn();
        double rotation = blockEntity.getPrevRotation()
                + (blockEntity.getRawRotation() - blockEntity.getPrevRotation()) * partialTick;
        double coreBob = on
                ? Math.sin(((double) gameTime % LegacyTileRenderPlans.EXPOSURE_CORE_BOB_PERIOD + partialTick)
                        * LegacyTileRenderPlans.EXPOSURE_CORE_BOB_SPEED)
                        * LegacyTileRenderPlans.EXPOSURE_CORE_BOB_AMOUNT
                : 0.0D;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            poseStack.pushPose();
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) rotation);
            MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay,
                    MAGNETS);
            poseStack.popPose();

            if (on) {
                poseStack.pushPose();
                LegacyPoseRotations.rotateYDegrees(poseStack, (float) (rotation / 2.0D));
                poseStack.translate(0.0D, coreBob, 0.0D);
                MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, LightTexture.FULL_BRIGHT,
                        packedOverlay, CORE);
                poseStack.popPose();
            }
        }

        if (on) {
            enqueueExposureBeams(poseStack, buffer, gameTime, currentMillis);
        }

        poseStack.popPose();
    }

    private static void enqueueExposureBeams(PoseStack poseStack, MultiBufferSource buffer, long gameTime,
            long currentMillis) {
        int randomColor = gameTime % LegacyTileRenderPlans.EXPOSURE_RANDOM_DURATION
                >= LegacyTileRenderPlans.EXPOSURE_RANDOM_DURATION / 2
                ? LegacyTileRenderPlans.EXPOSURE_RANDOM_BLUE_COLOR
                : LegacyTileRenderPlans.EXPOSURE_RANDOM_WHITE_COLOR;
        Random random = LegacyRenderRandom.seeded(gameTime / LegacyTileRenderPlans.EXPOSURE_RANDOM_DURATION);
        random.nextInt(LegacyTileRenderPlans.EXPOSURE_RANDOM_CHANCE);
        boolean randomTop = random.nextInt(LegacyTileRenderPlans.EXPOSURE_RANDOM_CHANCE) == 0;
        boolean randomRight = random.nextInt(LegacyTileRenderPlans.EXPOSURE_RANDOM_CHANCE) == 0;
        boolean randomLeft = random.nextInt(LegacyTileRenderPlans.EXPOSURE_RANDOM_CHANCE) == 0;
        int loopStart = (int) (currentMillis % 1000L) / 50;
        LegacyMachineEffectPresenter.enqueueExposureChamberBeams(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                buffer, randomTop, randomRight, randomLeft, randomColor, loopStart, currentMillis);
    }
}
