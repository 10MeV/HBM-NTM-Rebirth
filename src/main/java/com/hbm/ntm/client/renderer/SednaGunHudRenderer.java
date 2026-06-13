package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.LegacySednaGunConfigs;
import com.hbm.ntm.item.SednaGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SednaGunHudRenderer {
    private static final ResourceLocation OVERLAY_MISC_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, Player player) {
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SednaGunItem gun)) {
            return;
        }
        for (SednaGunItem.LegacyHudComponent component : gun.legacyHudComponents(stack, player)) {
            if (isDurability(component.componentName())) {
                renderDurability(graphics, screenWidth, screenHeight, component);
            } else if (isAmmo(component.componentName())) {
                renderAmmo(graphics, screenWidth, screenHeight, component);
            }
        }
    }

    private static void renderDurability(GuiGraphics graphics, int screenWidth, int screenHeight,
            SednaGunItem.LegacyHudComponent component) {
        int x = screenWidth / 2 + (isMirror(component.componentName()) ? -(62 + 36 + 52) : (62 + 36));
        int y = screenHeight - 21;
        int progress = Mth.clamp(50 - component.durabilityLoss(), 0, 50);
        LegacyScreenQuadRenderer.blit(OVERLAY_MISC_TEXTURE, graphics, x, y + 16, 94, 0, 52, 3);
        if (progress > 0) {
            LegacyScreenQuadRenderer.blit(OVERLAY_MISC_TEXTURE, graphics, x + 1, y + 16, 95, 3, progress, 3);
        }
    }

    private static void renderAmmo(GuiGraphics graphics, int screenWidth, int screenHeight,
            SednaGunItem.LegacyHudComponent component) {
        boolean noCounter = LegacySednaGunConfigs.HUD_COMPONENT_AMMO_NOCOUNTER.equals(component.componentName());
        int x = screenWidth / 2 + (isMirror(component.componentName()) ? -(62 + 36 + 52) : (62 + 36))
                + (noCounter ? 14 : 0);
        int y = screenHeight - component.bottomOffset() - 18;
        if (!noCounter) {
            graphics.drawString(Minecraft.getInstance().font, component.ammoText(), x + 17, y + 6, 0xFFFFFF, false);
        }
        if (!component.ammoIcon().isEmpty()) {
            graphics.renderItem(component.ammoIcon(), x, y);
        }
    }

    private static boolean isDurability(String componentName) {
        return LegacySednaGunConfigs.HUD_COMPONENT_DURABILITY.equals(componentName)
                || LegacySednaGunConfigs.HUD_COMPONENT_DURABILITY_MIRROR.equals(componentName);
    }

    private static boolean isAmmo(String componentName) {
        return LegacySednaGunConfigs.HUD_COMPONENT_AMMO.equals(componentName)
                || LegacySednaGunConfigs.HUD_COMPONENT_AMMO_MIRROR.equals(componentName)
                || LegacySednaGunConfigs.HUD_COMPONENT_AMMO_NOCOUNTER.equals(componentName)
                || LegacySednaGunConfigs.HUD_COMPONENT_AMMO_SECOND.equals(componentName);
    }

    private static boolean isMirror(String componentName) {
        return LegacySednaGunConfigs.HUD_COMPONENT_DURABILITY_MIRROR.equals(componentName)
                || LegacySednaGunConfigs.HUD_COMPONENT_AMMO_MIRROR.equals(componentName);
    }

    private SednaGunHudRenderer() {
    }
}
