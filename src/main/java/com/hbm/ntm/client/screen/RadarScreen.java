package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RadarScreen extends AbstractContainerScreen<RadarMenu> {
    private static final ResourceLocation LINK_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_link.png");
    private static final ResourceLocation RADAR_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_nt.png");
    private static final int[] CONTROLS = {
            RadarBlockEntity.CONTROL_SCAN_MISSILES,
            RadarBlockEntity.CONTROL_SCAN_SHELLS,
            RadarBlockEntity.CONTROL_SCAN_PLAYERS,
            RadarBlockEntity.CONTROL_SMART_MODE,
            RadarBlockEntity.CONTROL_RED_MODE,
            RadarBlockEntity.CONTROL_SHOW_MAP
    };
    private static final String[] TOOLTIP_KEYS = {
            "radar.detectMissiles",
            "radar.detectShells",
            "radar.detectPlayers",
            "radar.smartMode",
            "radar.redMode",
            "radar.showMap"
    };

    public RadarScreen(RadarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 184;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(LINK_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int powerWidth = menu.getPowerBarWidth(160);
        if (powerWidth > 0) {
            graphics.blit(LINK_TEXTURE, leftPos + 8, topPos + 64, 0, 185, powerWidth, 16);
        }
        renderToggleStrip(graphics);
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
        renderHoveredTooltips(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        for (int index = 0; index < CONTROLS.length; index++) {
            if (isHovering(toggleX(index), 82, 8, 8, mouseX, mouseY)) {
                sendButton(CONTROLS[index]);
                return true;
            }
        }
        return handled;
    }

    private void renderToggleStrip(GuiGraphics graphics) {
        for (int index = 0; index < CONTROLS.length; index++) {
            int x = leftPos + toggleX(index);
            int y = topPos + 82;
            int border = active(index) ? 0xFF1F8F32 : 0xFF4C4C4C;
            graphics.fill(x - 1, y - 1, x + 9, y + 9, 0xFF101010);
            graphics.fill(x - 1, y - 1, x + 9, y, border);
            graphics.fill(x - 1, y + 8, x + 9, y + 9, border);
            graphics.fill(x - 1, y, x, y + 8, border);
            graphics.fill(x + 8, y, x + 9, y + 8, border);
            if (active(index)) {
                graphics.blit(RADAR_TEXTURE, x, y, 238, 4 + index * 10, 8, 8);
            }
        }
    }

    private void renderHoveredTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(8, 64, 160, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"),
                    Component.literal("Redstone: " + menu.getRedPower()).withStyle(ChatFormatting.RED)
            )), mouseX, mouseY);
            return;
        }
        for (int index = 0; index < TOOLTIP_KEYS.length; index++) {
            if (isHovering(toggleX(index), 82, 8, 8, mouseX, mouseY)) {
                List<Component> tooltip = localizedLines(TOOLTIP_KEYS[index]);
                tooltip.add(Component.translatable(active(index)
                        ? "container.hbm_ntm_rebirth.radar.enabled"
                        : "container.hbm_ntm_rebirth.radar.disabled")
                        .withStyle(active(index) ? ChatFormatting.GREEN : ChatFormatting.RED));
                graphics.renderTooltip(font, split(tooltip), mouseX, mouseY);
                return;
            }
        }
    }

    private boolean active(int index) {
        return switch (index) {
            case 0 -> menu.scanMissiles();
            case 1 -> menu.scanShells();
            case 2 -> menu.scanPlayers();
            case 3 -> menu.smartMode();
            case 4 -> menu.redMode();
            case 5 -> menu.showMap();
            default -> false;
        };
    }

    private static int toggleX(int index) {
        return 52 + index * 12;
    }

    private void sendButton(int button) {
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(),
                RadarBlockEntity.controlTag(button)));
    }

    private static List<FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private static List<Component> localizedLines(String key) {
        return new ArrayList<>(Arrays.stream(Component.translatable(key).getString().split("\\$"))
                .map(Component::literal)
                .toList());
    }
}
