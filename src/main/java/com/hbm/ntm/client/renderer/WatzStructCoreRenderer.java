package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.WatzStructCoreBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class WatzStructCoreRenderer implements BlockEntityRenderer<WatzStructCoreBlockEntity> {
    private static final ResourceLocation ELEMENT_TOP = blockTexture("legacy_blocks/watz_element_top");
    private static final ResourceLocation ELEMENT_SIDE = blockTexture("legacy_blocks/watz_element_side");
    private static final ResourceLocation COOLER_TOP = blockTexture("legacy_blocks/watz_cooler_top");
    private static final ResourceLocation COOLER_SIDE = blockTexture("legacy_blocks/watz_cooler_side");
    private static final ResourceLocation RIVETED_END = blockTexture("legacy_blocks/watz_casing_bolted");

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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, blockEntity.getBlockState(),
                LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight), OverlayTexture.NO_OVERLAY)
                .withTranslucencyNoDepthWrite()
                .withColor(0xFFFFFF, LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA);

        renderPillar(context, COOLER_TOP, COOLER_SIDE, 0, 1, 0);
        renderPillar(context, COOLER_TOP, COOLER_SIDE, 0, 2, 0);

        for (int y = 0; y < 3; y++) {
            for (int[] offset : ELEMENT_OFFSETS) {
                renderPillar(context, ELEMENT_TOP, ELEMENT_SIDE, offset[0], y, offset[1]);
            }
            for (int[] offset : COOLER_OFFSETS) {
                renderPillar(context, COOLER_TOP, COOLER_SIDE, offset[0], y, offset[1]);
            }
            for (int z = -1; z < 2; z++) {
                renderCube(context, RIVETED_END, 3, y, z);
                renderCube(context, RIVETED_END, z, y, 3);
                renderCube(context, RIVETED_END, -3, y, z);
                renderCube(context, RIVETED_END, z, y, -3);
            }
            renderCube(context, RIVETED_END, 2, y, 2);
            renderCube(context, RIVETED_END, 2, y, -2);
            renderCube(context, RIVETED_END, -2, y, 2);
            renderCube(context, RIVETED_END, -2, y, -2);
        }
    }

    private static void renderPillar(ObjRenderContext context, ResourceLocation topBottom, ResourceLocation side,
            double x, double y, double z) {
        TextureAtlasSprite top = LegacyTexturedQuadRenderer.blockSprite(topBottom);
        TextureAtlasSprite sideSprite = LegacyTexturedQuadRenderer.blockSprite(side);
        LegacyAtlasCuboidRenderer.smallBlock(top, top, sideSprite, sideSprite, sideSprite, sideSprite,
                context, x, y, z);
    }

    private static void renderCube(ObjRenderContext context, ResourceLocation texture, double x, double y, double z) {
        TextureAtlasSprite sprite = LegacyTexturedQuadRenderer.blockSprite(texture);
        LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite, context, x, y, z);
    }

    private static ResourceLocation blockTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }
}
