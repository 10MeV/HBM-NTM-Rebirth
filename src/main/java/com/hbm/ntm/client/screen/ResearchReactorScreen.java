package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ResearchReactorMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.math.NumberUtils;

public class ResearchReactorScreen extends AbstractContainerScreen<ResearchReactorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_research_reactor.png");
    private final LegacyNumberDisplay[] displays = {
            new LegacyNumberDisplay(14, 25, 0x08FF00).setDigitLength(4),
            new LegacyNumberDisplay(12, 63, 0x08FF00).setDigitLength(3),
            new LegacyNumberDisplay(5, 101, 0x08FF00).setDigitLength(3)
    };

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
        levelField.setValue(Integer.toString(menu.getLevelPercent()));
        addWidget(levelField);
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
        displays[0].drawNumber(graphics, leftPos, topPos, menu.getTotalFlux());
        displays[1].drawNumber(graphics, leftPos, topPos, menu.getTemperatureDisplay());
        displays[2].drawNumber(graphics, leftPos, topPos, normalizeLevelField());
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 14, topPos + 23, 3);
        LegacyGuiElements.renderInfoPanel(graphics, leftPos - 14, topPos + 61, 2);
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
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 14, topPos + 23, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 14, topPos + 23, 16, 16, leftPos - 6, topPos + 39, java.util.List.of(
                    Component.literal("The reactor has to be submerged"),
                    Component.literal("in water on its sides to cool."),
                    Component.literal("The neutron flux is provided to"),
                    Component.literal("adjacent breeding reactors.")));
        } else if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 14, topPos + 61, 16, 16)) {
            LegacyGuiElements.renderCustomInfoTooltip(graphics, font, mouseX, mouseY,
                    leftPos - 14, topPos + 61, 16, 16, leftPos - 6, topPos + 77, java.util.List.of(
                    Component.literal("This reactor is fueled with plate fuel."),
                    Component.literal("The reaction needs a neutron source to start.")));
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        boolean fieldHovered = LegacyGuiElements.isMouseOver(
                mouseX, mouseY, leftPos + 8, topPos + 99, 33, 16);
        displays[2].setBlinks(fieldHovered);
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 44, topPos + 97, 11, 20)) {
            sendLevel();
            return true;
        }
        return handled;
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
        return levelField.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    private void sendLevel() {
        Integer percent = parseLevelFieldForSubmit();
        if (percent == null) {
            return;
        }
        levelField.setValue(Integer.toString(percent));
        CompoundTag tag = new CompoundTag();
        tag.putDouble("level", percent * 0.01D);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
        buttonTimer = 15;
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BLOCK_RBMK_AZ5_COVER.get(), 0.5F));
        }
    }

    private int normalizeLevelField() {
        String value = levelField.getValue();
        int level = NumberUtils.isDigits(value)
                ? (int) Mth.clamp(Double.parseDouble(value), 0.0D, 100.0D)
                : 0;
        levelField.setValue(Integer.toString(level));
        return level;
    }

    private Integer parseLevelFieldForSubmit() {
        String value = levelField.getValue();
        if (!NumberUtils.isNumber(value)) {
            return null;
        }
        return (int) Mth.clamp(Double.parseDouble(value), 0.0D, 100.0D);
    }
}
