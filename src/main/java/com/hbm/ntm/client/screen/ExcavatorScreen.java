package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ExcavatorMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class ExcavatorScreen extends AbstractContainerScreen<ExcavatorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_mining_drill.png");
    private static final String[] TOGGLES = { "drill", "crusher", "walling", "veinminer", "silktouch" };

    public ExcavatorScreen(ExcavatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 242;
        imageHeight = 204;
        titleLabelY = 6;
        inventoryLabelX = 41;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 242, 96);
        graphics.blit(TEXTURE, leftPos + 33, topPos + 104, 33, 104, 176, 100);
        renderEnergy(graphics);
        renderToggles(graphics);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 202, topPos + 70, 16, 52, menu.getTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (int i = 0; i < TOGGLES.length; i++) {
            if (isHovering(6 + i * 24, 42, 20, 40, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable("excavator." + TOGGLES[i]), mouseX, mouseY);
            }
        }
        if (isHovering(220, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 220, topPos + 18, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(202, 18, 16, 52, mouseX, mouseY)) {
            List<Component> tooltip = menu.getTankTooltip(hasShiftDown());
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(), tooltip, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        for (int i = 0; i < TOGGLES.length; i++) {
            if (isHovering(6 + i * 24, 42, 20, 40, mouseX, mouseY)) {
                CompoundTag tag = new CompoundTag();
                tag.putBoolean(TOGGLES[i], true);
                minecraft.player.playSound(SoundEvents.LEVER_CLICK, 1.0F, 1.0F);
                ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
                return true;
            }
        }
        return handled;
    }

    private void renderEnergy(GuiGraphics graphics) {
        int height = menu.getPowerBarHeight(52);
        if (height > 0) {
            graphics.blit(TEXTURE, leftPos + 220, topPos + 70 - height, 229, 156 - height, 16, height);
        }
        if (menu.getPower() > menu.getConsumption()) {
            graphics.blit(TEXTURE, leftPos + 224, topPos + 4, 239, 156, 9, 12);
        }
        if (!menu.hasDrillbit() && System.currentTimeMillis() % 1000L < 500L) {
            graphics.blit(TEXTURE, leftPos + 171, topPos + 74, 209, 154, 18, 18);
        }
    }

    private void renderToggles(GuiGraphics graphics) {
        renderToggle(graphics, 0, menu.drillEnabled(), menu.hasDrillbit() && menu.getPower() >= menu.getConsumption());
        renderToggle(graphics, 1, menu.crusherEnabled(), true);
        renderToggle(graphics, 2, menu.wallingEnabled(), true);
        renderToggle(graphics, 3, menu.veinMinerEnabled(), menu.canVeinMine());
        renderToggle(graphics, 4, menu.silkTouchEnabled(), menu.canSilkTouch());
    }

    private void renderToggle(GuiGraphics graphics, int index, boolean enabled, boolean valid) {
        if (!enabled) {
            return;
        }
        int x = 6 + index * 24;
        graphics.blit(TEXTURE, leftPos + x, topPos + 42, 209, 114, 20, 40);
        if (valid) {
            graphics.blit(TEXTURE, leftPos + x + 5, topPos + 5, 209, 104, 10, 10);
        } else if (System.currentTimeMillis() % 1000L < 500L) {
            graphics.blit(TEXTURE, leftPos + x + 5, topPos + 5, 219, 104, 10, 10);
        }
    }
}
