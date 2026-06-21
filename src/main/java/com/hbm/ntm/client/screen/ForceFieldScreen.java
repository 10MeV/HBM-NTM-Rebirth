package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ForceFieldMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class ForceFieldScreen extends AbstractContainerScreen<ForceFieldMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_field.png");

    public ForceFieldScreen(ForceFieldMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        titleLabelX = 0;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 69 - power, 176, 52 - power, 16, power);
        }
        int health = menu.getHealthBarHeight(52);
        if (health > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 69 - health, 192, 52 - health, 16, health);
        }
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 142, topPos + 34, 176, 52, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 8, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(62, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal(
                    menu.getHealth() + " / " + menu.getMaxHealth() + "HP")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos + 142, topPos + 34, 18, 18)) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
