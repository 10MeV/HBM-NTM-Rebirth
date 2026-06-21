package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.BalefireBombBlockEntity;
import com.hbm.ntm.menu.BalefireBombMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BalefireBombScreen extends AbstractContainerScreen<BalefireBombMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/fstbmbschematic.png");

    private EditBox timerField;

    public BalefireBombScreen(BalefireBombMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = BalefireBombMenu.IMAGE_WIDTH;
        imageHeight = BalefireBombMenu.IMAGE_HEIGHT;
        inventoryLabelX = BalefireBombMenu.PLAYER_INV_X;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        timerField = addRenderableWidget(new EditBox(font, leftPos + 94, topPos + 40, 29, 12, Component.empty()));
        timerField.setTextColor(0xFF0000);
        timerField.setTextColorUneditable(0x800000);
        timerField.setBordered(false);
        timerField.setMaxLength(3);
        timerField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        timerField.setValue(Integer.toString(Math.max(1, menu.getTimerSeconds())));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (timerField != null && !timerField.isFocused()) {
            timerField.setValue(Integer.toString(Math.max(1, menu.getTimerSeconds())));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.hasEgg()) {
            graphics.blit(TEXTURE, leftPos + 19, topPos + 90, 176, 0, 30, 16, 256, 256);
        }
        int battery = menu.getBattery();
        if (battery == 1) {
            graphics.blit(TEXTURE, leftPos + 88, topPos + 93, 176, 16, 18, 10, 256, 256);
        } else if (battery == 2) {
            graphics.blit(TEXTURE, leftPos + 88, topPos + 93, 194, 16, 18, 10, 256, 256);
        }
        if (menu.isStarted()) {
            graphics.blit(TEXTURE, leftPos + 142, topPos + 35, 176, 26, 18, 18, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        if (menu.hasBattery()) {
            String timer = menu.getMinutesText() + ":" + menu.getSecondsText();
            float scale = 0.75F;
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, scale);
            graphics.drawString(font, timer,
                    (int) ((69 - font.width(timer) / 2.0F) / scale),
                    (int) (95.5F / scale), 0xFF0000, false);
            graphics.pose().popPose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!menu.isStarted() && isStartButton(mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockPos(), 0, BalefireBombBlockEntity.CONTROL_START);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (timerField != null && timerField.keyPressed(keyCode, scanCode, modifiers)) {
            sendTimer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (timerField != null && timerField.charTyped(codePoint, modifiers)) {
            sendTimer();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private boolean isStartButton(double mouseX, double mouseY) {
        return mouseX >= leftPos + 142 && mouseX < leftPos + 160
                && mouseY > topPos + 35 && mouseY <= topPos + 53;
    }

    private void sendTimer() {
        if (timerField == null || timerField.getValue().isBlank()) {
            return;
        }
        int seconds;
        try {
            seconds = (int) Double.parseDouble(timerField.getValue());
        } catch (NumberFormatException ex) {
            return;
        }
        seconds = Math.max(1, Math.min(999, seconds));
        ModMessages.sendLegacyButton(menu.getBlockPos(), seconds, BalefireBombBlockEntity.CONTROL_TIMER_SECONDS);
    }
}
