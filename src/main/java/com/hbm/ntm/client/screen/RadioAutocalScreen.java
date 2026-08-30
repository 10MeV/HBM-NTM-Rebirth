package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RTTYAutocalState;
import com.hbm.ntm.blockentity.RadioAutocalBlockEntity;
import com.hbm.ntm.menu.RadioAutocalMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class RadioAutocalScreen extends AbstractContainerScreen<RadioAutocalMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_autocal.png");
    private static final int BUTTON_SIZE = 18;

    public RadioAutocalScreen(RadioAutocalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 170;
        imageHeight = 138;
        titleLabelY = 7;
        inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RTTYAutocalState state = state();
        if (state.isOn()) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 36, imageWidth, 0, BUTTON_SIZE, BUTTON_SIZE);
        }
        if (!state.ignoreError()) {
            graphics.blit(TEXTURE, leftPos + 28, topPos + 36, imageWidth, 18, BUTTON_SIZE, BUTTON_SIZE);
        }
        if (!state.autoReboot()) {
            graphics.blit(TEXTURE, leftPos + 48, topPos + 36, imageWidth, 36, BUTTON_SIZE, BUTTON_SIZE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String[] history = state().historyCopy();
        for (int i = 0; i < history.length; i++) {
            String line = history[i];
            if (line == null || line.isEmpty()) {
                continue;
            }
            graphics.drawString(font, line, 7, 73 + i * 10, 0x00FF00, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderButtonTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovering(8, 36, mouseX, mouseY)) {
            LegacyGuiElements.playClickSound();
            sendFlag("on");
            return true;
        }
        if (hovering(28, 36, mouseX, mouseY)) {
            LegacyGuiElements.playClickSound();
            sendFlag("ignore");
            return true;
        }
        if (hovering(48, 36, mouseX, mouseY)) {
            LegacyGuiElements.playClickSound();
            sendFlag("auto");
            return true;
        }
        if (hovering(84, 36, mouseX, mouseY)) {
            LegacyGuiElements.playClickSound();
            uploadClipboard();
            return true;
        }
        if (hovering(124, 36, mouseX, mouseY)) {
            LegacyGuiElements.playClickSound();
            copyScript();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendFlag(String key) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void uploadClipboard() {
        String payload = Minecraft.getInstance().keyboardHandler.getClipboard();
        CompoundTag tag = new CompoundTag();
        tag.putString("payload", payload == null ? "" : payload);
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void copyScript() {
        Minecraft.getInstance().keyboardHandler.setClipboard(state().scriptText());
    }

    private void renderButtonTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hovering(8, 36, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("ON/OFF").withStyle(ChatFormatting.RED), mouseX, mouseY);
        } else if (hovering(28, 36, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("Ignore Errors").withStyle(ChatFormatting.RED),
                    Component.literal("Skips instructions that error,"),
                    Component.literal("leaving the computer turned on."),
                    Component.literal("May cause unintended behavior"),
                    Component.literal("and inconsistencies.")), mouseX, mouseY);
        } else if (hovering(48, 36, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("Automatic Reboot").withStyle(ChatFormatting.RED),
                    Component.literal("Restarts the computer automatically when"),
                    Component.literal("the program stops due to an error"),
                    Component.literal("or after finishing.")), mouseX, mouseY);
        } else if (hovering(84, 36, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Upload Program").withStyle(ChatFormatting.BLUE),
                    mouseX, mouseY);
        } else if (hovering(124, 36, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Download Program").withStyle(ChatFormatting.BLUE),
                    mouseX, mouseY);
        }
    }

    private boolean hovering(int x, int y, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, BUTTON_SIZE, BUTTON_SIZE);
    }

    private RTTYAutocalState state() {
        RadioAutocalBlockEntity blockEntity = menu.getBlockEntity();
        return blockEntity.autocalState();
    }

}
