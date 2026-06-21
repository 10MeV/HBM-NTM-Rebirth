package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.RBMKControlAutoMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class RBMKControlAutoScreen extends AbstractContainerScreen<RBMKControlAutoMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_control_auto.png");
    private final EditBox[] fields = new EditBox[4];

    public RBMKControlAutoScreen(RBMKControlAutoMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int[] values = {
                menu.getLevelUpper(), menu.getLevelLower(), menu.getHeatUpper(), menu.getHeatLower()
        };
        for (int i = 0; i < fields.length; i++) {
            fields[i] = LegacyGuiElements.createLegacyTextField(font, leftPos + 30, topPos + 27 + 11 * i,
                    26, 8, i < 2 ? 3 : 4, Integer.toString(values[i]), 0xFFFFFF);
            addRenderableWidget(fields[i]);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int height = (int) (56.0D * (1.0D - menu.getLevelPercent() / 100.0D));
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 124, topPos + 29, 176, 56 - height, 8, height);
        }
        int function = Mth.clamp(menu.getFunction(), 0, 2);
        graphics.blit(TEXTURE, leftPos + 59, topPos + 27, 184, function * 19, 26, 19);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(28, 70, 30, 10, mouseX, mouseY)) {
            saveFields();
            return true;
        }
        for (int k = 0; k < 3; k++) {
            if (isHovering(61, 48 + k * 11, 22, 10, mouseX, mouseY)) {
                CompoundTag tag = new CompoundTag();
                tag.putInt(RBMKColumnKeys.FUNCTION, k);
                send(tag);
                return true;
            }
        }
        return handled;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(124, 29, 16, 56, mouseX, mouseY)) {
            LegacyGuiElements.renderTextTooltip(graphics, font, mouseX, mouseY, menu.getLevelPercent() + "%");
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void saveFields() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(RBMKColumnKeys.LEVEL_UPPER, parseClamped(fields[0], 100));
        tag.putDouble(RBMKColumnKeys.LEVEL_LOWER, parseClamped(fields[1], 100));
        tag.putDouble(RBMKColumnKeys.HEAT_UPPER, parseClamped(fields[2], 9999));
        tag.putDouble(RBMKColumnKeys.HEAT_LOWER, parseClamped(fields[3], 9999));
        send(tag);
    }

    private int parseClamped(EditBox box, int max) {
        int value;
        try {
            value = Integer.parseInt(box.getValue().trim());
        } catch (NumberFormatException ignored) {
            value = 0;
        }
        value = Mth.clamp(value, 0, max);
        box.setValue(Integer.toString(value));
        return value;
    }

    private void send(CompoundTag tag) {
        LegacyGuiElements.playClickSound();
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }
}
