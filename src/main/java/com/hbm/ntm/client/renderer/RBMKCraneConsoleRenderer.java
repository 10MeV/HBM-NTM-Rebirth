package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.block.RBMKCraneConsoleBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.RBMKCraneConsoleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RBMKCraneConsoleRenderer implements BlockEntityRenderer<RBMKCraneConsoleBlockEntity> {
    public RBMKCraneConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRender(RBMKCraneConsoleBlockEntity console, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(console, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(console, getViewDistance());
    }

    @Override
    public void render(RBMKCraneConsoleBlockEntity console, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(console, getViewDistance())) {
            return;
        }
        BlockState state = console.getBlockState();
        Direction facing = state.hasProperty(RBMKCraneConsoleBlock.FACING)
                ? state.getValue(RBMKCraneConsoleBlock.FACING)
                : Direction.SOUTH;
        int light = LegacyRenderLighting.resolveMultiblockLight(console, packedLight);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(console)) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, legacyYaw(facing));
            LegacyRbmkMachineRenderer.renderCraneConsole(poseStack, buffer, light, packedOverlay,
                    console.consoleRenderState(), partialTick,
                    System.currentTimeMillis(), LegacyMachineRenderShapes.renderChunkBakedStaticsInBer());
            poseStack.popPose();

            if (console.cranePlannerState().setUpCrane()) {
                poseStack.pushPose();
                BlockPos center = console.craneCenter();
                BlockPos pos = console.getBlockPos();
                poseStack.translate(center.getX() - pos.getX() + 0.5D, center.getY() - pos.getY(),
                        center.getZ() - pos.getZ() + 0.5D);
                LegacyPoseRotations.rotateYDegrees(poseStack, legacyYaw(facing));
                LegacyRbmkMachineRenderer.renderCrane(poseStack, buffer, light, packedOverlay,
                        console.craneRenderState(), partialTick);
                poseStack.popPose();
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKCraneConsoleBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }
}
