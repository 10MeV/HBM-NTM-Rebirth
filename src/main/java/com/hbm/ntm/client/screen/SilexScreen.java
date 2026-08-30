package com.hbm.ntm.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.SilexBlockEntity;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.menu.SilexMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.SilexRecipeRuntime;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;

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
        long gameTime = menu.getBlockEntity().getLevel() == null
                ? 0L : menu.getBlockEntity().getLevel().getGameTime();
        if (wavelength != LaserWavelength.NULL) {
            drawWave(graphics, 81, 46, 16, 84, 0.5F,
                    0.1F * (float) Math.pow(2, wavelength.ordinal()), guiColor(wavelength, gameTime), gameTime);
        }
        if (!menu.getTank().isEmpty()) {
            boolean valid = menu.getTank().type() == HbmFluids.PEROXIDE
                    || SilexRecipeRuntime.findFluidSource(menu.getBlockEntity().getLevel(), menu.getTank().type())
                            .isPresent();
            int v = valid ? 118 : 109;
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
            Component label = Component.translatable(wavelength.displayNameKey()).withStyle(wavelength.textColor());
            graphics.drawString(font, label, 100 + (32 - font.width(label) / 2), 16, 0, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 8, 42, 52, 7)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getTank(),
                    menu.getTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (menu.getCurrentFill() > 0
                && LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 27, 72, 16, 52)) {
            SilexBlockEntity silex = menu.getBlockEntity();
            List<Component> tooltip = List.of(Component.literal(menu.getCurrentFill() + "/"
                    + SilexBlockEntity.MAX_FILL + "mB"));
            Component source = !silex.getCurrentStack().isEmpty()
                    ? silex.getCurrentStack().getHoverName()
                    : silex.getCurrentFluid().getDisplayName();
            tooltip = List.of(tooltip.get(0), source);
            graphics.renderComponentTooltip(font,
                    tooltip, mouseX, mouseY);
        } else if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 10, 92, 10, 10)) {
            graphics.renderComponentTooltip(font,
                    List.of(Component.translatableWithFallback("gui.hbm_ntm_rebirth.silex.void", "Void contents")),
                    mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 10, 92, 12, 12)) {
            ModMessages.sendLegacyButton(menu.getBlockEntity(), 0, SilexBlockEntity.CONTROL_VOID);
            LegacyGuiElements.playClickSound();
            return true;
        }
        return handled;
    }

    private void drawWave(GuiGraphics graphics, int x, int y, int height, int width, float resolution, float freq,
            int color, long gameTime) {
        float samples = width / resolution;
        float scale = height / 2.0F;
        float offset = (float) (gameTime % (4.0D * Math.PI / freq));
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(3.0F);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 1; i <= samples; i++) {
            double currentX = offset + x + i * resolution;
            double currentY = y + scale * Math.sin(freq * currentX);
            buffer.vertex(matrix, leftPos + (float) (currentX - offset), topPos + (float) currentY, 0.0F)
                    .color(red, green, blue, 255)
                    .endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.lineWidth(1.0F);
    }

    private static int guiColor(LaserWavelength wavelength, long gameTime) {
        if (wavelength == LaserWavelength.VISIBLE) {
            return Mth.hsvToRgb(gameTime / 50.0F, 0.5F, 1.0F);
        }
        return wavelength.guiColor();
    }
}
