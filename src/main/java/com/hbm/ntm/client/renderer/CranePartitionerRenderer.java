package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CranePartitionerBlock;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Direct modern equivalent of legacy RenderPartitioner. */
public class CranePartitionerRenderer implements BlockEntityRenderer<CraneLogisticsBlockEntity> {
    private static final TextureAtlasSprite SIDE = sprite("crane_partitioner_side");
    private static final TextureAtlasSprite BACK = sprite("crane_partitioner_back");
    private static final TextureAtlasSprite TOP = sprite("crane_top");
    private static final TextureAtlasSprite INNER = sprite("crane_splitter_inner");
    private static final TextureAtlasSprite INNER_SIDE = sprite("crane_splitter_inner_side");
    private static final TextureAtlasSprite BELT = sprite("crane_splitter_belt");
    private static final LegacyWavefrontModel.SelectionHandle SIDE_PART =
            ObjBlockModels.CRANE_BUFFER.prepareRenderOnlyInCallOrder("Side");
    private static final LegacyWavefrontModel.SelectionHandle BACK_PART =
            ObjBlockModels.CRANE_BUFFER.prepareRenderOnlyInCallOrder("Back");
    private static final LegacyWavefrontModel.SelectionHandle TOP_PART =
            ObjBlockModels.CRANE_BUFFER.prepareRenderOnlyInCallOrder("Top_Top.001");
    private static final LegacyWavefrontModel.SelectionHandle INNER_PART =
            ObjBlockModels.CRANE_BUFFER.prepareRenderOnlyInCallOrder("Inner");
    private static final LegacyWavefrontModel.SelectionHandle INNER_SIDE_PART =
            ObjBlockModels.CRANE_BUFFER.prepareRenderOnlyInCallOrder("InnerSide");
    private static final LegacyWavefrontModel.SelectionHandle BELT_PART =
            ObjBlockModels.CRANE_BUFFER.prepareRenderOnlyInCallOrder("Belt");

    public CranePartitionerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRender(CraneLogisticsBlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity.kind() == CraneLogisticsBlockEntity.Kind.PARTITIONER
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(CraneLogisticsBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.kind() != CraneLogisticsBlockEntity.Kind.PARTITIONER
                || !LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, CranePartitionerBlock.legacyRenderRotationDegrees(state));
            renderPart(SIDE_PART, SIDE, poseStack, buffer, modelLight, packedOverlay);
            renderPart(BACK_PART, BACK, poseStack, buffer, modelLight, packedOverlay);
            renderPart(TOP_PART, TOP, poseStack, buffer, modelLight, packedOverlay);
            renderPart(INNER_PART, INNER, poseStack, buffer, modelLight, packedOverlay);
            renderPart(INNER_SIDE_PART, INNER_SIDE, poseStack, buffer, modelLight, packedOverlay);
            renderPart(BELT_PART, BELT, poseStack, buffer, modelLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, TextureAtlasSprite sprite,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjBlockModels.CRANE_BUFFER.renderOnlyInCallOrderWithSprite(sprite, poseStack, buffer, packedLight,
                packedOverlay, handle);
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(ObjBlockModels.texture(name));
    }
}
