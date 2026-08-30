package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RadarRenderer<T extends RadarBlockEntity> implements BlockEntityRenderer<T> {
    private static final LegacyWavefrontModel SMALL_MODEL = ObjModelLibrary.MACHINE_RADAR_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle SMALL_BASE =
            SMALL_MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle SMALL_DISH =
            SMALL_MODEL.prepareRenderOnlyInCallOrder("Dish");
    private static final LegacyWavefrontModel LARGE_MODEL = ObjModelLibrary.MACHINE_RADAR_LARGE_LEGACY;
    private static final LegacyWavefrontModel.SelectionHandle LARGE_BODY =
            LARGE_MODEL.prepareRenderOnlyInCallOrder("Radar");
    private static final LegacyWavefrontModel.SelectionHandle LARGE_DISH =
            LARGE_MODEL.prepareRenderOnlyInCallOrder("Dish");

    public RadarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(T radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(radar, getViewDistance())) {
            return;
        }
        if (radar instanceof RadarLargeBlockEntity) {
            renderLarge(radar, partialTick, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderSmall(radar, partialTick, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderSmall(RadarBlockEntity radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(radar, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(radar)) {
            if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                SMALL_MODEL.renderOnlyInCallOrder(SMALL_MODEL.textureLocation(), poseStack, buffer,
                        modelLight, packedOverlay, SMALL_BASE);
            }
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(radar)) {
                poseStack.pushPose();
                LegacyPoseRotations.rotateYDegrees(poseStack, -interpolatedRotation(radar, partialTick));
                poseStack.translate(-0.125D, 0.0D, 0.0D);
                SMALL_MODEL.renderOnlyInCallOrder(ObjModelLibrary.MACHINE_RADAR_DISH_TEXTURE,
                        poseStack, buffer, packedLight, packedOverlay, SMALL_DISH);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    private static void renderLarge(RadarBlockEntity radar, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = radar.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(radar, state, definition, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.yRotation(state));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        LegacyPoseRotations.rotateYDegrees(poseStack, definition.postModelYRotation(state));

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(radar)) {
            if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                LARGE_MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, modelLight,
                        packedOverlay, LARGE_BODY);
            }
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(radar)) {
                poseStack.pushPose();
                LegacyPoseRotations.rotateYDegrees(poseStack, -interpolatedRotation(radar, partialTick));
                LARGE_MODEL.renderOnlyInCallOrder(definition.textureLocation(), poseStack, buffer, packedLight,
                        packedOverlay, LARGE_DISH);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    private static float interpolatedRotation(RadarBlockEntity radar, float partialTick) {
        return Mth.lerp(partialTick, radar.getPreviousRotation(), radar.getRotation());
    }
}
