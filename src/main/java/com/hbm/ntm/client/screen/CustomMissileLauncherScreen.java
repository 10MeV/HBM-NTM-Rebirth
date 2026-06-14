package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.missile.CustomMissilePartProfile.PartSize;
import com.hbm.ntm.menu.CustomMissileLauncherMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public abstract class CustomMissileLauncherScreen<T extends CustomMissileLauncherMenu> extends AbstractContainerScreen<T> {
    private final ResourceLocation texture;
    private final boolean hasPadSizeButtons;

    protected CustomMissileLauncherScreen(T menu, Inventory inventory, Component title, String textureName,
            boolean hasPadSizeButtons) {
        super(menu, inventory, title);
        this.texture = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/" + textureName + ".png");
        this.hasPadSizeButtons = hasPadSizeButtons;
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int power = menu.getPowerBarWidth(34);
        if (power > 0) {
            graphics.blit(texture, leftPos + 134, topPos + 113, 176, 96, power, 6);
        }

        int solid = menu.getSolidBarHeight(52);
        if (solid > 0) {
            graphics.blit(texture, leftPos + 152, topPos + 88 - solid, 176, 96 - solid, 16, solid);
        }

        if (menu.isMissileValid()) {
            graphics.blit(texture, leftPos + 25, topPos + 35, 176, 26, 18, 18);
        }
        if (menu.hasDesignator()) {
            graphics.blit(texture, leftPos + 25, topPos + 71, 176, 26, 18, 18);
        }

        drawStateLight(graphics, menu.getLiquidState(), 121, 23);
        drawStateLight(graphics, menu.getOxidizerState(), 139, 23);
        drawStateLight(graphics, menu.getSolidState(), 157, 23);

        if (hasPadSizeButtons) {
            drawPadSizeButton(graphics);
        }

        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 116, topPos + 36,
                16, 34, menu.getFuelTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 134, topPos + 36,
                16, 34, menu.getOxidizerTankData());
        renderMissilePreview(graphics);
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
        if (isHovering(116, 36, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getFuelTankData(),
                    menu.getFuelTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(134, 36, 16, 34, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOxidizerTankData(),
                    menu.getOxidizerTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(152, 36, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Solid Fuel: " + menu.getSolidFuel() + "l"),
                    mouseX, mouseY);
        } else if (isHovering(134, 113, 34, 6, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 134, topPos + 113, 34, 6, menu.getPower(), menu.getMaxPower());
        } else if (hasPadSizeButtons && isHovering(7, 98, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Size 10 & 10/15"), mouseX, mouseY);
        } else if (hasPadSizeButtons && isHovering(25, 98, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Size 15 & 15/20"), mouseX, mouseY);
        } else if (hasPadSizeButtons && isHovering(43, 98, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Size 20"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hasPadSizeButtons) {
            if (isHovering(7, 98, 18, 18, mouseX, mouseY)) {
                ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), PartSize.SIZE_10.ordinal(), 0);
                return true;
            }
            if (isHovering(25, 98, 18, 18, mouseX, mouseY)) {
                ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), PartSize.SIZE_15.ordinal(), 0);
                return true;
            }
            if (isHovering(43, 98, 18, 18, mouseX, mouseY)) {
                ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), PartSize.SIZE_20.ordinal(), 0);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawStateLight(GuiGraphics graphics, int state, int x, int y) {
        if (state == 1) {
            graphics.blit(texture, leftPos + x, topPos + y, 176, 0, 6, 8);
        } else if (state == 0) {
            graphics.blit(texture, leftPos + x, topPos + y, 182, 0, 6, 8);
        }
    }

    private void drawPadSizeButton(GuiGraphics graphics) {
        switch (menu.getPadSize()) {
            case SIZE_10 -> graphics.blit(texture, leftPos + 7, topPos + 98, 176, 8, 18, 18);
            case SIZE_15 -> graphics.blit(texture, leftPos + 25, topPos + 98, 194, 8, 18, 18);
            case SIZE_20 -> graphics.blit(texture, leftPos + 43, topPos + 98, 212, 8, 18, 18);
            default -> {
            }
        }
    }

    private void renderMissilePreview(GuiGraphics graphics) {
        ItemStack missile = menu.getMissileStack();
        if (missile.isEmpty() || !menu.isMissileValid()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 88.0D, topPos + 115.0D, 100.0D);
        graphics.pose().scale(4.5F, 4.5F, 1.0F);
        graphics.renderItem(missile, -8, -8);
        graphics.pose().popPose();
    }
}
