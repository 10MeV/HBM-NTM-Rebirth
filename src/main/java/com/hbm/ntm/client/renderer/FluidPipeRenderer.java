package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FluidPipeRenderer implements BlockEntityRenderer<FluidPipeBlockEntity> {
    private static final String[] BASE_TEXTURES = {"pipe_neo", "pipe_silver", "pipe_colored"};
    private static final String[] OVERLAY_TEXTURES = {"pipe_neo_overlay", "pipe_silver_overlay", "pipe_colored_overlay"};

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

        int modelLight = LegacyRenderLighting.resolveMultiblockLight(pipe, packedLight);
        int color = pipe.getFluidType().getColor();
        int style = state.hasProperty(FluidPipeBlock.LEGACY_STYLE)
                ? FluidPipeBlock.clampLegacyStyle(state.getValue(FluidPipeBlock.LEGACY_STYLE))
                : 0;

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
            renderDuct("pX", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nX", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("pY", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nY", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("pZ", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nZ", style, color, poseStack, buffer, modelLight, packedOverlay);
        } else if ((east || west) && !up && !down && !south && !north) {
            renderDuct("pX", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nX", style, color, poseStack, buffer, modelLight, packedOverlay);
        } else if ((up || down) && !east && !west && !south && !north) {
            renderDuct("pY", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nY", style, color, poseStack, buffer, modelLight, packedOverlay);
        } else if ((south || north) && !east && !west && !up && !down) {
            renderDuct("pZ", style, color, poseStack, buffer, modelLight, packedOverlay);
            renderDuct("nZ", style, color, poseStack, buffer, modelLight, packedOverlay);
        } else {
            renderConnectedParts(east, west, up, down, south, north, style, color, poseStack, buffer, modelLight,
                    packedOverlay);
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
            boolean south, boolean north, int style, int color, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (east) renderDuct("pX", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (west) renderDuct("nX", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (up) renderDuct("pY", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (down) renderDuct("nY", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (south) renderDuct("nZ", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (north) renderDuct("pZ", style, color, poseStack, buffer, packedLight, packedOverlay);

        if (!east && !up && !south) renderDuct("ppn", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!east && !up && !north) renderDuct("ppp", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !up && !south) renderDuct("npn", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !up && !north) renderDuct("npp", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!east && !down && !south) renderDuct("pnn", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!east && !down && !north) renderDuct("pnp", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !down && !south) renderDuct("nnn", style, color, poseStack, buffer, packedLight, packedOverlay);
        if (!west && !down && !north) renderDuct("nnp", style, color, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderDuct(String part, int style, int color, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjBlockModels.PIPE_NEO.renderPart(part, ObjBlockModels.texture(BASE_TEXTURES[style]),
                poseStack, buffer, packedLight, packedOverlay,
                255, 255, 255, 255, true);
        ObjBlockModels.PIPE_NEO.renderPart(part, ObjBlockModels.texture(OVERLAY_TEXTURES[style]),
                poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255, true);
    }
}
