package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.AutocrafterBlockEntity;
import com.hbm.ntm.menu.AutocrafterMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class AutocrafterScreen extends AbstractContainerScreen<AutocrafterMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_autocrafter.png");

    public AutocrafterScreen(AutocrafterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 240;
        titleLabelY = 6;
        inventoryLabelY = 146;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int powerHeight = menu.getPowerBarHeight(52);
        if (powerHeight > 0) {
            graphics.blit(TEXTURE, leftPos + 17, topPos + 97 - powerHeight, 176,
                    52 - powerHeight, 16, powerHeight);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(17, 45, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font,
                    List.of(Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE")),
                    mouseX, mouseY);
        } else if (menu.getCarried().isEmpty()) {
            for (int slot = AutocrafterBlockEntity.SLOT_TEMPLATE_START;
                    slot <= AutocrafterBlockEntity.SLOT_TEMPLATE_END; slot++) {
                Slot screenSlot = menu.slots.get(slot);
                if (screenSlot.hasItem() && isHovering(screenSlot.x, screenSlot.y, 16, 16, mouseX, mouseY)) {
                    LegacyGuiElements.renderTooltip(graphics, font,
                            List.of(Component.literal("Right click to change").withStyle(ChatFormatting.RED),
                                    menu.getModeLabel(slot)),
                            mouseX, mouseY);
                    renderTooltip(graphics, mouseX, mouseY);
                    return;
                }
            }
            Slot preview = menu.slots.get(AutocrafterBlockEntity.SLOT_TEMPLATE_OUTPUT);
            if (menu.hasPreviewRecipe() && isHovering(preview.x, preview.y, 16, 16, mouseX, mouseY)) {
                LegacyGuiElements.renderTooltip(graphics, font,
                        List.of(Component.literal("Right click to change").withStyle(ChatFormatting.RED),
                                menu.getRecipeLabel()),
                        mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
