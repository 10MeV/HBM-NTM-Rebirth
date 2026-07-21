package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.PneumaticStorageAccessMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class PneumaticStorageAccessScreen extends AbstractContainerScreen<PneumaticStorageAccessMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/storage/gui_pneumatic_access.png");

    public PneumaticStorageAccessScreen(PneumaticStorageAccessMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 249;
        inventoryLabelY = 159;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
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
        if (!renderCacheTooltip(graphics, mouseX, mouseY)) {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private boolean renderCacheTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < PneumaticStorageAccessMenu.CACHE_SLOT_COUNT; index++) {
            Slot slot = menu.slots.get(index);
            if (!isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) || !slot.hasItem()
                    || slot.getItem().getTag() == null) {
                continue;
            }
            long amount = slot.getItem().getTag().getLong(PneumaticStorageAccessMenu.CACHE_AMOUNT_TAG);
            int stacks = slot.getItem().getTag().getInt(PneumaticStorageAccessMenu.CACHE_STACKS_TAG);
            graphics.renderComponentTooltip(font, List.of(slot.getItem().getHoverName(), Component.literal("x" + amount),
                    Component.literal("in " + stacks + " stacks")), mouseX, mouseY);
            return true;
        }
        return false;
    }
}
