package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.WeaponTableMenu;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class WeaponTableScreen extends AbstractContainerScreen<WeaponTableMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/machine/gui_weapon_modifier.png");
    private double yaw = 20.0D;
    private double pitch = -10.0D;

    public WeaponTableScreen(WeaponTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 240;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (!menu.getGunStack().isEmpty()) {
            graphics.blit(TEXTURE, leftPos + 35, topPos + 112, 176 + 6 * menu.getConfigIndex(), 0, 6, 8);
            renderGunPreview(graphics);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null
                && menu.getConfigCount() > 1 && isHovering(26, 112, 7, 9, mouseX, mouseY)) {
            int next = (menu.getConfigIndex() + 1) % menu.getConfigCount();
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, next);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && isHovering(8, 18, 160, 79, mouseX, mouseY)) {
            double distX = leftPos + 88.0D - mouseX;
            double distY = topPos + 57.5D - mouseY;
            yaw = distX / 80.0D * -180.0D;
            pitch = distY / 39.5D * 90.0D;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderGunPreview(GuiGraphics graphics) {
        ItemStack gun = menu.getGunStack();
        if (gun.isEmpty()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 88.0D, topPos + 57.0D, 120.0D);
        graphics.pose().mulPose(Axis.YP.rotationDegrees((float) yaw));
        graphics.pose().mulPose(Axis.XP.rotationDegrees((float) pitch));
        graphics.pose().scale(4.5F, 4.5F, 1.0F);
        graphics.renderItem(gun, -8, -8);
        graphics.pose().popPose();
    }
}
