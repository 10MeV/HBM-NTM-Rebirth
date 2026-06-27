package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.WatzStructCoreBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class WatzStructCoreRenderer implements BlockEntityRenderer<WatzStructCoreBlockEntity> {
    private static final TextureAtlasSprite ELEMENT_TOP = sprite("legacy_blocks/watz_element_top");
    private static final TextureAtlasSprite ELEMENT_SIDE = sprite("legacy_blocks/watz_element_side");
    private static final TextureAtlasSprite COOLER_TOP = sprite("legacy_blocks/watz_cooler_top");
    private static final TextureAtlasSprite COOLER_SIDE = sprite("legacy_blocks/watz_cooler_side");
    private static final TextureAtlasSprite RIVETED_END = sprite("legacy_blocks/watz_casing_bolted");

    private static final int[][] ELEMENT_OFFSETS = {
            {1, 0}, {2, 0}, {0, 1}, {0, 2}, {-1, 0}, {-2, 0}, {0, -1}, {0, -2},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };
    private static final int[][] COOLER_OFFSETS = {
            {2, 1}, {2, -1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {1, -2}, {-1, -2}
    };

    public WatzStructCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public boolean shouldRenderOffScreen(WatzStructCoreBlockEntity blockEntity) {
        return false;
    }

    @Override
    public void render(WatzStructCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> renderPreview(queuedPose, buffer, light));
    }

    private static void renderPreview(PoseStack poseStack, MultiBufferSource buffer, int light) {
        renderPillar(poseStack, buffer, light, COOLER_TOP, COOLER_SIDE, 0, 1, 0);
        renderPillar(poseStack, buffer, light, COOLER_TOP, COOLER_SIDE, 0, 2, 0);

        for (int y = 0; y < 3; y++) {
            for (int[] offset : ELEMENT_OFFSETS) {
                renderPillar(poseStack, buffer, light, ELEMENT_TOP, ELEMENT_SIDE, offset[0], y, offset[1]);
            }
            for (int[] offset : COOLER_OFFSETS) {
                renderPillar(poseStack, buffer, light, COOLER_TOP, COOLER_SIDE, offset[0], y, offset[1]);
            }
            for (int z = -1; z < 2; z++) {
                renderCube(poseStack, buffer, light, RIVETED_END, 3, y, z);
                renderCube(poseStack, buffer, light, RIVETED_END, z, y, 3);
                renderCube(poseStack, buffer, light, RIVETED_END, -3, y, z);
                renderCube(poseStack, buffer, light, RIVETED_END, z, y, -3);
            }
            renderCube(poseStack, buffer, light, RIVETED_END, 2, y, 2);
            renderCube(poseStack, buffer, light, RIVETED_END, 2, y, -2);
            renderCube(poseStack, buffer, light, RIVETED_END, -2, y, 2);
            renderCube(poseStack, buffer, light, RIVETED_END, -2, y, -2);
        }
    }

    private static void renderPillar(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            TextureAtlasSprite top, TextureAtlasSprite side, double x, double y, double z) {
        LegacyAtlasCuboidRenderer.smallBlock(top, top, side, side, side, side, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFF, LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, x, y, z);
    }

    private static void renderCube(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            TextureAtlasSprite texture, double x, double y, double z) {
        LegacyAtlasCuboidRenderer.smallBlock(texture, texture, texture, texture, texture, texture, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, x, y, z);
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }
}
