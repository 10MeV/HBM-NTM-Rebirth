package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ChemicalPlantMenu;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChemicalPlantScreen extends AbstractContainerScreen<ChemicalPlantMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_chemplant.png");

    public ChemicalPlantScreen(ChemicalPlantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 256;
        titleLabelX = 70;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(61);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 79 - power, 176, 61 - power, 16, power);
        }
        int progress = menu.getProgressWidth(70);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 126, 176, 61, progress, 16);
        }
        for (int i = 0; i < 3; i++) {
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 8 + i * 18, topPos + 52,
                    16, 34, menu.getInputTankData(i));
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 80 + i * 18, topPos + 52,
                    16, 34, menu.getOutputTankData(i));
        }
        GenericMachineRecipe recipe = menu.getBlockEntity().getSelectedRecipeDefinition();
        if (menu.getBlockEntity().canProcessSelectedRecipe()) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 121, 195, 0, 3, 6);
            graphics.blit(TEXTURE, leftPos + 56, topPos + 121, 195, 0, 3, 6);
        } else if (recipe != null) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 121, 192, 0, 3, 6);
            if (menu.getPower() >= recipe.getPower()) {
                graphics.blit(TEXTURE, leftPos + 56, topPos + 121, 192, 0, 3, 6);
            }
        }
        graphics.renderItem(recipeIcon(recipe), leftPos + 8, topPos + 126);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), titleLabelX, titleLabelY, 124, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, splitTooltip(recipeTooltip()), mouseX, mouseY);
        } else if (isHovering(152, 18, 16, 61, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"), mouseX, mouseY);
        } else {
            renderTankTooltip(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(7, 125, 18, 18, mouseX, mouseY)) {
            minecraft.setScreen(new ChemicalPlantRecipeSelectorScreen(this));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderTankTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = 0; i < 3; i++) {
            if (isHovering(8 + i * 18, 18, 16, 34, mouseX, mouseY)) {
                graphics.renderTooltip(font, splitTooltip(menu.getInputTankTooltip(i, hasShiftDown())), mouseX, mouseY);
                return;
            }
            if (isHovering(80 + i * 18, 18, 16, 34, mouseX, mouseY)) {
                graphics.renderTooltip(font, splitTooltip(menu.getOutputTankTooltip(i, hasShiftDown())), mouseX, mouseY);
                return;
            }
        }
    }

    private List<Component> recipeTooltip() {
        GenericMachineRecipe recipe = menu.getBlockEntity().getSelectedRecipeDefinition();
        List<Component> tooltip = new ArrayList<>();
        if (recipe == null) {
            tooltip.add(Component.literal("Set recipe"));
        } else {
            tooltip.add(Component.literal(recipe.getInternalName()));
            tooltip.add(Component.literal("Power: " + recipe.getPower() + " HE/t"));
            tooltip.add(Component.literal("Duration: " + recipe.getDuration() + " ticks"));
        }
        return tooltip;
    }

    private static List<FormattedCharSequence> splitTooltip(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private static ItemStack recipeIcon(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        return result.isEmpty() ? recipe.getToastSymbol() : result;
    }
}
