package com.hbm.ntm.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.StrandCasterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StrandCasterScreen extends AbstractContainerScreen<StrandCasterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_strand_caster.png");

    public StrandCasterScreen(StrandCasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 214;
        titleLabelY = 4;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int molten = menu.getMoltenPixels();
        if (molten > 0) {
            RenderSystem.disableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            int color = menu.getMoltenColor();
            graphics.setColor(((color >> 16) & 255) / 255.0F,
                    ((color >> 8) & 255) / 255.0F,
                    (color & 255) / 255.0F, 1.0F);
            graphics.blit(TEXTURE, leftPos + 17, topPos + 93 - molten, 176, 89 - molten, 34, molten);
            RenderSystem.enableBlend();
            graphics.setColor(1.0F, 1.0F, 1.0F, 0.3F);
            graphics.blit(TEXTURE, leftPos + 17, topPos + 93 - molten, 176, 89 - molten, 34, molten);
            RenderSystem.disableBlend();
        }
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 82, topPos + 38, 16, 24,
                menu.getWaterTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 82, topPos + 89, 16, 24,
                menu.getSteamTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 16, 17, 36, 81)) {
            graphics.renderTooltip(font, menu.getMoltenText(hasShiftDown()), mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 82, 14, 16, 24)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getWaterTank(),
                    menu.getWaterTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 82, 65, 16, 24)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSteamTank(),
                    menu.getSteamTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
