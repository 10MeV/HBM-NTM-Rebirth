package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.SilexBlockEntity;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.menu.SilexMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class SilexScreen extends AbstractContainerScreen<SilexMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_silex.png");

    public SilexScreen(SilexMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        titleLabelX = -54;
        titleLabelY = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        LaserWavelength wavelength = LaserWavelength.byOrdinal(menu.getModeOrdinal());
        if (wavelength != LaserWavelength.NULL) {
            drawWave(graphics, 81, 46, 16, 84, 0.5F,
                    0.1F * (float) Math.pow(2, wavelength.ordinal()), guiColor(wavelength));
        }
        if (!menu.getTank().isEmpty()) {
            int v = menu.getTank().type() == HbmFluids.PEROXIDE ? 118 : 109;
            graphics.blit(TEXTURE, leftPos + 7, topPos + 41, 176, v, 54, 9);
        }
        int progress = menu.getProgressWidth(69);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 45, topPos + 82, 176, 0, progress, 43);
        }
        int current = menu.getCurrentFillHeight(52);
        if (current > 0) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 124 - current, 176, 109 - current, 16, current);
        }
        int tank = menu.getTank().scaledFill(52);
        if (tank > 0) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 42, 176,
                    menu.getTank().type() == HbmFluids.PEROXIDE ? 43 : 50, tank, 7);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2 - 54, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        LaserWavelength wavelength = LaserWavelength.byOrdinal(menu.getModeOrdinal());
        if (wavelength != LaserWavelength.NULL) {
            Component label = Component.translatable(wavelengthNameKey(wavelength)).withStyle(wavelength.textColor());
            graphics.drawString(font, label, 100 + (32 - font.width(label) / 2), 16, 0, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(8, 42, 52, 7, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(),
                    menu.getTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(27, 72, 16, 52, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font,
                    List.of(Component.literal(menu.getCurrentFill() + "/" + SilexBlockEntity.MAX_FILL + "mB")),
                    mouseX, mouseY);
        } else if (isHovering(10, 92, 12, 12, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font,
                    List.of(Component.translatableWithFallback("gui.hbm_ntm_rebirth.silex.void", "Void contents")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(10, 92, 12, 12, mouseX, mouseY)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, SilexBlockEntity.CONTROL_VOID);
            LegacyGuiElements.playClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawWave(GuiGraphics graphics, int x, int y, int height, int width, float resolution, float freq,
            int color) {
        float samples = width / resolution;
        float scale = height / 2.0F;
        float offset = (float) (System.currentTimeMillis() / 50L % (4.0D * Math.PI / freq));
        int argb = 0xFF000000 | color;
        for (int i = 1; i < samples; i++) {
            double currentX = offset + x + i * resolution;
            double currentY = y + scale * Math.sin(freq * currentX);
            graphics.fill(leftPos + (int) Math.round(currentX - offset), topPos + (int) Math.round(currentY),
                    leftPos + (int) Math.round(currentX - offset) + 2,
                    topPos + (int) Math.round(currentY) + 2, argb);
        }
    }

    private static int guiColor(LaserWavelength wavelength) {
        if (wavelength == LaserWavelength.VISIBLE) {
            return Mth.hsvToRgb((System.currentTimeMillis() % 2500L) / 2500.0F, 0.5F, 1.0F);
        }
        return wavelength.guiColor();
    }

    private static String wavelengthNameKey(LaserWavelength wavelength) {
        return switch (wavelength) {
            case IR -> "wavelengths.name.ir";
            case VISIBLE -> "wavelengths.name.visible";
            case UV -> "wavelengths.name.uv";
            case GAMMA -> "wavelengths.name.gamma";
            case DRX -> "wavelengths.name.drx";
            default -> "";
        };
    }
}
