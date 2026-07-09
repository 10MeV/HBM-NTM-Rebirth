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
import net.minecraft.world.phys.Vec3;

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
    public boolean shouldRender(WatzStructCoreBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(WatzStructCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            int alpha = LegacyBlockEntityRenderCulling.fadedStaticAlpha(
                    LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA);
            if (alpha <= 0) {
                return;
            }
            int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

            LegacyMachineEffectPresenter.enqueueAtlasSpriteQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES,
                    poseStack, buffer, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                    quads -> renderPreview(quads, light, alpha));
        }
    }

    private static void renderPreview(LegacyTexturedQuadRenderer.SpritePixelQuadSink quads, int light, int alpha) {
        renderPillar(quads, light, alpha, COOLER_TOP, COOLER_SIDE, 0, 1, 0);
        renderPillar(quads, light, alpha, COOLER_TOP, COOLER_SIDE, 0, 2, 0);

        for (int y = 0; y < 3; y++) {
            for (int[] offset : ELEMENT_OFFSETS) {
                renderPillar(quads, light, alpha, ELEMENT_TOP, ELEMENT_SIDE, offset[0], y, offset[1]);
            }
            for (int[] offset : COOLER_OFFSETS) {
                renderPillar(quads, light, alpha, COOLER_TOP, COOLER_SIDE, offset[0], y, offset[1]);
            }
            for (int z = -1; z < 2; z++) {
                renderCube(quads, light, alpha, RIVETED_END, 3, y, z);
                renderCube(quads, light, alpha, RIVETED_END, z, y, 3);
                renderCube(quads, light, alpha, RIVETED_END, -3, y, z);
                renderCube(quads, light, alpha, RIVETED_END, z, y, -3);
            }
            renderCube(quads, light, alpha, RIVETED_END, 2, y, 2);
            renderCube(quads, light, alpha, RIVETED_END, 2, y, -2);
            renderCube(quads, light, alpha, RIVETED_END, -2, y, 2);
            renderCube(quads, light, alpha, RIVETED_END, -2, y, -2);
        }
    }

    private static void renderPillar(LegacyTexturedQuadRenderer.SpritePixelQuadSink quads, int packedLight, int alpha,
            TextureAtlasSprite top, TextureAtlasSprite side, double x, double y, double z) {
        LegacyAtlasCuboidRenderer.smallBlock(top, top, side, side, side, side, quads, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFF, alpha,
                x, y, z);
    }

    private static void renderCube(LegacyTexturedQuadRenderer.SpritePixelQuadSink quads, int packedLight, int alpha,
            TextureAtlasSprite texture, double x, double y, double z) {
        LegacyAtlasCuboidRenderer.smallBlock(texture, texture, texture, texture, texture, texture, quads,
                packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, alpha,
                x, y, z);
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/" + name);
    }
}
