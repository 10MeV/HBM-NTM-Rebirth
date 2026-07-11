package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.RebarPlacerItem;
import com.hbm.ntm.menu.RebarPlacerMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RebarPlacerScreen extends AbstractContainerScreen<RebarPlacerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/gui_rebar.png");

    public RebarPlacerScreen(RebarPlacerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 182;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (!RebarPlacerItem.isValidConcrete(menu.getSlot(0).getItem())) {
            graphics.blit(TEXTURE, leftPos + 87, topPos + 17, 176, 0, 56, 56, 256, 256);
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
        renderConcreteTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderConcreteTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.getCarried().isEmpty()
                || !LegacyGuiElements.isMouseOverSlot(menu.getSlot(0), leftPos, topPos, mouseX, mouseY)
                || menu.getSlot(0).hasItem()) {
            return;
        }
        List<ItemStack> concrete = new ArrayList<>(RebarPlacerItem.acceptableConcreteStacks());
        if (concrete.isEmpty()) {
            return;
        }

        int selectedIndex = 0;
        Component selectedName = concrete.get(0).getHoverName();
        if (concrete.size() > 1) {
            selectedIndex = (int) ((System.currentTimeMillis() % (1000L * concrete.size())) / 1000L);
            selectedName = concrete.get(selectedIndex).getHoverName();
            ItemStack selected = concrete.get(selectedIndex).copy();
            selected.setCount(0);
            concrete.set(selectedIndex, selected);
        }

        List<List<LegacyGuiElements.StackTextPart>> lines = new ArrayList<>();
        if (concrete.size() < 10) {
            lines.add(stackLine(concrete));
        } else if (concrete.size() < 24) {
            lines.add(stackLine(concrete.subList(0, concrete.size() / 2)));
            lines.add(stackLine(concrete.subList(concrete.size() / 2, concrete.size())));
        } else {
            int bound0 = (int) Math.ceil(concrete.size() / 3.0D);
            int bound1 = (int) Math.ceil(concrete.size() / 3.0D * 2.0D);
            lines.add(stackLine(concrete.subList(0, bound0)));
            lines.add(stackLine(concrete.subList(bound0, bound1)));
            lines.add(stackLine(concrete.subList(bound1, concrete.size())));
        }
        lines.add(List.of(LegacyGuiElements.StackTextPart.text(selectedName)));
        LegacyGuiElements.renderStackText(graphics, font, lines, mouseX, mouseY);
    }

    private static List<LegacyGuiElements.StackTextPart> stackLine(List<ItemStack> stacks) {
        List<LegacyGuiElements.StackTextPart> parts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            parts.add(LegacyGuiElements.StackTextPart.stack(stack));
        }
        return parts;
    }
}
