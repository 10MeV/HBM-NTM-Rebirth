package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ICFPressBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.ICFPressMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ICFPressScreen extends AbstractContainerScreen<ICFPressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_icf_press.png");

    public ICFPressScreen(ICFPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 179;
        inventoryLabelY = 85;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int muon = menu.getMuon() * 52 / ICFPressBlockEntity.MAX_MUON;
        graphics.blit(TEXTURE, leftPos + 28, topPos + 70 - muon, 176, 52 - muon, 4, muon);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 44, topPos + 70, 16, 52,
                menu.getDeuteriumTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 152, topPos + 70, 16, 52,
                menu.getTritiumTank());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getDeuteriumTank(), 44, 18, 16, 52);
        renderTankTooltip(graphics, mouseX, mouseY, menu.getTritiumTank(), 152, 18, 16, 52);
        renderEmptySlotHint(graphics, mouseX, mouseY, 4, "Item input: Top/Bottom");
        renderEmptySlotHint(graphics, mouseX, mouseY, 5, "Item input: Sides");
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) {
            return false;
        }
        graphics.renderComponentTooltip(font, tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        return true;
    }

    private void renderEmptySlotHint(GuiGraphics graphics, int mouseX, int mouseY, int slotIndex, String text) {
        Slot slot = menu.getSlot(slotIndex);
        if (!slot.hasItem() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font,
                    List.of(Component.literal(text).withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
        }
    }
}
