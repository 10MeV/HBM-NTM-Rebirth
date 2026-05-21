package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.menu.MachineBatteryMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MachineBatteryScreen extends AbstractContainerScreen<MachineBatteryMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_battery.png");

    public MachineBatteryScreen(MachineBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int powerHeight = menu.getPowerBarHeight(52);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 69 - powerHeight, 176, 52 - powerHeight, 52, powerHeight);
        }

        graphics.blit(TEXTURE, leftPos + 133, topPos + 16, 176, 52 + menu.getRedLow() * 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 133, topPos + 52, 176, 52 + menu.getRedHigh() * 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 152, topPos + 35, 194, 52 + priorityTextureIndex(menu.getPriority()) * 16, 16, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString() + " (" + menu.getPower() + " HE)";
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (isHovering(62, 17, 52, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(energyTooltip()), mouseX, mouseY);
        } else if (isHovering(133, 16, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(modeTooltip("container.hbm.battery.red_low", menu.getRedLow())), mouseX, mouseY);
        } else if (isHovering(133, 52, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(modeTooltip("container.hbm.battery.red_high", menu.getRedHigh())), mouseX, mouseY);
        } else if (isHovering(152, 35, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(priorityTooltip()), mouseX, mouseY);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(133, 16, 18, 18, mouseX, mouseY)) {
            sendButton(MachineBatteryBlockEntity.CONTROL_RED_LOW);
            return true;
        } else if (isHovering(133, 52, 18, 18, mouseX, mouseY)) {
            sendButton(MachineBatteryBlockEntity.CONTROL_RED_HIGH);
            return true;
        } else if (isHovering(152, 35, 16, 16, mouseX, mouseY)) {
            sendButton(MachineBatteryBlockEntity.CONTROL_PRIORITY);
            return true;
        }
        return handled;
    }

    private void sendButton(int button) {
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), MachineBatteryBlockEntity.controlTag(button)));
    }

    private List<Component> energyTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"));
        long delta = menu.getDelta();
        ChatFormatting color = delta > 0L ? ChatFormatting.GREEN : delta < 0L ? ChatFormatting.RED : ChatFormatting.YELLOW;
        String sign = delta >= 0L ? "+" : "-";
        tooltip.add(Component.literal(sign + Math.abs(delta) + " HE/s").withStyle(color));
        return tooltip;
    }

    private static List<Component> modeTooltip(String labelKey, int mode) {
        return List.of(
                Component.translatable(labelKey),
                Component.translatable("container.hbm.battery.mode." + modeName(mode)).withStyle(ChatFormatting.YELLOW));
    }

    private List<FormattedCharSequence> splitTooltip(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private List<Component> priorityTooltip() {
        return List.of(
                Component.translatable("container.hbm.battery.priority"),
                Component.translatable("container.hbm.battery.priority." + priorityName(menu.getPriority())).withStyle(ChatFormatting.YELLOW),
                Component.translatable("container.hbm.battery.priority.recommended").withStyle(ChatFormatting.GRAY));
    }

    private static String modeName(int mode) {
        return switch (mode) {
            case MachineBatteryBlockEntity.MODE_BUFFER -> "buffer";
            case MachineBatteryBlockEntity.MODE_OUTPUT -> "output";
            case MachineBatteryBlockEntity.MODE_NONE -> "none";
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
}
