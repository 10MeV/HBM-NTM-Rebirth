package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.PneumaticTubeMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class PneumaticTubeScreen extends AbstractContainerScreen<PneumaticTubeMenu> {
    private static final ResourceLocation PIPE = texture("gui_pneumatic_pipe");
    private static final ResourceLocation ENDPOINT = texture("gui_pneumatic_endpoint");

    public PneumaticTubeScreen(PneumaticTubeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 185;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = menu.isEndpointOnly() ? ENDPOINT : PIPE;
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(texture, leftPos + 139, topPos + (menu.isWhitelist() ? 33 : 47), 176, 0, 3, 6);

        if (!menu.isEndpointOnly()) {
            if (menu.isRedstoneEnabled()) {
                graphics.blit(texture, leftPos + 7, topPos + 52, 179, 0, 18, 18);
            }
            graphics.blit(texture, leftPos + 151, topPos + 16, 197, 18 * menu.getReceiveOrder(), 18, 18);
            graphics.blit(texture, leftPos + 151, topPos + 52, 215, 18 * menu.getSendOrder(), 18, 18);

            int pressure = menu.getTankData().pressure();
            if (pressure > 0) {
                graphics.blit(texture, leftPos + 6 + 4 * (pressure - 1), topPos + 36, 179, 18, 4, 8);
            }
            double fill = menu.getTankData().capacity() <= 0
                    ? 0.0D
                    : (double) menu.getTankData().fill() / (double) menu.getTankData().capacity();
            LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 16, topPos + 25, fill,
                    5, 2, 1, 0xFFCA6C43, 0xFFAB4223);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), imageWidth / 2, 5, imageWidth, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPneumaticTooltips(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!menu.isEndpointOnly()) {
            if (sendIfHovered(7, 52, 18, 18, mouseX, mouseY, "redstone")) {
                return true;
            }
            if (sendIfHovered(6, 36, 20, 8, mouseX, mouseY, "pressure")) {
                return true;
            }
            if (sendIfHovered(151, 16, 18, 18, mouseX, mouseY, "receive")) {
                return true;
            }
            if (sendIfHovered(151, 52, 18, 18, mouseX, mouseY, "send")) {
                return true;
            }
        }
        if (sendIfHovered(128, 30, 14, 26, mouseX, mouseY, "whitelist")) {
            return true;
        }
        return handled;
    }

    private void renderPneumaticTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.isEndpointOnly()) {
            if (isHovering(7, 16, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                        menu.getTankTooltip(Screen.hasShiftDown()), mouseX, mouseY);
                return;
            }
            if (isHovering(7, 52, 18, 18, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(Component.literal(
                        (menu.isRedstoneEnabled() ? "ON " : "OFF ") + "with Redstone")), mouseX, mouseY);
                return;
            }
            if (isHovering(6, 36, 20, 8, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(
                        Component.literal("Compressor: " + menu.getTankData().pressure() + " PU"),
                        Component.literal("Max range: " + menu.getRangeFromPressure() + "m")), mouseX, mouseY);
                return;
            }
            if (isHovering(151, 16, 18, 18, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(
                        Component.literal("Receiver order:"),
                        Component.literal(menu.getReceiveOrder() == 0 ? "Round robin" : "Random")),
                        mouseX, mouseY);
                return;
            }
            if (isHovering(151, 52, 18, 18, mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, List.of(
                        Component.literal("Provider slot order:"),
                        Component.literal(switch (menu.getSendOrder()) {
                            case 1 -> "Last to first";
                            case 2 -> "Random";
                            default -> "First to last";
                        })), mouseX, mouseY);
                return;
            }
        }
        if (minecraft != null && minecraft.player != null && minecraft.player.containerMenu.getCarried().isEmpty()) {
            for (int slotIndex = 0; slotIndex < 15; slotIndex++) {
                Slot slot = menu.slots.get(slotIndex);
                if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.hasItem()) {
                    Component mode = menu.getModeLabel(slotIndex);
                    if (!mode.getString().isEmpty()) {
                        graphics.renderComponentTooltip(font,
                                List.of(Component.literal("Right click to change"), mode), mouseX, mouseY);
                    }
                    return;
                }
            }
        }
    }

    private boolean sendIfHovered(int x, int y, int width, int height, double mouseX, double mouseY, String key) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) {
            return false;
        }
        playButtonClick();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
        return true;
    }

    private static void playButtonClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/" + name + ".png");
    }
}
