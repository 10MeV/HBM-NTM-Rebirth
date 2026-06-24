package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RadioReceiverBlockEntity;
import com.hbm.ntm.menu.RadioReceiverMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class RadioReceiverScreen extends AbstractContainerScreen<RadioReceiverMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/machine/gui_radio.png");

    private EditBox frequency;

    public RadioReceiverScreen(RadioReceiverMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 42;
        titleLabelY = 1000;
        inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        RadioReceiverBlockEntity receiver = menu.getBlockEntity();
        frequency = LegacyGuiElements.createLegacyTextField(font, leftPos + 29, topPos + 21, 82, 14, 10,
                receiver.channel(), 0x00FF00);
        frequency.setBordered(false);
        addRenderableWidget(frequency);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getBlockEntity().isOn()) {
            graphics.blit(TEXTURE, leftPos + 173, topPos + 17, 0, 42, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hovering(mouseX, mouseY, 137, 17)) {
            graphics.renderTooltip(font, Component.literal("Save Settings"), mouseX, mouseY);
        } else if (hovering(mouseX, mouseY, 173, 17)) {
            graphics.renderTooltip(font, Component.literal("Toggle"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (hovering(mouseX, mouseY, 137, 17)) {
            playClick();
            CompoundTag tag = new CompoundTag();
            tag.putString("channel", frequency.getValue());
            ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
            return true;
        }
        if (hovering(mouseX, mouseY, 173, 17)) {
            playClick();
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("isOn", !menu.getBlockEntity().isOn());
            ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (frequency.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return frequency.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    private boolean hovering(double mouseX, double mouseY, int x, int y) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, 18, 18);
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
