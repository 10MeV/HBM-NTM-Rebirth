package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RustedLaunchPadBlockEntity;
import com.hbm.ntm.menu.RustedLaunchPadMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RustedLaunchPadScreen extends AbstractContainerScreen<RustedLaunchPadMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/weapon/gui_launch_pad_rusted.png");
    private static final ItemStack RUSTED_MISSILE = new ItemStack(ModItems.MISSILE_DOOMSDAY_RUSTED.get());

    public RustedLaunchPadScreen(RustedLaunchPadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 236;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.hasLaunchCode()) {
            graphics.blit(TEXTURE, leftPos + 121, topPos + 32, 192, 0, 6, 8);
        }
        if (menu.hasLaunchKey()) {
            graphics.blit(TEXTURE, leftPos + 139, topPos + 32, 192, 0, 6, 8);
        }
        if (menu.hasLaunchCode() && menu.hasLaunchKey() && menu.isMissileLoaded()) {
            int launchCode = menu.launchCodeNumber();
            for (int i = 0; i < 8; i++) {
                int magnitude = (int) Math.pow(10, i);
                int digit = (launchCode % (magnitude * 10)) / magnitude;
                graphics.blit(TEXTURE, leftPos + 109 + 6 * i, topPos + 85, 192 + 6 * digit, 8, 6, 8);
            }
        }
        if (menu.isMissileLoaded()) {
            graphics.renderItem(RUSTED_MISSILE, leftPos + 62, topPos + 105);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 4, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(26, 36, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatableWithFallback(
                    "gui.hbm_ntm_rebirth.launch_pad_rusted.release", "Release Missile"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(26, 36, 16, 16, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0,
                    RustedLaunchPadBlockEntity.BUTTON_RELEASE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
