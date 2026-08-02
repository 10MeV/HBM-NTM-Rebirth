package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.LegacyReedsBlockEntity;
import com.hbm.ntm.client.obj.LegacyIsbrhBlockPlans;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.config.HbmClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Exact modern renderer for 1.7.10 {@code RenderReeds}, including the optional water-depth stack. */
public final class LegacyReedsRenderer implements BlockEntityRenderer<LegacyReedsBlockEntity> {
    private static final ResourceLocation TOP = texture("reeds_top");
    private static final ResourceLocation MIDDLE = texture("reeds_mid");
    private static final ResourceLocation BOTTOM = texture("reeds_bottom");

    public LegacyReedsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LegacyReedsBlockEntity reeds, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        Level level = reeds.getLevel();
        if (level == null) {
            return;
        }

        boolean renderDeepReeds = HbmClientConfig.renderReeds();
        int waterDepth = waterDepth(level, reeds.getBlockPos());
        LegacyIsbrhBlockPlans.PlantCrossRenderPlan plan = LegacyIsbrhBlockPlans.reedsWorldPlan(0xFFFFFF,
                renderDeepReeds, waterDepth + 1, 0.0D, 0.0D, 0.0D);
        BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();
        for (LegacyIsbrhBlockPlans.CrossedSquareLayerPlan layer : plan.layers()) {
            lightPos.set(reeds.getBlockPos()).move(0, (int) layer.yOffset(), 0);
            int layerLight = LevelRenderer.getLightColor(level, lightPos);
            ResourceLocation texture = switch (layer.iconRole()) {
                case "top" -> TOP;
                case "middle" -> MIDDLE;
                case "bottom" -> BOTTOM;
                default -> throw new IllegalStateException("Unexpected legacy reeds texture role: " + layer.iconRole());
            };
            drawLayer(layer, texture, poseStack, buffer, layerLight, packedOverlay);
        }
    }

    private static int waterDepth(Level level, BlockPos reedsPos) {
        int depth = 0;
        BlockPos.MutableBlockPos cursor = reedsPos.mutable().move(0, -1, 0);
        while (!level.isOutsideBuildHeight(cursor) && level.getBlockState(cursor).is(Blocks.WATER)) {
            depth++;
            cursor.move(0, -1, 0);
        }
        return depth;
    }

    private static void drawLayer(LegacyIsbrhBlockPlans.CrossedSquareLayerPlan layer, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (LegacyIsbrhBlockPlans.LegacyQuadPlan quad : layer.quads()) {
            LegacyIsbrhBlockPlans.LegacyVertex v0 = quad.vertices().get(0);
            LegacyIsbrhBlockPlans.LegacyVertex v1 = quad.vertices().get(1);
            LegacyIsbrhBlockPlans.LegacyVertex v2 = quad.vertices().get(2);
            LegacyIsbrhBlockPlans.LegacyVertex v3 = quad.vertices().get(3);
            LegacyTexturedQuadRenderer.quadWithComputedNormalDirect(texture, poseStack, buffer, packedLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                    v0.x(), v0.y(), v0.z(), v0.u(), v0.v(), 255,
                    v1.x(), v1.y(), v1.z(), v1.u(), v1.v(), 255,
                    v2.x(), v2.y(), v2.z(), v2.u(), v2.v(), 255,
                    v3.x(), v3.y(), v3.z(), v3.u(), v3.v(), 255, layer.color().color());
        }
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/" + name + ".png");
    }
}
