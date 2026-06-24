package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FusionTorusStructCoreBlockEntity;
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

public class FusionTorusStructCoreRenderer implements BlockEntityRenderer<FusionTorusStructCoreBlockEntity> {
    private static final ResourceLocation BSCCO_WELDED =
            blockTexture("legacy_blocks/fusion_component.bscco_welded");
    private static final ResourceLocation BLANKET =
            blockTexture("legacy_blocks/fusion_component.blanket");
    private static final ResourceLocation MOTOR =
            blockTexture("legacy_blocks/fusion_component.motor");

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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, blockEntity.getBlockState(),
                LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight), OverlayTexture.NO_OVERLAY)
                .withTranslucencyNoDepthWrite()
                .withColor(0xFFFFFF, LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA);

        for (int y = 0; y < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_HEIGHT; y++) {
            for (int x = 0; x < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_SIZE; x++) {
                for (int z = 0; z < FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_SIZE; z++) {
                    int component = FusionTorusStructCoreBlockEntity.legacyLayoutComponent(x, y, z);
                    if (component == 0) {
                        continue;
                    }
                    TextureAtlasSprite sprite = textureFor(component);
                    LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite,
                            context,
                            x - FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_RADIUS,
                            y,
                            z - FusionTorusStructCoreBlockEntity.LEGACY_LAYOUT_RADIUS);
                }
            }
        }
    }

    private static TextureAtlasSprite textureFor(int component) {
        return LegacyTexturedQuadRenderer.blockSprite(switch (component) {
            case 2 -> BLANKET;
            case 3 -> MOTOR;
            default -> BSCCO_WELDED;
        });
    }

    private static ResourceLocation blockTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }
}
