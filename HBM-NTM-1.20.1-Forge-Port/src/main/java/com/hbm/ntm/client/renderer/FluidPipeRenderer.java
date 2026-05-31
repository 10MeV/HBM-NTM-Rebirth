package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FluidPipeRenderer implements BlockEntityRenderer<FluidPipeBlockEntity> {
    public FluidPipeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(FluidPipeBlockEntity pipe, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = pipe.getBlockState();
        if (!hasConnectionProperties(state)) {
            return;
        }

        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(pipe, packedLight);
        int color = pipe.getFluidType().getColor();

        boolean east = state.getValue(HbmFluidNodeBlock.EAST);
        boolean west = state.getValue(HbmFluidNodeBlock.WEST);
        boolean up = state.getValue(HbmFluidNodeBlock.UP);
        boolean down = state.getValue(HbmFluidNodeBlock.DOWN);
        boolean south = state.getValue(HbmFluidNodeBlock.SOUTH);
        boolean north = state.getValue(HbmFluidNodeBlock.NORTH);
        int mask = (east ? 32 : 0)
                | (west ? 16 : 0)
                | (up ? 8 : 0)
                | (down ? 4 : 0)
                | (south ? 2 : 0)
                | (north ? 1 : 0);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        if (mask == 0) {
            renderDuct("pX", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nX", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("pY", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nY", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("pZ", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nZ", color, poseStack, buffer, modelLight, packedOverlay);
        } else if ((east || west) && !up && !down && !south && !north) {
            renderDuct("pX", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nX", color, poseStack, buffer, modelLight, packedOverlay);
        } else if ((up || down) && !east && !west && !south && !north) {
            renderDuct("pY", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nY", color, poseStack, buffer, modelLight, packedOverlay);
        } else if ((south || north) && !east && !west && !up && !down) {
            renderDuct("pZ", color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nZ", color, poseStack, buffer, modelLight, packedOverlay);
        } else {
            renderConnectedParts(east, west, up, down, south, north, color, poseStack, buffer, modelLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static boolean hasConnectionProperties(BlockState state) {
        return state.hasProperty(HbmFluidNodeBlock.NORTH)
                && state.hasProperty(HbmFluidNodeBlock.EAST)
                && state.hasProperty(HbmFluidNodeBlock.SOUTH)
                && state.hasProperty(HbmFluidNodeBlock.WEST)
                && state.hasProperty(HbmFluidNodeBlock.UP)
                && state.hasProperty(HbmFluidNodeBlock.DOWN);
    }

    private static void renderConnectedParts(boolean east, boolean west, boolean up, boolean down,
            boolean south, boolean north, int color, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (east) renderDuct("pX", color, poseStack, buffer, packedLight, packedOverlay);
        if (west) renderDuct("nX", color, poseStack, buffer, packedLight, packedOverlay);
        if (up) renderDuct("pY", color, poseStack, buffer, packedLight, packedOverlay);
        if (down) renderDuct("nY", color, poseStack, buffer, packedLight, packedOverlay);
        if (south) renderDuct("nZ", color, poseStack, buffer, packedLight, packedOverlay);
        if (north) renderDuct("pZ", color, poseStack, buffer, packedLight, packedOverlay);

        if (!east && !up && !south) renderDuct("ppn", color, poseStack, buffer, packedLight, packedOverlay);
        if (!east && !up && !north) renderDuct("ppp", color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !up && !south) renderDuct("npn", color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !up && !north) renderDuct("npp", color, poseStack, buffer, packedLight, packedOverlay);
        if (!east && !down && !south) renderDuct("pnn", color, poseStack, buffer, packedLight, packedOverlay);
        if (!east && !down && !north) renderDuct("pnp", color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !down && !south) renderDuct("nnn", color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !down && !north) renderDuct("nnp", color, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderDuct(String part, int color, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjBlockModels.PIPE_NEO.renderPart(part, ObjBlockModels.PIPE_NEO_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, true);
        ObjBlockModels.PIPE_NEO.renderPart(part, ObjBlockModels.PIPE_NEO_OVERLAY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255, true);
    }
}
