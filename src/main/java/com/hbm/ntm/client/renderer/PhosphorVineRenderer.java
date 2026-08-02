package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.PhosphorVineBlock;
import com.hbm.ntm.blockentity.PhosphorVineBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Exact two-pass modern carrier for 1.7.10 {@code RenderHangingVine}. */
public final class PhosphorVineRenderer implements BlockEntityRenderer<PhosphorVineBlockEntity> {
    private static final ResourceLocation BASE = texture("vine_phosphor");
    private static final ResourceLocation GROUND = texture("vine_phosphor_ground");
    private static final ResourceLocation HANG = texture("vine_phosphor_hang");
    private static final ResourceLocation SPOTS = texture("vine_phosphor_spots");
    private static final ResourceLocation HANG_SPOTS = texture("vine_phosphor_spots_hang");
    private static final double FACTOR = 0.45D;

    public PhosphorVineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(PhosphorVineBlockEntity vine, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(vine, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(vine, getViewDistance());
    }

    @Override
    public void render(PhosphorVineBlockEntity vine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(vine, getViewDistance()) || vine.getLevel() == null) {
            return;
        }
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(vine)) {
            Level level = vine.getLevel();
            BlockState below = level.getBlockState(vine.getBlockPos().below());
            ResourceLocation base = below.isFaceSturdy(level, vine.getBlockPos().below(), net.minecraft.core.Direction.UP)
                    ? GROUND : below.is(vine.getBlockState().getBlock()) ? BASE : HANG;
            ResourceLocation spots = below.isAir() ? HANG_SPOTS : SPOTS;
            int biomeColor = BiomeColors.getAverageFoliageColor(level, vine.getBlockPos());
            drawCrossedSquares(base, poseStack, buffer, packedLight, packedOverlay, biomeColor);
            drawCrossedSquares(spots, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay, 0xFFFFFF);
        }
    }

    private static void drawCrossedSquares(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int color) {
        double min = 0.5D - FACTOR;
        double max = 0.5D + FACTOR;
        draw(texture, poseStack, buffer, packedLight, packedOverlay, color,
                min, 0.0D, min, 1.0D, 1.0D, min, 1.0D, min, 1.0D, 0.0D,
                max, 1.0D, max, 0.0D, 0.0D, max, 0.0D, max, 0.0D, 1.0D);
        draw(texture, poseStack, buffer, packedLight, packedOverlay, color,
                max, 0.0D, max, 1.0D, 1.0D, max, 1.0D, max, 1.0D, 0.0D,
                min, 1.0D, min, 0.0D, 0.0D, min, 0.0D, min, 0.0D, 1.0D);
        draw(texture, poseStack, buffer, packedLight, packedOverlay, color,
                max, 0.0D, min, 1.0D, 1.0D, max, 1.0D, min, 1.0D, 0.0D,
                min, 1.0D, max, 0.0D, 0.0D, min, 0.0D, max, 0.0D, 1.0D);
        draw(texture, poseStack, buffer, packedLight, packedOverlay, color,
                min, 0.0D, max, 1.0D, 1.0D, min, 1.0D, max, 1.0D, 0.0D,
                max, 1.0D, min, 0.0D, 0.0D, max, 0.0D, min, 0.0D, 1.0D);
    }

    private static void draw(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, int color,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3) {
        LegacyTexturedQuadRenderer.quadWithComputedNormalDirect(texture, poseStack, buffer, packedLight,
                packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                x0, y0, z0, u0, v0, 255, x1, y1, z1, u1, v1, 255,
                x2, y2, z2, u2, v2, 255, x3, y3, z3, u3, v3, 255, color);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/" + name + ".png");
    }
}
