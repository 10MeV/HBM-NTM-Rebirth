package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.DecoComputerBlock;
import com.hbm.ntm.blockentity.DecoComputerBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Exact modern carrier for RenderBlockDecoModel's IBM_300PL whole-OBJ render. */
public final class DecoComputerRenderer implements BlockEntityRenderer<DecoComputerBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjBlockModels.DECO_COMPUTER;
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/block/deco_computer.png");
    public DecoComputerRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public int getViewDistance() { return LegacyBlockEntityRenderDistances.machine(); }
    @Override public boolean shouldRender(DecoComputerBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos) && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }
    @Override public void render(DecoComputerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) return;
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            renderModel(blockEntity.getBlockState(), poseStack, buffer, LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight), packedOverlay);
        }
    }
    public static void renderItemModel(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderModel(state.setValue(DecoComputerBlock.FACING, Direction.SOUTH), poseStack, buffer, packedLight, packedOverlay);
    }
    private static void renderModel(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose(); poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, legacyYaw(state.getValue(DecoComputerBlock.FACING)));
        MODEL.renderAll(TEXTURE, poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }
    private static float legacyYaw(Direction facing) {
        return switch (facing) { case NORTH -> 180.0F; case WEST -> 270.0F; case EAST -> 90.0F; default -> 0.0F; };
    }
}
