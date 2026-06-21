package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.FusionPlasmaForgeMenu;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusionPlasmaForgeScreen extends AbstractContainerScreen<FusionPlasmaForgeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_fusion_plasmaforge.png");

    public FusionPlasmaForgeScreen(FusionPlasmaForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 244;
        inventoryLabelX = 8;
        inventoryLabelY = 150;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(62);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 80 - power, 176, 62 - power, 16, power);
        }
        int progress = menu.getProgressWidth(70);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 81, 176, 62, progress, 16);
        }
        GenericMachineRecipe recipe = selectedRecipe();
        if (menu.getBlockEntity().didProcess()) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 76, 195, 0, 3, 6);
            graphics.blit(TEXTURE, leftPos + 56, topPos + 76, 195, 0, 3, 6);
        } else if (recipe != null) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 76, 192, 0, 3, 6);
            if (menu.getPower() >= recipe.getPower()) {
                graphics.blit(TEXTURE, leftPos + 56, topPos + 76, 192, 0, 3, 6);
            }
        }
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 124,
                plasmaInputGauge(recipe), 5, 2, 1, 0xA00000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 70, topPos + 124,
                menu.getMaxBooster() <= 0 ? 0.0D : menu.getBooster() / (double) menu.getMaxBooster(),
                5, 2, 1, 0xA00000);
        graphics.renderItem(recipe == null ? LegacyGuiElements.templateFolderStack() : recipe.getIcon(),
                leftPos + 8, topPos + 81);
        LegacyRecipeGhostRenderer.renderItemInputGhosts(graphics, minecraft, menu, TEXTURE, leftPos, topPos,
                recipe, new int[] {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 80, topPos + 70, 16, 52, menu.getInputTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(7, 80, 18, 18, mouseX, mouseY)) {
            GenericMachineRecipe recipe = selectedRecipe();
            if (recipe != null) {
                LegacyGuiElements.renderRecipeTooltip(graphics, font, recipe.getDisplayLines(), mouseX, mouseY);
            } else {
                graphics.renderTooltip(font,
                        Component.translatableWithFallback("gui.recipe.setRecipe", "Select recipe")
                                .withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
            }
        } else if (isHovering(80, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTank(),
                    menu.getInputTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(152, 18, 16, 62, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 62, menu.getPower(), menu.getMaxPower());
        } else if (isHovering(62, 81, 70, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal(String.format(Locale.US, "Progress: %.1f%%", menu.getProgress() * 100.0D)),
                    Component.literal(shortNumber(menu.getPlasmaEnergy()) + " TU")), mouseX, mouseY);
        } else if (isHovering(25, 115, 18, 18, mouseX, mouseY)) {
            long ignition = selectedRecipe() == null ? 0L : selectedRecipe().getExtraData().plasmaForge()
                    .map(GenericMachineRecipeExtraData.PlasmaForge::ignitionTemp).orElse(0L);
            LegacyGuiElements.renderTooltip(graphics, font, List.of(Component.literal("-> "
                    + shortNumber(menu.getPlasmaEnergy()) + "TU / " + shortNumber(ignition) + "TU")), mouseX, mouseY);
        } else if (isHovering(98, 116, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.literal("Booster: " + menu.getBooster() + " / " + menu.getMaxBooster()),
                    Component.literal("Co-60, Sr-90, Au-198, I-131, Xe-135, Cs-137, At-209")), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 7, 80, 18, 18)) {
            LegacyGuiElements.playClickSound();
            minecraft.setScreen(new FusionPlasmaForgeRecipeSelectorScreen(this));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String shortNumber(long value) {
        if (value >= 1_000_000L) return String.format(Locale.US, "%.1fM", value / 1_000_000.0D);
        if (value >= 1_000L) return String.format(Locale.US, "%.1fk", value / 1_000.0D);
        return Long.toString(value);
    }

    private GenericMachineRecipe selectedRecipe() {
        return menu.getBlockEntity().getSelectedRecipeDefinition();
    }

    private double plasmaInputGauge(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return 0.0D;
        }
        long ignition = recipe.getExtraData().plasmaForge()
                .map(GenericMachineRecipeExtraData.PlasmaForge::ignitionTemp)
                .orElse(0L);
        return ignition <= 0L ? 0.0D : Math.min(menu.getPlasmaEnergy() / (double) ignition, 1.5D) / 1.5D;
    }
}
