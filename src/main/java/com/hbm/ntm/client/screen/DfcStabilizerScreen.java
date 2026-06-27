package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.menu.DfcStabilizerMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DfcStabilizerScreen extends AbstractContainerScreen<DfcStabilizerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/dfc/gui_stabilizer.png");
    private EditBox wattsField;
    private boolean updatingField;

    public DfcStabilizerScreen(DfcStabilizerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        wattsField = LegacyGuiElements.createLegacyTextField(font, leftPos + 75, topPos + 57, 29, 12, 3,
                Integer.toString(menu.getWatts()));
        wattsField.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        addRenderableWidget(wattsField);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (wattsField != null && !wattsField.isFocused()) {
            String value = Integer.toString(menu.getWatts());
            if (!value.equals(wattsField.getValue())) {
                updatingField = true;
                wattsField.setValue(value);
                updatingField = false;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (wattsField != null && wattsField.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 71, topPos + 53, 192, 4, 34, 16);
        }
        graphics.blit(TEXTURE, leftPos + 71, topPos + 45, 192, 0, menu.getWatts() * 34 / 100, 4);
        int power = (int) (menu.getPower() * 52L / DfcStabilizerBlockEntity.MAX_POWER);
        graphics.blit(TEXTURE, leftPos + 35, topPos + 69 - power, 176, 52 - power, 16, power);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (leftPos + 124 <= mouseX && mouseX < leftPos + 142 && topPos + 52 < mouseY && mouseY <= topPos + 70) {
            sendWatts();
            return true;
        }
        return handled;
    }

    private void sendWatts() {
        if (updatingField || wattsField == null) return;
        int value;
        try {
            value = Integer.parseInt(wattsField.getValue());
        } catch (NumberFormatException ignored) {
            value = 1;
        }
        value = Math.max(1, Math.min(100, value));
        wattsField.setValue(Integer.toString(value));
        ModMessages.sendToServer(ModMessages.auxButtonPacket(menu.getBlockEntity().getBlockPos(), value,
                DfcStabilizerBlockEntity.CONTROL_SET_WATTS));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, 0, 6, imageWidth, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(35, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 35, topPos + 17, 16, 52, menu.getPower(), DfcStabilizerBlockEntity.MAX_POWER);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
