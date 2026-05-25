package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.menu.FluidTankMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class FluidTankScreen extends AbstractContainerScreen<FluidTankMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/gui_tank.png");

    public FluidTankScreen(FluidTankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(TEXTURE, leftPos + 151, topPos + 34, 176, menu.getMode() * 18, 18, 18);

        int tank = menu.getTankFillHeight(52);
        if (tank > 0) {
            int color = menu.getBlockEntity().getTank().getTankType().getColor();
            graphics.fill(leftPos + 71, topPos + 17 + 52 - tank, leftPos + 105, topPos + 69,
                    0xCC000000 | (color & 0xFFFFFF));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(71, 17, 34, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, menu.getTankInfo(), mouseX, mouseY);
        } else if (isHovering(151, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, modeTooltip().stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(151, 34, 18, 18, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity().getBlockPos(), 0, FluidTankBlockEntity.CONTROL_MODE);
            return true;
        }
        return handled;
    }

    private List<Component> modeTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("container.fluidtank.mode"));
        tooltip.add(Component.translatable("container.fluidtank.mode." + modeName(menu.getMode())).withStyle(ChatFormatting.YELLOW));
        if (menu.isExploded()) {
            tooltip.add(Component.translatable("container.fluidtank.damaged").withStyle(ChatFormatting.RED));
        } else if (menu.isOnFire()) {
            tooltip.add(Component.translatable("container.fluidtank.burning").withStyle(ChatFormatting.RED));
        }
        return tooltip;
    }

    private static String modeName(int mode) {
        return switch (mode) {
            case FluidTankBlockEntity.MODE_BUFFER -> "buffer";
            case FluidTankBlockEntity.MODE_OUTPUT -> "output";
            case FluidTankBlockEntity.MODE_NONE -> "none";
            default -> "input";
        };
    }
}
