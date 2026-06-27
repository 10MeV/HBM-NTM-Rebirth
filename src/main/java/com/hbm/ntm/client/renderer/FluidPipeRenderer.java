package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class FluidPipeRenderer implements BlockEntityRenderer<FluidPipeBlockEntity> {
    private static final ResourceLocation[] BASE_TEXTURE_LOCATIONS = buildTextures(false);
    private static final ResourceLocation[] OVERLAY_TEXTURE_LOCATIONS = buildTextures(true);
    private static final String[][] PARTS_BY_MASK = buildPartsByMask();
    private static final LegacyWavefrontModel.SelectionHandle[] PART_HANDLES = buildPartHandles(PARTS_BY_MASK);

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

        renderParts(mask, style, color, poseStack, buffer, modelLight, packedOverlay);

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

    private static void renderParts(int mask, int style, int color, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = PART_HANDLES[mask];
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(BASE_TEXTURE_LOCATIONS[style], poseStack, buffer,
                packedLight, packedOverlay, 255, 255, 255, 255, true, handle);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(OVERLAY_TEXTURE_LOCATIONS[style], poseStack, buffer,
                packedLight, packedOverlay, color >> 16 & 255, color >> 8 & 255, color & 255, 255, true, handle);
    }

    private static ResourceLocation[] buildTextures(boolean overlay) {
        ResourceLocation[] textures = new ResourceLocation[HbmFluidDuctVariants.STANDARD_STYLE_COUNT];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = ObjBlockModels.texture(overlay
                    ? HbmFluidDuctVariants.standardOverlayTexture(i)
                    : HbmFluidDuctVariants.standardParticleTexture(i));
        }
        return textures;
    }

    private static String[][] buildPartsByMask() {
        String[][] parts = new String[64][];
        for (int mask = 0; mask < parts.length; mask++) {
            parts[mask] = buildParts(mask);
        }
        return parts;
    }

    private static LegacyWavefrontModel.SelectionHandle[] buildPartHandles(String[][] partsByMask) {
        LegacyWavefrontModel.SelectionHandle[] handles = new LegacyWavefrontModel.SelectionHandle[partsByMask.length];
        for (int mask = 0; mask < partsByMask.length; mask++) {
            handles[mask] = ObjBlockModels.PIPE_NEO.prepareRenderOnlyInCallOrder(partsByMask[mask]);
        }
        return handles;
    }

    private static String[] buildParts(int mask) {
        boolean east = (mask & 32) != 0;
        boolean west = (mask & 16) != 0;
        boolean up = (mask & 8) != 0;
        boolean down = (mask & 4) != 0;
        boolean south = (mask & 2) != 0;
        boolean north = (mask & 1) != 0;

        if (mask == 0) {
            return new String[]{"pX", "nX", "pY", "nY", "pZ", "nZ"};
        }
        if ((east || west) && !up && !down && !south && !north) {
            return new String[]{"pX", "nX"};
        }
        if ((up || down) && !east && !west && !south && !north) {
            return new String[]{"pY", "nY"};
        }
        if ((south || north) && !east && !west && !up && !down) {
            return new String[]{"pZ", "nZ"};
        }

        List<String> parts = new ArrayList<>(14);
        if (east) parts.add("pX");
        if (west) parts.add("nX");
        if (up) parts.add("pY");
        if (down) parts.add("nY");
        if (south) parts.add("nZ");
        if (north) parts.add("pZ");

        if (!east && !up && !south) parts.add("ppn");
        if (!east && !up && !north) parts.add("ppp");
        if (!west && !up && !south) parts.add("npn");
        if (!west && !up && !north) parts.add("npp");
        if (!east && !down && !south) parts.add("pnn");
        if (!east && !down && !north) parts.add("pnp");
        if (!west && !down && !south) parts.add("nnn");
        if (!west && !down && !north) parts.add("nnp");
        return parts.toArray(String[]::new);
    }
}
