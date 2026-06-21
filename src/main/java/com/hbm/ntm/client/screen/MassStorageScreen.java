package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.MassStorageMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;

public class MassStorageScreen extends AbstractContainerScreen<MassStorageMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/storage/gui_mass_storage.png");

    public MassStorageScreen(MassStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 221;
        this.inventoryLabelY = 127;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int gauge = menu.stockpile() * 88 / menu.capacity();
        graphics.blit(TEXTURE, leftPos + 97, topPos + 105 - gauge, 176, 88 - gauge, 16, gauge);
        if (menu.output()) {
            graphics.blit(TEXTURE, leftPos + 80, topPos + 72, 192, 0, 14, 14);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(96, 16, 18, 90, mouseX, mouseY)) {
            String percent = (((int) (menu.stockpile() * 1000D / (double) menu.capacity())) / 10D) + "%";
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal(String.format(Locale.US, "%,d", menu.stockpile()) + " / "
                            + String.format(Locale.US, "%,d", menu.capacity())),
                    Component.literal(percent)), mouseX, mouseY);
        }
        if (isHovering(62, 72, 14, 14, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("Click: Provide one"),
                    Component.literal("Shift-click: Provide stack")), mouseX, mouseY);
        }
        if (isHovering(80, 72, 14, 14, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("Toggle output")), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(62, 72, 14, 14, mouseX, mouseY)) {
            playButtonClick();
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("provide", Screen.hasShiftDown());
            ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
            return true;
        }
        if (isHovering(80, 72, 14, 14, mouseX, mouseY)) {
            playButtonClick();
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("toggle", false);
            ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
            return true;
        }
        return handled;
    }

    private void playButtonClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
