package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BreedingReactorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BreedingReactorRenderer implements BlockEntityRenderer<BreedingReactorBlockEntity> {
    private static final float[] LEGACY_SPARK_YAW_RADIANS = {
            0.0F,
            ((float) Math.PI) * Mth.DEG_TO_RAD,
            ((float) (Math.PI * 2.0D)) * Mth.DEG_TO_RAD
    };

    public BreedingReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(BreedingReactorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(BreedingReactorBlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity.getProgress() > 0.0F
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(BreedingReactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getProgress() <= 0.0F) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, yRotation(facing));
        enqueueLegacySparks(blockEntity, poseStack, buffer);
        poseStack.popPose();
    }

    private static void enqueueLegacySparks(BreedingReactorBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (blockEntity.getProgress() <= 0.0F) {
            return;
        }
        int seed = (int) ((System.currentTimeMillis() % 10_000L) / 100L);
        LegacyMachineEffectPresenter.enqueueSparkGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, group -> {
                    for (int i = 0; i < LEGACY_SPARK_YAW_RADIANS.length; i++) {
                        group.addRadians(seed + i, 0.0D, 1.5625D, 0.0D, LEGACY_SPARK_YAW_RADIANS[i],
                                0.15F, 3, 4, 0x00FF00, 0xFFFFFF);
                    }
                });
    }

    private static float yRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            case EAST -> 0.0F;
            default -> 0.0F;
        };
    }
}
