package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKCraneConsoleBlock;
import com.hbm.ntm.blockentity.RBMKCraneConsoleBlockEntity;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKCraneConsoleRenderer implements BlockEntityRenderer<RBMKCraneConsoleBlockEntity> {
    public RBMKCraneConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RBMKCraneConsoleBlockEntity console, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = console.getBlockState();
        Direction facing = state.hasProperty(RBMKCraneConsoleBlock.FACING)
                ? state.getValue(RBMKCraneConsoleBlock.FACING)
                : Direction.SOUTH;
        int light = LegacyRenderLighting.resolveMultiblockLight(console, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(facing)));
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);
        LegacyRbmkMachineRenderer.renderCraneConsole(context, console.consoleRenderState(), partialTick,
                System.currentTimeMillis());
        poseStack.popPose();

        if (console.cranePlannerState().setUpCrane()) {
            poseStack.pushPose();
            BlockPos center = console.craneCenter();
            BlockPos pos = console.getBlockPos();
            poseStack.translate(center.getX() - pos.getX() + 0.5D, center.getY() - pos.getY(),
                    center.getZ() - pos.getZ() + 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(facing)));
            LegacyRbmkMachineRenderer.renderCrane(new ObjRenderContext(poseStack, buffer, state, light, packedOverlay),
                    console.craneRenderState(), partialTick);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKCraneConsoleBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
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
