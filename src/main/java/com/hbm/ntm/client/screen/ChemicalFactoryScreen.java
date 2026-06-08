package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.menu.ChemicalFactoryMenu;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ChemicalFactoryScreen extends AbstractContainerScreen<ChemicalFactoryMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_chemical_factory.png");

    public ChemicalFactoryScreen(ChemicalFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 248;
        imageHeight = 216;
        titleLabelX = 84;
        titleLabelY = 6;
        inventoryLabelX = 26;
        inventoryLabelY = 124;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 248, 116);
        graphics.blit(TEXTURE, leftPos + 18, topPos + 116, 18, 116, 230, 100);
        int power = menu.getPowerBarHeight(68);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 224, topPos + 86 - power, 0, 184 - power, 16, power);
        }
        for (int module = 0; module < 4; module++) {
            int y = module * 22;
            int progress = menu.getProgressWidth(module, 22);
            if (progress > 0) {
                graphics.blit(TEXTURE, leftPos + 113, topPos + 29 + y, 0, 216, progress, 6);
            }
            GenericMachineRecipe recipe = menu.getBlockEntity().getSelectedRecipeDefinition(module);
            if (menu.getBlockEntity().isProcessing(module)) {
                graphics.blit(TEXTURE, leftPos + 113, topPos + 21 + y, 4, 222, 4, 4);
                graphics.blit(TEXTURE, leftPos + 121, topPos + 21 + y, 4, 222, 4, 4);
            } else if (recipe != null) {
                graphics.blit(TEXTURE, leftPos + 113, topPos + 21 + y, 0, 222, 4, 4);
                if (menu.getPower() >= recipe.getPower() && menu.canCool()) {
                    graphics.blit(TEXTURE, leftPos + 121, topPos + 21 + y, 0, 222, 4, 4);
                }
            }
            graphics.renderItem(recipeIcon(recipe), leftPos + 75, topPos + 20 + y);
            for (int tank = 0; tank < 3; tank++) {
                LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 60 + tank * 5, topPos + 36 + y,
                        3, 16, menu.getInputTankData(module, tank));
                LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 189 + tank * 5, topPos + 36 + y,
                        3, 16, menu.getOutputTankData(module, tank));
            }
            LegacyRecipeGhostRenderer.renderItemInputGhosts(graphics, minecraft, menu, TEXTURE, leftPos, topPos,
                    recipe, ChemicalFactoryBlockEntity.inputSlots(module));
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 224, topPos + 177, 7, 52, menu.getWaterTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 233, topPos + 177, 7, 52, menu.getSpentSteamTankData());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        LegacyGuiText.drawCenteredLabel(graphics, font, title.getString(), titleLabelX, titleLabelY, 120, 0x404040);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(224, 18, 16, 68, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"), mouseX, mouseY);
        } else {
            renderModuleTooltips(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int module = 0; module < 4; module++) {
            int y = module * 22;
            if (isHovering(74, 19 + y, 18, 18, mouseX, mouseY)) {
                minecraft.setScreen(new ChemicalFactoryRecipeSelectorScreen(this, module));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderModuleTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int module = 0; module < 4; module++) {
            int y = module * 22;
            if (isHovering(74, 19 + y, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.renderRecipeTooltip(graphics, font, recipeTooltip(module), mouseX, mouseY);
                return;
            }
            for (int tank = 0; tank < 3; tank++) {
                if (isHovering(60 + tank * 5, 20 + y, 3, 16, mouseX, mouseY)) {
                    LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTankData(module, tank),
                            menu.getInputTankTooltip(module, tank, hasShiftDown()), mouseX, mouseY);
                    return;
                }
                if (isHovering(189 + tank * 5, 20 + y, 3, 16, mouseX, mouseY)) {
                    LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTankData(module, tank),
                            menu.getOutputTankTooltip(module, tank, hasShiftDown()), mouseX, mouseY);
                    return;
                }
            }
        }
        if (isHovering(224, 125, 7, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getWaterTankData(),
                    menu.getWaterTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(233, 125, 7, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getSpentSteamTankData(),
                    menu.getSpentSteamTankTooltip(hasShiftDown()), mouseX, mouseY);
        }
    }

    private List<Component> recipeTooltip(int module) {
        GenericMachineRecipe recipe = menu.getBlockEntity().getSelectedRecipeDefinition(module);
        if (recipe == null) {
            return List.of(Component.translatableWithFallback("gui.recipe.setRecipe", "Set recipe")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return recipe.getDisplayLines();
    }

    private static List<FormattedCharSequence> splitTooltip(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private static ItemStack recipeIcon(GenericMachineRecipe recipe) {
        return recipe == null ? new ItemStack(ModItems.TEMPLATE_FOLDER.get()) : recipe.getIcon();
    }
}
