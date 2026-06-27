package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FusionTorusStructCoreBlockEntity;
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

public class FusionTorusStructCoreRenderer implements BlockEntityRenderer<FusionTorusStructCoreBlockEntity> {
    private static final TextureAtlasSprite BSCCO_WELDED =
            sprite("legacy_blocks/fusion_component.bscco_welded");
    private static final TextureAtlasSprite BLANKET =
            sprite("legacy_blocks/fusion_component.blanket");
    private static final TextureAtlasSprite MOTOR =
            sprite("legacy_blocks/fusion_component.motor");

    public FusionTorusStructCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionTorusStructCoreBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FusionTorusStructCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> renderPreview(queuedPose, buffer, light));
    }

    private static void renderPreview(PoseStack poseStack, MultiBufferSource buffer, int light) {
        for (int y = 0; y < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_HEIGHT; y++) {
            for (int x = 0; x < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_SIZE; x++) {
                for (int z = 0; z < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_SIZE; z++) {
                    int component = FusionTorusStructCoreBlockEntity.legacyLayoutComponent(x, y, z);
                    if (component == 0) {
                        continue;
                    }
                    TextureAtlasSprite sprite = textureFor(component);
                    LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite,
                            poseStack, buffer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFF,
                            LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA,
                            LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                            x - FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_RADIUS,
                            y,
                            z - FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_RADIUS);
                }
            }
        }
    }

    private static TextureAtlasSprite textureFor(int component) {
        return switch (component) {
            case 2 -> BLANKET;
            case 3 -> MOTOR;
            default -> BSCCO_WELDED;
        };
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }
}
