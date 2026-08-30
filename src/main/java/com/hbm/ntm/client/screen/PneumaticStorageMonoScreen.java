package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.PneumaticStorageMonoBlockEntity;
import com.hbm.ntm.menu.PneumaticStorageMonoMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class PneumaticStorageMonoScreen extends AbstractContainerScreen<PneumaticStorageMonoMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_pneumatic_mono.png");
    public PneumaticStorageMonoScreen(PneumaticStorageMonoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title); imageWidth = 200; imageHeight = 181; inventoryLabelY = imageHeight - 96 + 2;
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (int slot = 0; slot < PneumaticStorageMonoBlockEntity.SLOT_COUNT; slot++) {
            int width = menu.getAmount(slot) * 124 / PneumaticStorageMonoBlockEntity.CAPACITY;
            if (width > 0) graphics.blit(TEXTURE, leftPos + 44, topPos + 17 + slot * 18, 0, 181, width, 16);
        }
        int pressure = menu.getTankData().pressure();
        if (pressure > 0) graphics.blit(TEXTURE, leftPos + 174 + 4 * (pressure - 1), topPos + 36, 200, 0, 4, 8);
        double fill = menu.getTankData().capacity() <= 0 ? 0D : (double) menu.getTankData().fill() / menu.getTankData().capacity();
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 184, topPos + 25, fill, 5, 2, 1, 0xFFCA6C43, 0xFFAB4223);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 88 - font.width(title) / 2, 5, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        for (int slot = 0; slot < PneumaticStorageMonoBlockEntity.SLOT_COUNT; slot++) if (menu.getSlot(slot).hasItem()) {
            int amount = menu.getAmount(slot); String text = String.format(Locale.US, "%,d", amount) + " (" + ((int) (amount * 1000D / PneumaticStorageMonoBlockEntity.CAPACITY) / 10D) + "%)";
            graphics.drawString(font, text, 50, 22 + slot * 18, 0x000000, false);
        }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(174, 36, 20, 8, mouseX, mouseY)) graphics.renderComponentTooltip(font, List.of(
                Component.literal("Compressor: " + menu.getTankData().pressure() + " PU"), Component.literal("Max range: " + menu.getRangeFromPressure() + "m")), mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!isHovering(174, 36, 20, 8, mouseX, mouseY)) return handled;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
        CompoundTag tag = new CompoundTag(); tag.putBoolean("pressure", true);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag)); return true;
    }
}
