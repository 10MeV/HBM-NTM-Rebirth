package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ResearchReactorMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class ResearchReactorScreen extends AbstractContainerScreen<ResearchReactorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_research_reactor.png");

    private EditBox levelField;
    private int buttonTimer;

    public ResearchReactorScreen(ResearchReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        levelField = new EditBox(font, leftPos + 8, topPos + 99, 33, 16, Component.empty());
        levelField.setBordered(false);
        levelField.setMaxLength(3);
        levelField.setValue(Integer.toString(menu.getTargetLevelPercent()));
        addRenderableWidget(levelField);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getLevel() <= 0.5D) {
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    graphics.blit(TEXTURE, leftPos + 81 + 36 * x, topPos + 26 + 36 * y, 176, 0, 8, 8);
                }
            }
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    graphics.blit(TEXTURE, leftPos + 99 + 36 * x, topPos + 44 + 36 * y, 176, 0, 8, 8);
                }
            }
        }
        if (buttonTimer > 0) {
            graphics.blit(TEXTURE, leftPos + 44, topPos + 97, 176, 8, 11, 20);
            buttonTimer--;
        }
        drawDisplay(graphics, 14, 25, menu.getTotalFlux(), 4);
        drawDisplay(graphics, 12, 63, menu.getTemperatureDisplay(), 3);
        drawDisplay(graphics, 5, 101, parseLevelField(), 3);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 121 - font.width(title) / 2, 6, 0xE5E5E5, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        graphics.drawString(font, "Flux", 6, 13, 0xE5E5E5, false);
        graphics.drawString(font, "Heat", 6, 51, 0xE5E5E5, false);
        graphics.drawString(font, "Control", 6, 89, 0xE5E5E5, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(-14, 23, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Water on the reactor sides cools it."), mouseX, mouseY);
        } else if (isHovering(-14, 61, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Plate fuel needs neutron flux to start."), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (leftPos + 44 <= mouseX && mouseX < leftPos + 55 && topPos + 97 < mouseY && mouseY <= topPos + 117) {
            sendLevel();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (levelField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return Character.isDigit(codePoint) && levelField.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    private void sendLevel() {
        int percent = parseLevelField();
        levelField.setValue(Integer.toString(percent));
        CompoundTag tag = new CompoundTag();
        tag.putDouble("level", percent * 0.01D);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
        buttonTimer = 15;
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.LEVER_CLICK, 0.5F, 1.0F);
        }
    }

    private int parseLevelField() {
        try {
            return Mth.clamp(Integer.parseInt(levelField.getValue()), 0, 100);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void drawDisplay(GuiGraphics graphics, int x, int y, int value, int digits) {
        String text = Integer.toString(Math.max(0, value));
        if (text.length() > digits) {
            text = text.substring(text.length() - digits);
        }
        graphics.drawString(font, text, x, y, 0x08FF00, false);
    }
}
