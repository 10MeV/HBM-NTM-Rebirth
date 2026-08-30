package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.menu.BatteryReddMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BatteryReddScreen extends AbstractContainerScreen<BatteryReddMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_battery_redd.png");
    private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance(Locale.US);

    public BatteryReddScreen(BatteryReddMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 181;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(TEXTURE, leftPos + 133, topPos + 16, 176, 52 + menu.getRedLow() * 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 133, topPos + 52, 176, 52 + menu.getRedHigh() * 18, 18, 18);
        graphics.blit(TEXTURE, leftPos + 152, topPos + 35, 194,
                52 + priorityTextureIndex(menu.getPriority()) * 16, 16, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        String power = NUMBER.format(menu.getPowerBig()) + " HE";
        graphics.drawString(font, power, 242 - font.width(power), 45, 0x00ff00, false);
        BigInteger delta = menu.getDeltaBig();
        String deltaText = (delta.signum() >= 0 ? "+" : "") + NUMBER.format(delta) + " HE/s";
        ChatFormatting color = delta.signum() > 0 ? ChatFormatting.GREEN
                : delta.signum() < 0 ? ChatFormatting.RED : ChatFormatting.YELLOW;
        graphics.drawString(font, Component.literal(deltaText).withStyle(color),
                242 - font.width(deltaText), 65, 0x00ff00, false);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 133, 16, 18, 18)) {
            sendButton(BatteryReddBlockEntity.CONTROL_RED_LOW);
            return true;
        }
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 133, 52, 18, 18)) {
            sendButton(BatteryReddBlockEntity.CONTROL_RED_HIGH);
            return true;
        }
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 152, 35, 16, 16)) {
            sendButton(BatteryReddBlockEntity.CONTROL_PRIORITY);
            return true;
        }
        return handled;
    }

    private void sendButton(int button) {
        LegacyGuiElements.playClickSound();
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(),
                BatteryReddBlockEntity.controlTag(button)));
    }

    private static int priorityTextureIndex(HbmEnergyReceiver.ConnectionPriority priority) {
        return switch (priority) {
            case NORMAL -> 1;
            case HIGH -> 2;
            default -> 0;
        };
    }
}
