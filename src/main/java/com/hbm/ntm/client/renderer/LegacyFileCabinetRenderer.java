package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.LegacyFileCabinetBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LegacyFileCabinetRenderer implements BlockEntityRenderer<LegacyFileCabinetBlockEntity> {
    private static final double DRAWER_TRAVEL = 0.6875D;
    private static final LegacyWavefrontModel MODEL = ObjUtilityModels.FILE_CABINET;
    private static final LegacyWavefrontModel.SelectionHandle CABINET =
            MODEL.prepareRenderOnlyInCallOrder("Cabinet");
    private static final LegacyWavefrontModel.SelectionHandle LOWER_DRAWER =
            MODEL.prepareRenderOnlyInCallOrder("LowerDrawer");
    private static final LegacyWavefrontModel.SelectionHandle UPPER_DRAWER =
            MODEL.prepareRenderOnlyInCallOrder("UpperDrawer");

    public LegacyFileCabinetRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(LegacyFileCabinetBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(LegacyFileCabinetBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, legacyYaw(state));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            renderModel(poseStack, buffer, texture(blockEntity.variant()), modelLight, packedLight, packedOverlay,
                    blockEntity.lowerExtent(partialTick), blockEntity.upperExtent(partialTick),
                    LegacyMachineRenderShapes.renderChunkBakedStaticsInBer());
        }
        poseStack.popPose();
    }

    public static void renderItemModel(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int variant,
            int packedLight, int packedOverlay) {
        renderModel(poseStack, buffer, texture(variant), packedLight, packedLight, packedOverlay, 0.0F, 0.0F, true);
    }

    private static void renderModel(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture,
            int fixedLight, int movingLight, int packedOverlay, float lower, float upper, boolean includeCabinet) {
        if (includeCabinet) {
            renderPart(texture, CABINET, poseStack, buffer, fixedLight, packedOverlay);
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER_TRAVEL * lower);
        renderPart(texture, LOWER_DRAWER, poseStack, buffer, movingLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, DRAWER_TRAVEL * upper);
        renderPart(texture, UPPER_DRAWER, poseStack, buffer, movingLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderPart(ResourceLocation texture, LegacyWavefrontModel.SelectionHandle handle,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle,
                LegacyTexturedRenderMode.CUTOUT_CULL);
    }

    private static ResourceLocation texture(int variant) {
        return variant == 1 ? ObjUtilityModels.FILE_CABINET_STEEL_TEXTURE : ObjUtilityModels.FILE_CABINET_TEXTURE;
    }

    private static float legacyYaw(BlockState state) {
        Direction facing = state.hasProperty(LegacyFileCabinetBlock.FACING)
                ? state.getValue(LegacyFileCabinetBlock.FACING)
                : Direction.NORTH;
        // Keep the legacy four-way mapping explicit. An enum switch creates a
        // compiler-generated $1 class, which is not reliably copied into the
        // Forge dev runtime's bin/main output during incremental compilation.
        if (facing == Direction.SOUTH) {
            return 0.0F;
        }
        if (facing == Direction.WEST) {
            return 270.0F;
        }
        if (facing == Direction.EAST) {
            return 90.0F;
        }
        return 180.0F;
    }
}
