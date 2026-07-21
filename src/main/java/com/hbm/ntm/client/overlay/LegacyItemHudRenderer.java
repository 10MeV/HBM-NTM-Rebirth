package com.hbm.ntm.client.overlay;

import com.hbm.interfaces.IItemHUD;
import com.hbm.interfaces.ItemHudRenderContext;
import com.hbm.ntm.config.HbmClientConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

/** Client-only dispatcher for the modernized legacy {@link IItemHUD} contract. */
public final class LegacyItemHudRenderer {
    private LegacyItemHudRenderer() {
    }

    public static void renderCrosshair(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof IItemHUD hud) {
            hud.renderHUD(new Context(event), player, stack);
        }
    }

    private record Context(RenderGuiOverlayEvent.Post event) implements ItemHudRenderContext {
        @Override
        public int screenWidth() {
            return event.getWindow().getGuiScaledWidth();
        }

        @Override
        public int screenHeight() {
            return event.getWindow().getGuiScaledHeight();
        }

        @Override
        public int toolAbilityHudOffsetX() {
            return HbmClientConfig.toolHudIndicatorX();
        }

        @Override
        public int toolAbilityHudOffsetY() {
            return HbmClientConfig.toolHudIndicatorY();
        }

        @Override
        public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height,
                         int textureWidth, int textureHeight, BlendMode blendMode) {
            GuiGraphics graphics = event.getGuiGraphics();
            if (blendMode == BlendMode.LEGACY_INVERTED) {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);
                graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                return;
            }
            graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        }
    }
}
