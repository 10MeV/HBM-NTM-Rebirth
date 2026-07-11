package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.ZirnoxDestroyedBlockEntity;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ZirnoxDestroyedRenderer implements BlockEntityRenderer<ZirnoxDestroyedBlockEntity> {
    public ZirnoxDestroyedRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ZirnoxDestroyedBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public boolean shouldRender(ZirnoxDestroyedBlockEntity blockEntity, Vec3 cameraPos) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(ZirnoxDestroyedBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, rotation(blockEntity.getBlockState()));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjReactorModels.ZIRNOX_DESTROYED.renderAll(ObjReactorModels.ZIRNOX_DESTROYED_TEXTURE,
                    poseStack, buffer, light, packedOverlay);
        }
        poseStack.popPose();
    }

    private static float rotation(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }
}
