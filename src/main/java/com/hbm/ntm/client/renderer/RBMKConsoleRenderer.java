package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKConsoleRenderer implements BlockEntityRenderer<RBMKConsoleBlockEntity> {
    public RBMKConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RBMKConsoleBlockEntity console, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(console, getViewDistance())) {
            return;
        }
        BlockState state = console.getBlockState();
        Direction facing = state.hasProperty(RBMKConsoleBlock.FACING)
                ? state.getValue(RBMKConsoleBlock.FACING)
                : Direction.SOUTH;
        int light = LegacyRenderLighting.resolveMultiblockLight(console, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(facing)));
        poseStack.translate(0.5D, 0.0D, 0.0D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(console)) {
            ObjRbmkModels.CONSOLE.renderAll(ObjRbmkModels.CONSOLE_TEXTURE, poseStack, buffer, light, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL);
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKConsoleBlockEntity blockEntity) {
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
