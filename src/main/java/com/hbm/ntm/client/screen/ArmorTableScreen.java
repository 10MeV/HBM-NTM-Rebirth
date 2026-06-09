package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.menu.ArmorTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArmorTableScreen extends AbstractContainerScreen<ArmorTableMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_armor_modifier.png");
    private static final List<Component> SLOT_TOOLTIPS = List.of(
            Component.translatable("armorMod.type.helmet").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.chestplate").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.leggings").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.boots").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.servo").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.cladding").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.insert").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.special").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.type.battery").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.translatable("armorMod.insertHere").withStyle(ChatFormatting.YELLOW)
    );

    public ArmorTableScreen(ArmorTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 198;
        imageHeight = 222;
        titleLabelX = 22 + (176 - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = 30;
        inventoryLabelY = 128;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos + 22, topPos, 0, 0, imageWidth - 22, imageHeight);
        graphics.blit(TEXTURE, leftPos, topPos + 31, 176, 96, 22, 100);

        ItemStack armor = menu.getArmorStack();
        if (!armor.isEmpty()) {
            graphics.blit(TEXTURE, leftPos + 63, topPos + 60,
                    armor.getItem() instanceof ArmorItem ? 176 : 176,
                    armor.getItem() instanceof ArmorItem ? 74 : 52,
                    22, 22);
        } else if ((System.currentTimeMillis() / 500L) % 2L == 0L) {
            graphics.blit(TEXTURE, leftPos + 63, topPos + 60, 176, 52, 22, 22);
        }

        for (int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
            drawIndicator(graphics, i);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderEmptySlotTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawIndicator(GuiGraphics graphics, int index) {
        ItemStack mod = menu.getUpgradeStack(index);
        if (mod.isEmpty()) {
            return;
        }
        var slot = menu.slots.get(index);
        boolean valid = ArmorModHandler.isApplicable(menu.getArmorStack(), mod);
        graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 176, valid ? 34 : 16, 18, 18);
    }

    private void renderEmptySlotTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!getMenu().getCarried().isEmpty()) {
            return;
        }
        for (int i = 0; i < SLOT_TOOLTIPS.size(); i++) {
            var slot = menu.slots.get(i);
            if (!slot.hasItem() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                graphics.renderTooltip(font, SLOT_TOOLTIPS.get(i), mouseX, mouseY);
                return;
            }
        }
    }
}
