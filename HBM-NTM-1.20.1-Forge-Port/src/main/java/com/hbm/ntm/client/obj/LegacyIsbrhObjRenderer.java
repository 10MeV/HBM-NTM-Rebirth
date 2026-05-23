package com.hbm.ntm.client.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Bridge for old ISimpleBlockRenderingHandler OBJ paths that used ObjUtil with block icons.
 */
public final class LegacyIsbrhObjRenderer {
    public static void renderWithTexture(LegacyWavefrontModel model, ResourceLocation spriteTexture, ObjRenderContext context) {
        renderWithTexture(model, spriteTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderWithTexture(LegacyWavefrontModel model, ResourceLocation spriteTexture, ObjRenderContext context,
            float yawRadians, float pitchRadians, float rollRadians) {
        model.renderWithSprite(sprite(spriteTexture), context, yawRadians, pitchRadians, rollRadians);
    }

    public static void renderPartWithTexture(LegacyWavefrontModel model, String partName, ResourceLocation spriteTexture,
            ObjRenderContext context) {
        renderPartWithTexture(model, partName, spriteTexture, context, 0.0F, 0.0F, 0.0F);
    }

    public static void renderPartWithTexture(LegacyWavefrontModel model, String partName, ResourceLocation spriteTexture,
            ObjRenderContext context, float yawRadians, float pitchRadians, float rollRadians) {
        model.renderPartWithSprite(partName, sprite(spriteTexture), context, yawRadians, pitchRadians, rollRadians);
    }

    public static TextureAtlasSprite sprite(ResourceLocation textureLocation) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureLocation);
    }

    private LegacyIsbrhObjRenderer() {
    }
}
