package com.hbm.ntm.client.overlay;

import com.hbm.ntm.ability.ToolAbilityConfiguration;
import com.hbm.ntm.ability.ToolAreaAbilities;
import com.hbm.ntm.ability.ToolPreset;
import com.hbm.ntm.client.screen.ToolAbilityScreen;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public final class ToolAbilityHudRenderer {
    private static final int ICON_SIZE = 16;
    private static final Map<Object, Uv> AREA_ICON_UV = Map.of(
            ToolAreaAbilities.RECURSION, new Uv(0, 138),
            ToolAreaAbilities.HAMMER, new Uv(16, 138),
            ToolAreaAbilities.HAMMER_FLAT, new Uv(32, 138),
            ToolAreaAbilities.EXPLOSION, new Uv(48, 138));

    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof HbmAbilityToolItem tool)) {
            return;
        }

        ToolAbilityConfiguration configuration = tool.getConfiguration(stack);
        ToolPreset preset = configuration.getActivePreset();
        Uv uv = AREA_ICON_UV.get(preset.areaAbility);
        if (uv == null) {
            return;
        }

        int x = event.getWindow().getGuiScaledWidth() / 2 - ICON_SIZE - 8 + HbmClientConfig.toolHudIndicatorX();
        int y = event.getWindow().getGuiScaledHeight() / 2 + 8 + HbmClientConfig.toolHudIndicatorY();
        GuiGraphics graphics = event.getGuiGraphics();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        graphics.blit(ToolAbilityScreen.TEXTURE, x, y, uv.u(), uv.v(), ICON_SIZE, ICON_SIZE, 256, 256);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private ToolAbilityHudRenderer() {
    }

    private record Uv(int u, int v) {
    }
}
