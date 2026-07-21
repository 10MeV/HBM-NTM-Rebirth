package com.hbm.interfaces;

import net.minecraft.resources.ResourceLocation;

/**
 * Client-neutral drawing surface supplied by the client HUD dispatcher.
 */
public interface ItemHudRenderContext {
    int screenWidth();

    int screenHeight();

    int toolAbilityHudOffsetX();

    int toolAbilityHudOffsetY();

    void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height,
              int textureWidth, int textureHeight, BlendMode blendMode);

    enum BlendMode {
        DEFAULT,
        LEGACY_INVERTED
    }
}
