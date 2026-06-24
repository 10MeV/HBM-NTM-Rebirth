package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.WatzReactorMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WatzReactorScreen extends AbstractContainerScreen<WatzReactorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_watz.png");

    public WatzReactorScreen(WatzReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 229;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        float color = heatColor();
        graphics.setColor(1.0F, color, color, 1.0F);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 131, 122);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, leftPos + 131, topPos, 131, 0, 36, 122);
        graphics.blit(TEXTURE, leftPos, topPos + 130, 0, 130, imageWidth, 99);
        graphics.blit(TEXTURE, leftPos + 126, topPos + 31, 176, 31, 9, 60);
        graphics.blit(TEXTURE, leftPos + 105, topPos + 96, 185, 26, 30, 26);
        graphics.blit(TEXTURE, leftPos + 9, topPos + 96, 184, 0, 26, 26);
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 147, topPos + 8, 176, 0, 8, 8);
        }
        if (menu.isLocked()) {
            graphics.blit(TEXTURE, leftPos + 142, topPos + 70, 210, 0, 18, 18);
        }
        renderRoundHeatGauge(graphics, 1.0F - color);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 143, topPos + 69, 4, 43,
                menu.getCoolantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 149, topPos + 69, 4, 43,
                menu.getHotCoolantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 155, topPos + 69, 4, 43,
                menu.getMudTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        String flux = String.format(Locale.US, "%,.1f", menu.getFluxDisplay());
        graphics.pose().pushPose();
        double scale = 1.25D;
        graphics.pose().scale((float) (1.0D / scale), (float) (1.0D / scale), 1.0F);
        graphics.drawString(font, flux, (int) (161 * scale - font.width(flux)), (int) (107 * scale),
                0x00FF00, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(13, 100, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font,
                    Component.literal(String.format(Locale.US, "%,d TU", menu.getHeat())), mouseX, mouseY);
        } else if (isHovering(143, 71, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(menu.isLocked()
                    ? "Unlock pellet IO configuration"
                    : "Lock pellet IO configuration")), mouseX, mouseY);
        } else {
            renderTankTooltip(graphics, mouseX, mouseY, menu.getCoolantTank(), 142, 23, 6, 45);
            renderTankTooltip(graphics, mouseX, mouseY, menu.getHotCoolantTank(), 148, 23, 6, 45);
            renderTankTooltip(graphics, mouseX, mouseY, menu.getMudTank(), 154, 23, 6, 45);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (leftPos + 142 <= mouseX && mouseX < leftPos + 160 && topPos + 70 < mouseY
                && mouseY <= topPos + 88) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("lock", true);
            ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
            playClick();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private float heatColor() {
        return Math.max(0.0F, Math.min(1.0F,
                1.0F - (float) Math.log(menu.getHeat() / 100_000.0D + 1.0D) * 0.4F));
    }

    private void renderRoundHeatGauge(GuiGraphics graphics, float amount) {
        int frame = Math.max(0, Math.min(12, Math.round(amount * 12.0F)));
        graphics.blit(TEXTURE, leftPos + 13, topPos + 100, 184, frame * 18, 18, 18);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY, HbmFluidGuiHelper.TankData tank,
            int x, int y, int width, int height) {
        if (isHovering(x, y, width, height, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, tank.tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()), mouseX, mouseY);
        }
    }

    private void playClick() {
        LegacyGuiElements.playClickSound();
    }
}
