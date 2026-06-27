package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.DfcEmitterMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.util.BobMathUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DfcEmitterScreen extends AbstractContainerScreen<DfcEmitterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/dfc/gui_emitter.png");
    private EditBox wattsField;
    private boolean updatingField;

    public DfcEmitterScreen(DfcEmitterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        wattsField = LegacyGuiElements.createLegacyTextField(font, leftPos + 57, topPos + 57, 29, 12, 3,
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
        if (wattsField != null && wattsField.isFocused()) graphics.blit(TEXTURE, leftPos + 53, topPos + 53, 210, 4, 34, 16);
        if (menu.isOn()) graphics.blit(TEXTURE, leftPos + 133, topPos + 52, 192, 0, 18, 18);
        graphics.blit(TEXTURE, leftPos + 53, topPos + 45, 210, 0, menu.getWatts() * 34 / 100, 4);
        int power = (int) (menu.getPower() * 52L / DfcEmitterBlockEntity.MAX_POWER);
        graphics.blit(TEXTURE, leftPos + 26, topPos + 69 - power, 176, 52 - power, 16, power);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 17, 16, 52, menu.getCryogel());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (leftPos + 97 <= mouseX && mouseX < leftPos + 115 && topPos + 52 < mouseY && mouseY <= topPos + 70) {
            sendWatts();
            return true;
        }
        if (leftPos + 133 <= mouseX && mouseX < leftPos + 151 && topPos + 52 < mouseY && mouseY <= topPos + 70) {
            ModMessages.sendToServer(ModMessages.auxButtonPacket(menu.getBlockEntity().getBlockPos(), 0,
                    DfcEmitterBlockEntity.CONTROL_TOGGLE));
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
                DfcEmitterBlockEntity.CONTROL_SET_WATTS));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiElements.drawCenteredLabel(graphics, font, title, 0, 6, imageWidth, 0x404040);
        graphics.drawString(font, "Output: " + BobMathUtil.getShortNumber(menu.getPrev()) + "Spk", 50, 30, 0xFF7F7F, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getCryogel().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        } else if (isHovering(26, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 26, topPos + 17, 16, 52, menu.getPower(), DfcEmitterBlockEntity.MAX_POWER);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
