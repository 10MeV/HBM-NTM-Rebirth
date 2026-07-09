package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.ZirnoxReactorBlock;
import com.hbm.ntm.blockentity.ZirnoxReactorBlockEntity;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class ZirnoxReactorRenderer implements BlockEntityRenderer<ZirnoxReactorBlockEntity> {
    public ZirnoxReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ZirnoxReactorBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(ZirnoxReactorBlockEntity blockEntity, Vec3 cameraPos) {
        return needsBodyRenderer(blockEntity)
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(ZirnoxReactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!needsBodyRenderer(blockEntity)) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        boolean tilted = isTilted(blockEntity, state);
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (tilted) {
            poseStack.translate(0.0D, -0.5D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(5.0F));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(state)));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjReactorModels.ZIRNOX.renderAll(ObjReactorModels.ZIRNOX_TEXTURE,
                    poseStack, buffer, light, packedOverlay);
        }
        poseStack.popPose();
    }

    private static boolean needsBodyRenderer(ZirnoxReactorBlockEntity blockEntity) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                || isTilted(blockEntity, blockEntity.getBlockState());
    }

    private static boolean isTilted(ZirnoxReactorBlockEntity blockEntity, BlockState state) {
        return state.hasProperty(ZirnoxReactorBlock.TILTED) ? state.getValue(ZirnoxReactorBlock.TILTED)
                : blockEntity.isTilted();
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
