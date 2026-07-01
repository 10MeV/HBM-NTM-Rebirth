package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BreedingReactorBlockEntity;
import com.hbm.ntm.client.obj.LegacySparkRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BreedingReactorRenderer implements BlockEntityRenderer<BreedingReactorBlockEntity> {
    public BreedingReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(BreedingReactorBlockEntity blockEntity) {
        return false;
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
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));
        enqueueLegacySparks(blockEntity, poseStack, buffer);
        poseStack.popPose();
    }

    private static void enqueueLegacySparks(BreedingReactorBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (blockEntity.getProgress() <= 0.0F) {
            return;
        }
        int seed = (int) ((System.currentTimeMillis() % 10_000L) / 100L);
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> renderLegacySparks(seed, queuedPose, buffer));
    }

    private static void renderLegacySparks(int seed, PoseStack poseStack, MultiBufferSource buffer) {
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (Math.PI * i)));
            LegacySparkRenderer.renderSpark(poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                    seed + i, 0.0D, 1.5625D, 0.0D,
                    0.15F, 3, 4, 0x00FF00, 0xFFFFFF);
            poseStack.popPose();
        }
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
