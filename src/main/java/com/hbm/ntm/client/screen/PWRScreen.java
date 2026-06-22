package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.menu.PWRMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PWRScreen extends AbstractContainerScreen<PWRMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_pwr.png");
    private EditBox controlField;

    public PWRScreen(PWRMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 188;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        controlField = LegacyGuiElements.createLegacyTextField(font, leftPos + 57, topPos + 63,
                30, 8, 3, Double.toString(100.0D - menu.getRodTargetExact()));
        addRenderableWidget(controlField);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (controlField != null && !controlField.isFocused()) {
            controlField.setValue(Double.toString(100.0D - menu.getRodTargetExact()));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getHullHeat() > PWRControllerHeat.hullDanger()
                || menu.getCoreHeat() > menu.getCoreHeatCapacity() * 8L / 10L) {
            graphics.blit(TEXTURE, leftPos + 147, topPos, 176, 14, 26, 26);
        }
        graphics.blit(TEXTURE, leftPos + 54, topPos + 33, 176, 0, menu.getProgressScaled(33), 14);
        graphics.blit(TEXTURE, leftPos + 53, topPos + 54, 176, 40, menu.getRodLevelScaled(52), 2);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 124, topPos + 40,
                menu.getCoreHeatCapacity() <= 0L ? 0.0D : menu.getCoreHeat() / (double) menu.getCoreHeatCapacity(),
                5, 2, 1, 0x7F0000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 160, topPos + 40,
                menu.getHullHeat() / (double) PWRControllerHeat.HULL_CAPACITY, 5, 2, 1, 0x7F0000);
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8, topPos + 57, 16, 52,
                menu.getCoolantTank());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 26, topPos + 57, 16, 52,
                menu.getHotCoolantTank());
        if (menu.getTypeLoaded() >= 0 && menu.getAmountLoaded() > 0) {
            ItemStack fuel = loadedFuelStack();
            LegacyGuiElements.renderItemWithLabel(graphics, font, fuel, leftPos + 89, topPos + 5,
                    "\u00a7e" + menu.getAmountLoaded() + "/" + menu.getRodCount());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        String flux = String.format(Locale.US, "%,.1f", menu.getFlux());
        graphics.pose().pushPose();
        float scale = 0.8F;
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, flux, (int) ((165 - font.width(flux) * scale) / scale),
                (int) (64 / scale), 0x00FF00, false);
        graphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(115, 31, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal(
                    "Core: " + String.format(Locale.US, "%,d", menu.getCoreHeat()) + " / "
                            + String.format(Locale.US, "%,d", menu.getCoreHeatCapacity()) + " TU")), mouseX, mouseY);
        } else if (isHovering(151, 31, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal(
                    "Hull: " + String.format(Locale.US, "%,d", menu.getHullHeat()) + " / "
                            + String.format(Locale.US, "%,d", PWRControllerHeat.HULL_CAPACITY) + " TU")),
                    mouseX, mouseY);
        } else if (isHovering(52, 31, 36, 18, mouseX, mouseY)) {
            int percent = (int) (menu.getProgress() * 100.0D / menu.getProcessTime());
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal(percent + "%")), mouseX, mouseY);
        } else if (isHovering(52, 53, 54, 4, mouseX, mouseY)) {
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal(
                    "Control rod level: " + (100.0D - Math.round(menu.getRodLevelExact() * 100.0D) / 100.0D) + "%")),
                    mouseX, mouseY);
        } else if (isHovering(88, 4, 18, 18, mouseX, mouseY)
                && menu.getTypeLoaded() >= 0 && menu.getAmountLoaded() > 0) {
            graphics.renderTooltip(font, loadedFuelStack(), mouseX, mouseY);
        } else if (isHovering(8, 5, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getCoolantTank().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        } else if (isHovering(26, 5, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getHotCoolantTank().tooltip(HbmFluidGuiHelper.showHiddenFluidInfo()),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (leftPos + 88 <= mouseX && mouseX < leftPos + 106 && topPos + 58 < mouseY && mouseY <= topPos + 76) {
            if (sendControl()) {
                playClick();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean sendControl() {
        int display;
        try {
            display = (int) Mth.clamp(Double.parseDouble(controlField.getValue()), 0.0D, 100.0D);
        } catch (NumberFormatException ignored) {
            return false;
        }
        controlField.setValue(Integer.toString(display));
        CompoundTag tag = new CompoundTag();
        tag.putInt("control", 100 - display);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
        return true;
    }

    private ItemStack loadedFuelStack() {
        int index = menu.getTypeLoaded();
        if (index < 0 || index >= com.hbm.ntm.registry.ModItems.PWR_FUEL_ITEMS.size()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(com.hbm.ntm.registry.ModItems.PWR_FUEL_ITEMS.get(index).get());
    }

    private void playClick() {
        LegacyGuiElements.playClickSound();
    }

    private static final class PWRControllerHeat {
        private static final long HULL_CAPACITY = 10_000_000L;

        private static long hullDanger() {
            return HULL_CAPACITY * 8L / 10L;
        }
    }
}
