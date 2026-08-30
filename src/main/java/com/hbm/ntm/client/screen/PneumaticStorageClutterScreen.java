package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.PneumaticStorageClutterMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class PneumaticStorageClutterScreen extends AbstractContainerScreen<PneumaticStorageClutterMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/storage/gui_pneumatic_clutter.png");

    public PneumaticStorageClutterScreen(PneumaticStorageClutterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 200;
        imageHeight = 235;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int pressure = menu.getTankData().pressure();
        if (pressure > 0) {
            graphics.blit(TEXTURE, leftPos + 174 + 4 * (pressure - 1), topPos + 36, 200, 0, 4, 8);
        }
        double fill = menu.getTankData().capacity() <= 0
                ? 0.0D
                : (double) menu.getTankData().fill() / (double) menu.getTankData().capacity();
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 184, topPos + 25, fill,
                5, 2, 1, 0xFFCA6C43, 0xFFAB4223);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 88 - font.width(title) / 2, 5, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(174, 36, 20, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, java.util.List.of(
                    Component.literal("Compressor: " + menu.getTankData().pressure() + " PU"),
                    Component.literal("Max range: " + menu.getRangeFromPressure() + "m")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!isHovering(174, 36, 20, 8, mouseX, mouseY)) {
            return handled;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("pressure", true);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
        return true;
    }
}
