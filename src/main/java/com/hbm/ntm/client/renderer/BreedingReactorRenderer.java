package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.BreedingReactorBlockEntity;
import com.hbm.ntm.client.obj.LegacySparkRenderer;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
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
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(BreedingReactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        int light = LegacyRenderLighting.resolveBoundsLight(blockEntity, blockEntity.getRenderBoundingBox(), packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation(facing)));
        ObjReactorModels.BREEDER.renderAll(ObjReactorModels.BREEDER_TEXTURE, poseStack, buffer, light, packedOverlay);
        renderLegacySparks(blockEntity, poseStack, buffer, state, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLegacySparks(BreedingReactorBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, BlockState state, int packedOverlay) {
        if (blockEntity.getProgress() <= 0.0F) {
            return;
        }
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, LightTexture.FULL_BRIGHT,
                packedOverlay).withAdditiveTranslucency();
        int seed = (int) ((System.currentTimeMillis() % 10_000L) / 100L);
        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (Math.PI * i)));
            LegacySparkRenderer.renderSpark(context, seed + i, 0.0D, 1.5625D, 0.0D,
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
