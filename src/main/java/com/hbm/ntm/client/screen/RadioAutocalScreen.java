package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RTTYAutocalState;
import com.hbm.ntm.blockentity.RadioAutocalBlockEntity;
import com.hbm.ntm.menu.RadioAutocalMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
        addControlButton(8, 36, "on", Component.literal("I"));
        addControlButton(28, 36, "ignore", Component.literal("!"));
        addControlButton(48, 36, "auto", Component.literal("A"));
        addRenderableWidget(Button.builder(Component.literal("Up"), button -> uploadClipboard())
                .bounds(leftPos + 84, topPos + 36, BUTTON_SIZE, BUTTON_SIZE)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cp"), button -> copyScript())
                .bounds(leftPos + 124, topPos + 36, BUTTON_SIZE, BUTTON_SIZE)
                .build());
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
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, statusLine(), 7, 61, 0x00AA00, false);
        String[] history = state().historyCopy();
        for (int i = 0; i < history.length; i++) {
            String line = history[i];
            if (line == null || line.isEmpty()) {
                continue;
            }
            graphics.drawString(font, trim(line, 156), 7, 73 + i * 10, 0x00FF00, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderButtonTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void addControlButton(int x, int y, String key, Component label) {
        addRenderableWidget(Button.builder(label, button -> sendFlag(key))
                .bounds(leftPos + x, topPos + y, BUTTON_SIZE, BUTTON_SIZE)
                .build());
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
                    Component.literal("Skips instructions that error.")), mouseX, mouseY);
        } else if (hovering(48, 36, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("Automatic Reboot").withStyle(ChatFormatting.RED),
                    Component.literal("Restarts when the program stops.")), mouseX, mouseY);
        } else if (hovering(84, 36, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Upload Clipboard").withStyle(ChatFormatting.BLUE),
                    mouseX, mouseY);
        } else if (hovering(124, 36, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Copy Program").withStyle(ChatFormatting.BLUE),
                    mouseX, mouseY);
        }
    }

    private boolean hovering(int x, int y, int mouseX, int mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, BUTTON_SIZE, BUTTON_SIZE);
    }

    private String statusLine() {
        RTTYAutocalState state = state();
        return "PC " + state.context().current()
                + "  CLK " + state.context().clockSpeed()
                + "  " + (state.isOn() ? "ON" : "OFF");
    }

    private RTTYAutocalState state() {
        RadioAutocalBlockEntity blockEntity = menu.getBlockEntity();
        return blockEntity.autocalState();
    }

    private String trim(String value, int width) {
        if (font.width(value) <= width) {
            return value;
        }
        String suffix = "...";
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end) + suffix) > width) {
            end--;
        }
        return value.substring(0, end) + suffix;
    }
}
