package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.MicrowaveBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class MicrowaveRenderer implements BlockEntityRenderer<MicrowaveBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.MICROWAVE;
    private static final LegacyWavefrontModel.SelectionHandle MAIN_BODY =
            MODEL.prepareRenderOnlyInCallOrder("mainbody_Cube.001");
    private static final LegacyWavefrontModel.SelectionHandle WINDOW =
            MODEL.prepareRenderOnlyInCallOrder("window_Cube.002");
    private static final LegacyWavefrontModel.SelectionHandle PLATE =
            MODEL.prepareRenderOnlyInCallOrder("plate_Cylinder");

    public MicrowaveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MicrowaveBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(MicrowaveBlockEntity microwave, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = microwave.getBlockState();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(microwave, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.785D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(state)));
        poseStack.translate(-0.5D, 0.0D, 0.65D);
        MODEL.renderOnlyInCallOrder(ObjMachineModels.MICROWAVE_TEXTURE, poseStack, buffer, modelLight,
                packedOverlay, MAIN_BODY);
        MODEL.renderOnlyInCallOrder(ObjMachineModels.MICROWAVE_TEXTURE, poseStack, buffer, modelLight,
                packedOverlay, WINDOW);
        if (microwave.getTime() > 0) {
            double rotation = (System.currentTimeMillis() * microwave.getSpeed() / 10.0D) % 360.0D;
            poseStack.translate(0.575D, 0.0D, -0.45D);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) rotation));
            poseStack.translate(-0.575D, 0.0D, 0.45D);
        }
        MODEL.renderOnlyInCallOrder(ObjMachineModels.MICROWAVE_TEXTURE, poseStack, buffer, modelLight,
                packedOverlay, PLATE);
        poseStack.popPose();
    }

    private static float rotation(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }
}
