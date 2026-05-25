package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class LiquefactorRenderer implements BlockEntityRenderer<LiquefactorBlockEntity> {
    public LiquefactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(LiquefactorBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(LiquefactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int modelLight = packedLight;
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            LegacyMachineDefinition definition = block.definition();
            modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        }
        float rotation = state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? 270.0F - state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING).toYRot()
                : 180.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart("Main", poseStack, buffer, modelLight, packedOverlay);
        if (blockEntity.getTank().getFill() > 0) {
            int color = blockEntity.getTank().getTankType().getColor();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            double height = (double) blockEntity.getTank().getFill() / (double) blockEntity.getTank().getMaxFill();
            poseStack.pushPose();
            poseStack.translate(0.0D, 1.0D, 0.0D);
            poseStack.scale(1.0F, (float) height, 1.0F);
            poseStack.translate(0.0D, -1.0D, 0.0D);
            ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart("Fluid", ObjModelLibrary.MACHINE_LIQUEFACTOR.textureLocation(),
                    poseStack, buffer, modelLight, packedOverlay, red, green, blue, 255);
            poseStack.popPose();
        }
        ObjModelLibrary.MACHINE_LIQUEFACTOR.renderPart("Glass", ObjModelLibrary.MACHINE_LIQUEFACTOR.textureLocation(),
                poseStack, buffer, modelLight, packedOverlay, 191, 255, 255, 64);

        poseStack.popPose();
    }
}
