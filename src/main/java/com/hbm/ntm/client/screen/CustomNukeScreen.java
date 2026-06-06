package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CustomNukeBlockEntity.CustomNukeStats;
import com.hbm.ntm.explosion.CustomNukeExplosion;
import com.hbm.ntm.menu.CustomNukeMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CustomNukeScreen extends AbstractContainerScreen<CustomNukeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/gun_bomb_schematic.png");

    public CustomNukeScreen(CustomNukeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = CustomNukeMenu.IMAGE_WIDTH;
        imageHeight = CustomNukeMenu.IMAGE_HEIGHT;
        inventoryLabelX = CustomNukeMenu.PLAYER_INV_X;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        renderStageIcons(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderStageTooltip(graphics, mouseX, mouseY);
    }

    private void renderStageIcons(GuiGraphics graphics) {
        CustomNukeStats stats = menu.stats();
        if (stats.euph() > 0.0F) {
            blitIcon(graphics, 142, 89, 108);
        } else if (stats.schrab() > 0.0F) {
            blitIcon(graphics, 106, 89, 90);
        } else if (stats.amat() > 0.0F) {
            blitIcon(graphics, 70, 89, 54);
        } else if (stats.hydro() > 0.0F) {
            blitIcon(graphics, 52, 89, 36);
        } else if (stats.nuke() > 0.0F) {
            blitIcon(graphics, 34, 89, 18);
        } else if (stats.tnt() > 0.0F) {
            blitIcon(graphics, 16, 89, 0);
        }

        if (menu.hasDirtyOverlay()) {
            blitIcon(graphics, 88, 89, 72);
        }
    }

    private void renderStageTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int localX = mouseX - leftPos;
        int localY = mouseY - topPos;
        if (localY < 89 || localY >= 107) {
            return;
        }

        CustomNukeStats stats = menu.stats();
        List<Component> lines = null;
        if (inside(localX, 16)) {
            lines = List.of(
                    line("Conventional Explosives", stats.tnt(), Math.min(stats.tnt(), CustomNukeExplosion.MAX_TNT), ChatFormatting.YELLOW),
                    Component.literal("Caps at " + CustomNukeExplosion.MAX_TNT),
                    Component.literal("N2-like above level 75"),
                    Component.literal("\"Goes boom\"").withStyle(ChatFormatting.ITALIC));
        } else if (inside(localX, 34)) {
            lines = List.of(
                    line("Nuclear", stats.nuke(), stats.adjustedNuke(), ChatFormatting.YELLOW),
                    Component.literal("Requires TNT level 16"),
                    Component.literal("Caps at " + CustomNukeExplosion.MAX_NUKE),
                    Component.literal("Has fallout"));
        } else if (inside(localX, 52)) {
            lines = List.of(
                    line("Thermonuclear", stats.hydro(), stats.adjustedHydro(), ChatFormatting.YELLOW),
                    Component.literal("Requires nuclear level 100"),
                    Component.literal("Caps at " + CustomNukeExplosion.MAX_HYDRO),
                    Component.literal("Salted fallout contribution is reduced by 75%"));
        } else if (inside(localX, 70)) {
            lines = List.of(
                    line("Antimatter", stats.amat(), stats.adjustedAmat(), ChatFormatting.YELLOW),
                    Component.literal("Requires nuclear level 50"),
                    Component.literal("Caps at " + CustomNukeExplosion.MAX_AMAT));
        } else if (inside(localX, 88)) {
            lines = List.of(
                    line("Salted", stats.dirty(), Math.min(stats.dirty(), 100.0F), ChatFormatting.YELLOW),
                    Component.literal("Extends nuclear and thermonuclear fallout"),
                    Component.literal("Caps at 100"));
        } else if (inside(localX, 106)) {
            lines = List.of(
                    line("Schrabidium", stats.schrab(), stats.adjustedSchrab(), ChatFormatting.YELLOW),
                    Component.literal("Requires nuclear level 50"),
                    Component.literal("Caps at " + CustomNukeExplosion.MAX_SCHRAB));
        } else if (inside(localX, 142)) {
            lines = List.of(
                    Component.literal("Ice cream (Level unknown)").withStyle(ChatFormatting.YELLOW),
                    Component.literal("\"Probably not ice cream but the label came off.\"").withStyle(ChatFormatting.ITALIC));
        }

        if (lines != null) {
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

    private static boolean inside(int localX, int left) {
        return localX >= left && localX < left + 18;
    }

    private static Component line(String name, float raw, float adjusted, ChatFormatting color) {
        return Component.literal(name + " (Level " + fmt(raw) + "/" + fmt(adjusted) + ")").withStyle(color);
    }

    private static String fmt(float value) {
        if (Math.abs(value - Math.round(value)) < 0.05F) {
            return Integer.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private void blitIcon(GuiGraphics graphics, int x, int y, int v) {
        graphics.blit(TEXTURE, leftPos + x, topPos + y, 176, v, 18, 18, 256, 256);
    }
}
