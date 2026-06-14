package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.HeaterHeatexMenu;
import com.hbm.ntm.network.ModMessages;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HeaterHeatexScreen extends AbstractContainerScreen<HeaterHeatexMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_heatex.png");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private EditBox cyclesField;
    private EditBox delayField;

    public HeaterHeatexScreen(HeaterHeatexMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 204;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        cyclesField = addRenderableWidget(new EditBox(font, leftPos + 73, topPos + 31, 30, 10, Component.empty()));
        initText(cyclesField, menu.getAmountToCool());
        delayField = addRenderableWidget(new EditBox(font, leftPos + 73, topPos + 49, 30, 10, Component.empty()));
        initText(delayField, menu.getTickDelay());
    }

    private static void initText(EditBox field, int value) {
        field.setTextColor(0x00FF00);
        field.setTextColorUneditable(0x00FF00);
        field.setBordered(false);
        field.setMaxLength(5);
        field.setValue(Integer.toString(Math.max(1, value)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (cyclesField != null && !cyclesField.isFocused()) {
            cyclesField.setValue(Integer.toString(Math.max(1, menu.getAmountToCool())));
        }
        if (delayField != null && !delayField.isFocused()) {
            delayField.setValue(Integer.toString(Math.max(1, menu.getTickDelay())));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 44, topPos + 88, 16, 52,
                menu.getInputTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 116, topPos + 88, 16, 52,
                menu.getOutputTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(44, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTankData(),
                    menu.getInputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(116, 36, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTankData(),
                    menu.getOutputTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(70, 26, 36, 18, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("Amount per cycle")), mouseX, mouseY);
        } else if (isHovering(70, 44, 36, 18, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal("Cycle tick delay")), mouseX, mouseY);
        } else if (isHovering(70, 62, 36, 8, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal(NUMBER_FORMAT.format(menu.getHeatEnergy()) + " TU"),
                    Component.literal(menu.getLastInputUsed() + " mB -> " + menu.getLastOutputProduced() + " mB")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (cyclesField != null && cyclesField.keyPressed(keyCode, scanCode, modifiers)) {
            sendInt("toCool", cyclesField.getValue());
            return true;
        }
        if (delayField != null && delayField.keyPressed(keyCode, scanCode, modifiers)) {
            sendInt("delay", delayField.getValue());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (cyclesField != null && cyclesField.charTyped(codePoint, modifiers)) {
            sendInt("toCool", cyclesField.getValue());
            return true;
        }
        if (delayField != null && delayField.charTyped(codePoint, modifiers)) {
            sendInt("delay", delayField.getValue());
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void sendInt(String key, String value) {
        int parsed;
        try {
            parsed = Math.max(Integer.parseInt(value), 1);
        } catch (NumberFormatException ex) {
            parsed = 1;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt(key, parsed);
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }
}
