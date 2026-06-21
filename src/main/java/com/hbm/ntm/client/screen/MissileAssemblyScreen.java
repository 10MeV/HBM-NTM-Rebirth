package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.MissileAssemblyMenu;
import com.hbm.ntm.network.ModMessages;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MissileAssemblyScreen extends AbstractContainerScreen<MissileAssemblyMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/gui_missile_assembly.png");

    public MissileAssemblyScreen(MissileAssemblyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawState(graphics, menu.getChipState(), 13);
        drawState(graphics, menu.getWarheadState(), 31);
        drawState(graphics, menu.getFuselageState(), 49);
        drawState(graphics, menu.getStabilityState(), 67);
        drawState(graphics, menu.getThrusterState(), 85);
        if (menu.canBuild()) {
            graphics.blit(TEXTURE, leftPos + 115, topPos + 35, 176, 0, 18, 18);
        }

        ItemStack preview = menu.previewMissileStack();
        if (!preview.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 83.0F, topPos + 89.0F, 120.0F);
            graphics.pose().mulPose(Axis.YN.rotationDegrees((float) ((System.currentTimeMillis() / 10L) % 360L)));
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            graphics.renderItem(preview, -8, -8);
            graphics.pose().popPose();
        }
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
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(115, 35, 18, 18, mouseX, mouseY)) {
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawState(GuiGraphics graphics, int state, int x) {
        if (state == 1) {
            graphics.blit(TEXTURE, leftPos + x, topPos + 23, 194, 0, 6, 8);
        } else if (state == 0) {
            graphics.blit(TEXTURE, leftPos + x, topPos + 23, 200, 0, 6, 8);
        }
    }
}
