package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
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
import java.util.List;
import java.util.Locale;

public class MachineBatterySocketScreen extends AbstractContainerScreen<MachineBatterySocketMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_battery_socket.png");

    public MachineBatterySocketScreen(MachineBatterySocketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 181;
        inventoryLabelY = 87;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int powerHeight = menu.getPowerBarHeight(52);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 69 - powerHeight, 176, 52 - powerHeight, 34, powerHeight);
        }

        graphics.blit(TEXTURE, leftPos + 106, topPos + 16, 176, 52 + menu.getRedLow() * 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 106, topPos + 52, 176, 52 + menu.getRedHigh() * 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 125, topPos + 35, 194, 52 + priorityTextureIndex(menu.getPriority()) * 16, 16, 16);
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

        if (isHovering(62, 17, 34, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(energyTooltip()), mouseX, mouseY);
        } else if (isHovering(106, 16, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(modeTooltip("container.hbm_ntm_rebirth.battery.red_low", menu.getRedLow())), mouseX, mouseY);
        } else if (isHovering(106, 52, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(modeTooltip("container.hbm_ntm_rebirth.battery.red_high", menu.getRedHigh())), mouseX, mouseY);
        } else if (isHovering(125, 35, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(priorityTooltip()), mouseX, mouseY);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(106, 16, 18, 18, mouseX, mouseY)) {
            sendButton(MachineBatterySocketBlockEntity.CONTROL_RED_LOW);
            return true;
        } else if (isHovering(106, 52, 18, 18, mouseX, mouseY)) {
            sendButton(MachineBatterySocketBlockEntity.CONTROL_RED_HIGH);
            return true;
        } else if (isHovering(125, 35, 16, 16, mouseX, mouseY)) {
            sendButton(MachineBatterySocketBlockEntity.CONTROL_PRIORITY);
            return true;
        }
        return handled;
    }

    private void sendButton(int button) {
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), MachineBatterySocketBlockEntity.controlTag(button)));
    }

    private List<Component> energyTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(shortNumber(menu.getPower()) + "/" + shortNumber(menu.getMaxPower()) + "HE"));
        long delta = menu.getDelta();
        ChatFormatting color = delta > 0L ? ChatFormatting.GREEN : delta < 0L ? ChatFormatting.RED : ChatFormatting.YELLOW;
        String sign = delta >= 0L ? "+" : "-";
        tooltip.add(Component.literal(sign + shortNumber(Math.abs(delta)) + "HE/s").withStyle(color));
        return tooltip;
    }

    private static List<Component> modeTooltip(String labelKey, int mode) {
        return List.of(
                Component.translatable(labelKey),
                Component.translatable("container.hbm_ntm_rebirth.battery.mode." + modeName(mode)).withStyle(ChatFormatting.YELLOW));
    }

    private List<Component> priorityTooltip() {
        return List.of(
                Component.translatable("container.hbm_ntm_rebirth.battery.priority"),
                Component.translatable("container.hbm_ntm_rebirth.battery.priority." + priorityName(menu.getPriority())).withStyle(ChatFormatting.YELLOW),
                Component.translatable("container.hbm_ntm_rebirth.battery.priority.recommended").withStyle(ChatFormatting.GRAY));
    }

    private List<FormattedCharSequence> splitTooltip(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private static String modeName(int mode) {
        return switch (mode) {
            case MachineBatterySocketBlockEntity.MODE_BUFFER -> "buffer";
            case MachineBatterySocketBlockEntity.MODE_OUTPUT -> "output";
            case MachineBatterySocketBlockEntity.MODE_NONE -> "none";
            default -> "input";
        };
    }

    private static String priorityName(HbmEnergyReceiver.ConnectionPriority priority) {
        return switch (priority) {
            case NORMAL -> "normal";
            case HIGH -> "high";
            default -> "low";
        };
    }

    private static int priorityTextureIndex(HbmEnergyReceiver.ConnectionPriority priority) {
        return switch (priority) {
            case NORMAL -> 1;
            case HIGH -> 2;
            default -> 0;
        };
    }

    private static String shortNumber(long value) {
        double result;
        String suffix;
        double abs = Math.abs((double) value);
        if (abs >= 1_000_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (abs >= 1_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (abs >= 1_000_000_000_000.0D) {
            result = value / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (abs >= 1_000_000_000.0D) {
            result = value / 1_000_000_000.0D;
            suffix = "G";
        } else if (abs >= 1_000_000.0D) {
            result = value / 1_000_000.0D;
            suffix = "M";
        } else if (abs >= 1_000.0D) {
            result = value / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(value);
        }
        double rounded = result <= -100.0D ? Math.round(result * 10.0D) / 10.0D : Math.round(result * 100.0D) / 100.0D;
        return String.format(Locale.US, "%s%s", rounded, suffix);
    }
}
