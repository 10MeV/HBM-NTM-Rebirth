package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.FensuBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class FensuRenderer implements BlockEntityRenderer<FensuBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ObjMachineModels.FENSU_TEXTURE;
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.FENSU_LEGACY;

    public FensuRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FensuBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(FensuBlockEntity fensu, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(fensu, packedLight);
        BlockState state = fensu.getBlockState();
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYRotation(state)));

        MODEL.renderPart("Base", TEXTURE, context);

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(fensu.getInterpolatedRotation(partialTick)));
        poseStack.translate(0.0D, -2.5D, 0.0D);
        MODEL.renderPart("Disc", TEXTURE, context);
        poseStack.popPose();

        MODEL.renderPart("Lights", TEXTURE, context.fullBright());
        poseStack.popPose();
    }

    private static float legacyYRotation(BlockState state) {
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
