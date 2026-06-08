package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.menu.AssemblyFactoryMenu;
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

public class AssemblyFactoryScreen extends AbstractContainerScreen<AssemblyFactoryMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_assembly_factory.png");

    public AssemblyFactoryScreen(AssemblyFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 240;
        titleLabelX = 92;
        titleLabelY = 6;
        inventoryLabelX = 33;
        inventoryLabelY = 148;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, 256, 140);
        graphics.blit(TEXTURE, leftPos + 25, topPos + 140, 25, 140, 231, 100);
        int power = menu.getPowerBarHeight(92);
        if (power > 0) {
            graphics.blit(TEXTURE, leftPos + 234, topPos + 110 - power, 0, 232 - power, 16, power);
        }
        for (int module = 0; module < 4; module++) {
            int ox = (module % 2) * 109;
            int oy = (module / 2) * 56;
            int progress = menu.getProgressWidth(module, 37);
            if (progress > 0) {
                graphics.blit(TEXTURE, leftPos + 45 + ox, topPos + 63 + oy, 0, 240, progress, 6);
            }
            GenericMachineRecipe recipe = menu.getBlockEntity().getSelectedRecipeDefinition(module);
            if (menu.getBlockEntity().isProcessing(module)) {
                graphics.blit(TEXTURE, leftPos + 45 + ox, topPos + 55 + oy, 4, 236, 4, 4);
                graphics.blit(TEXTURE, leftPos + 53 + ox, topPos + 55 + oy, 4, 236, 4, 4);
            } else if (recipe != null) {
                graphics.blit(TEXTURE, leftPos + 45 + ox, topPos + 55 + oy, 0, 236, 4, 4);
                if (menu.getPower() >= recipe.getPower() && menu.canCool()) {
                    graphics.blit(TEXTURE, leftPos + 53 + ox, topPos + 55 + oy, 0, 236, 4, 4);
                }
            }
            graphics.renderItem(recipeIcon(recipe), leftPos + 7 + ox, topPos + 54 + oy);
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 105 + ox, topPos + 52 + oy,
                    5, 32, menu.getInputTankData(module));
            LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 105 + ox, topPos + 70 + oy,
                    5, 16, menu.getOutputTankData(module));
            LegacyRecipeGhostRenderer.renderItemInputGhosts(graphics, minecraft, menu, TEXTURE, leftPos, topPos,
                    recipe, AssemblyFactoryBlockEntity.inputSlots(module));
        }
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 232, topPos + 201, 7, 52, menu.getWaterTankData());
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 241, topPos + 201, 7, 52, menu.getSpentSteamTankData());
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
        if (isHovering(234, 18, 16, 92, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 234, topPos + 18, 16, 92, menu.getPower(), menu.getMaxPower());
        } else {
            renderModuleTooltips(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int module = 0; module < 4; module++) {
            int ox = (module % 2) * 109;
            int oy = (module / 2) * 56;
            if (isHovering(6 + ox, 53 + oy, 18, 18, mouseX, mouseY)) {
                minecraft.setScreen(new AssemblyFactoryRecipeSelectorScreen(this, module));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderModuleTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int module = 0; module < 4; module++) {
            int ox = (module % 2) * 109;
            int oy = (module / 2) * 56;
            if (isHovering(6 + ox, 53 + oy, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.renderRecipeTooltip(graphics, font, recipeTooltip(module), mouseX, mouseY);
                return;
            }
            if (isHovering(105 + ox, 20 + oy, 5, 32, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTankData(module),
                        menu.getInputTankTooltip(module, hasShiftDown()), mouseX, mouseY);
                return;
            }
            if (isHovering(105 + ox, 54 + oy, 5, 16, mouseX, mouseY)) {
                LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getOutputTankData(module),
                        menu.getOutputTankTooltip(module, hasShiftDown()), mouseX, mouseY);
                return;
            }
        }
        if (isHovering(232, 149, 7, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getWaterTankData(),
                    menu.getWaterTankTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(241, 149, 7, 52, mouseX, mouseY)) {
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
