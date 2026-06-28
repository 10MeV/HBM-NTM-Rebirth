package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.BedrockOreDepositBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class BedrockOreDepositRenderer implements BlockEntityRenderer<BedrockOreDepositBlockEntity> {
    private static final double SURFACE_OFFSET = 0.001D;
    private static final TextureAtlasSprite[] OVERLAYS = new TextureAtlasSprite[10];

    static {
        for (int i = 0; i < OVERLAYS.length; i++) {
            OVERLAYS[i] = sprite("ore_random_" + (i + 1));
        }
    }

    public BedrockOreDepositRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BedrockOreDepositBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        TextureAtlasSprite overlay = OVERLAYS[Math.floorMod(blockEntity.getShape(), OVERLAYS.length)];
        LegacyAtlasCuboidRenderer.croppedCuboid(overlay, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, blockEntity.getColor(), 255, LegacyTexturedRenderMode.CUTOUT_CULL,
                -SURFACE_OFFSET, -SURFACE_OFFSET, -SURFACE_OFFSET,
                1.0D + SURFACE_OFFSET, 1.0D + SURFACE_OFFSET, 1.0D + SURFACE_OFFSET);
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }
}
