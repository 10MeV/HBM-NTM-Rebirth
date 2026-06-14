package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RBMKAutoloaderBlockEntity;
import com.hbm.ntm.menu.RBMKAutoloaderMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.neutron.RBMKMenuScreenPlanner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class RBMKAutoloaderScreen extends AbstractContainerScreen<RBMKAutoloaderMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_autoloader.png");

    public RBMKAutoloaderScreen(RBMKAutoloaderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 182;
        inventoryLabelY = 88;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        RBMKMenuScreenPlanner.AutoloaderScreenPlan plan =
                RBMKMenuScreenPlanner.autoloaderScreenPlan(menu.getCycle());
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        graphics.drawString(font, plan.cycleText(), imageWidth / 2 - font.width(plan.cycleText()) / 2,
                plan.cycleTextY(), plan.cycleTextColor(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        RBMKMenuScreenPlanner.AutoloaderScreenPlan plan =
                RBMKMenuScreenPlanner.autoloaderScreenPlan(menu.getCycle());
        for (RBMKMenuScreenPlanner.ControlButton control : plan.buttons()) {
            if (isHovering(control.x(), control.y(), control.width(), control.height(), mouseX, mouseY)) {
                sendButton(control.packetKey());
                return true;
            }
        }
        return handled;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void sendButton(String key) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
    }
}
