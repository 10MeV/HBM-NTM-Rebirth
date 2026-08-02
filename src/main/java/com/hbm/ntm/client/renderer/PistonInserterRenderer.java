package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.PistonInserterBlock;
import com.hbm.ntm.blockentity.PistonInserterBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Source-aligned replacement for RenderPistonInserter's Frame/Piston/item order. */
public final class PistonInserterRenderer implements BlockEntityRenderer<PistonInserterBlockEntity> {
    public PistonInserterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(PistonInserterBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(PistonInserterBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(PistonInserterBlock.FACING);
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.5D, 0.5D);
                rotateLegacyFacing(poseStack, facing);
                poseStack.translate(0.0D, -0.5D, 0.0D);
                ObjModelLibrary.MACHINE_PISTON_INSERTER.renderPart("Frame",
                        ObjMachineModels.PISTON_INSERTER_TEXTURE, poseStack, buffer, modelLight, packedOverlay);
                poseStack.translate(0.0D, blockEntity.getInterpolatedExtend(partialTick)
                        / PistonInserterBlockEntity.MAX_EXTEND * 0.9375D, 0.0D);
                ObjModelLibrary.MACHINE_PISTON_INSERTER.renderPart("Piston",
                        ObjMachineModels.PISTON_INSERTER_TEXTURE, poseStack, buffer, modelLight, packedOverlay);
                renderHeldItem(blockEntity.getSlot(), blockEntity, poseStack, buffer, packedLight);
                poseStack.popPose();
            }
        }
    }

    private static void renderHeldItem(ItemStack stack, PistonInserterBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (stack.getItem() instanceof BlockItem) {
            poseStack.translate(0.0D, 1.125D, 0.0D);
        } else {
            poseStack.translate(0.0D, 1.0625D, 0.1D);
            LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    static void rotateLegacyFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
            case UP -> {
            }
            case NORTH -> {
                LegacyPoseRotations.rotateXDegrees(poseStack, -90.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            }
            case SOUTH -> LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
            case WEST -> {
                LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
            }
            case EAST -> {
                LegacyPoseRotations.rotateZDegrees(poseStack, -90.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            }
        }
    }
}
