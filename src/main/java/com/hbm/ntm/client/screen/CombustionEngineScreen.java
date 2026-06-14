package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.menu.CombustionEngineMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CombustionEngineScreen extends AbstractContainerScreen<CombustionEngineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/generators/gui_combustion.png");

    private boolean draggingThrottle;
    private int localThrottle;

    public CombustionEngineScreen(CombustionEngineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 203;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
        localThrottle = menu.getThrottle();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        localThrottle = draggingThrottle ? localThrottle : menu.getThrottle();
        ItemStack piston = menu.getBlockEntity().getItems().getStackInSlot(CombustionEngineBlockEntity.SLOT_PISTON);
        if (!piston.isEmpty()) {
            int pistonIndex = pistonIndex(piston);
            graphics.blit(TEXTURE, leftPos + 80, topPos + 51, 176, 52 + pistonIndex * 12, 25, 12);
        }
        graphics.blit(TEXTURE, leftPos + 79 + (localThrottle * 32 / 30), topPos + 38, 192, 15, 4, 8);
        if (menu.isOn()) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 13, 192, 0, 35, 15);
        }
        int power = menu.getPowerBarHeight(52);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 143, topPos + 69 - power, 176, 52 - power, 16, power);
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 35, topPos + 69, 16, 52,
                menu.getTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!draggingThrottle && isHovering(143, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 143, topPos + 17, 16, 52, menu.getPower(), menu.getMaxPower());
        } else if (!draggingThrottle && isHovering(35, 17, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTankData(),
                    menu.getTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (draggingThrottle || isHovering(80, 39, 34, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(String.format(Locale.US, "%.1f mB/t", localThrottle * 0.2D)))),
                    Mth.clamp(mouseX, leftPos + 80, leftPos + 114),
                    Mth.clamp(mouseY, topPos + 38, topPos + 46));
        } else if (isHovering(79, 50, 35, 14, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getLastPowerProduced() + " HE/t"),
                    Component.literal((menu.getLastPowerProduced() * 20L) + " HE/s"))), mouseX, mouseY);
        } else if (isHovering(79, 13, 35, 15, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(Component.literal("Ignition"))), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(89, 14, 16, 14, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0,
                    CombustionEngineBlockEntity.CONTROL_TOGGLE);
            return true;
        }
        if (isHovering(79, 39, 36, 8, mouseX, mouseY)) {
            draggingThrottle = true;
            updateThrottle(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingThrottle) {
            updateThrottle(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingThrottle) {
            draggingThrottle = false;
            updateThrottle(mouseX);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateThrottle(double mouseX) {
        int next = Mth.clamp((int) ((mouseX - leftPos - 81.0D) * 30.0D / 32.0D), 0, 30);
        if (next != localThrottle) {
            localThrottle = next;
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), next,
                    CombustionEngineBlockEntity.CONTROL_THROTTLE);
        }
    }

    private static int pistonIndex(ItemStack stack) {
        if (stack.getItem() instanceof com.hbm.ntm.item.PistonSetItem piston) {
            return piston.type().ordinal();
        }
        return 0;
    }

    private static List<net.minecraft.util.FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}
