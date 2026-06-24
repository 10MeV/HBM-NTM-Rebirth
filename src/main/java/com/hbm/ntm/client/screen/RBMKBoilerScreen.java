package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.menu.RBMKBoilerMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RBMKBoilerScreen extends AbstractContainerScreen<RBMKBoilerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_boiler.png");

    public RBMKBoilerScreen(RBMKBoilerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int feedHeight = menu.getFeedTank().scaledFill(58);
        graphics.blit(TEXTURE, leftPos + 126, topPos + 82 - feedHeight,
                176, 58 - feedHeight, 14, feedHeight);
        int steamHeight = menu.getSteamTank().scaledFill(22);
        if (steamHeight > 0) {
            steamHeight++;
        }
        if (steamHeight > 22) {
            steamHeight++;
        }
        graphics.blit(TEXTURE, leftPos + 91, topPos + 65 - steamHeight,
                190, 24 - steamHeight, 4, steamHeight);
        if (menu.getSteamTank().type() == HbmFluids.STEAM) {
            graphics.blit(TEXTURE, leftPos + 36, topPos + 24, 194, 0, 14, 58);
        } else if (menu.getSteamTank().type() == HbmFluids.HOTSTEAM) {
            graphics.blit(TEXTURE, leftPos + 36, topPos + 24, 208, 0, 14, 58);
        } else if (menu.getSteamTank().type() == HbmFluids.SUPERHOTSTEAM) {
            graphics.blit(TEXTURE, leftPos + 36, topPos + 24, 222, 0, 14, 58);
        } else if (menu.getSteamTank().type() == HbmFluids.ULTRAHOTSTEAM) {
            graphics.blit(TEXTURE, leftPos + 36, topPos + 24, 236, 0, 14, 58);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(33, 21, 20, 64, mouseX, mouseY)) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(RBMKColumnKeys.COMPRESSION, true);
            LegacyGuiElements.playClickSound();
            ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
            return true;
        }
        return handled;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(126, 24, 16, 56, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFeedTank(),
                    menu.getFeedTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        if (isHovering(89, 39, 8, 28, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSteamTank(),
                    menu.getSteamTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
