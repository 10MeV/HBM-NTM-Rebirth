package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.menu.RBMKRodMenu;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RBMKRodScreen extends AbstractContainerScreen<RBMKRodMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_rbmk_element.png");

    public RBMKRodScreen(RBMKRodMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        ItemStack stack = menu.getSlot(0).getItem();
        if (stack.getItem() instanceof RBMKFuelRodItem item) {
            RBMKFuelRodSpec spec = item.getSpec();
            RBMKFuelRodState state = item.getState(stack);
            graphics.blit(TEXTURE, leftPos + 34, topPos + 21, 176, 0, 18, 67);
            int depletion = (int) ((1.0D - state.enrichment(spec)) * 67.0D);
            if (depletion > 0) {
                graphics.blit(TEXTURE, leftPos + 34, topPos + 21, 194, 0, 18, Math.min(67, depletion));
            }
            int xenon = (int) (state.xenonLevel() * 58.0D);
            if (xenon > 0) {
                graphics.blit(TEXTURE, leftPos + 126, topPos + 82 - xenon, 212, 58 - xenon, 14, xenon);
            }
        }
        if (!menu.getBlockEntity().coldEnoughForAutoloader()) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 20, 6);
        }
        if (!menu.getBlockEntity().canManualUnloadFuelRod()) {
            LegacyGuiElements.renderInfoPanel(graphics, leftPos - 16, topPos + 36, 7);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!menu.getBlockEntity().coldEnoughForAutoloader()
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 20, 16, 16)) {
            LegacyGuiElements.renderTextTooltip(graphics, font, mouseX, mouseY,
                    "Fuel skin temperature has exceeded 1,000C,",
                    "autoloaders can no longer cycle fuel!");
        }
        if (!menu.getBlockEntity().canManualUnloadFuelRod()
                && LegacyGuiElements.isMouseOver(mouseX, mouseY, leftPos - 16, topPos + 36, 16, 16)) {
            LegacyGuiElements.renderTextTooltip(graphics, font, mouseX, mouseY,
                    "Fuel skin temperature has exceeded 200C,",
                    "fuel can no longer be removed by hand!");
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
