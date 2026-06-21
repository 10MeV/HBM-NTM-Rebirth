package com.hbm.ntm.client.screen;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.CrucibleMenu;
import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CrucibleScreen extends AbstractContainerScreen<CrucibleMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_crucible.png");

    public CrucibleScreen(CrucibleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 214;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getProgressPixels();
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 126, topPos + 82, 176, 0, progress, 5);
        }
        int heat = menu.getHeatPixels();
        if (heat > 0) {
            graphics.blit(TEXTURE, leftPos + 126, topPos + 91, 176, 5, heat, 5);
        }
        renderMaterialStack(graphics, menu.getWasteStacks(), menu.wasteCapacity(), 17, 97);
        renderMaterialStack(graphics, menu.getRecipeStacks(), menu.recipeCapacity(), 62, 97);
        graphics.renderItem(recipeIcon(), leftPos + 107, topPos + 81);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isHovering(16, 17, 36, 81, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getWasteTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(61, 17, 36, 81, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, menu.getRecipeTooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isHovering(125, 81, 34, 7, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getProgressText()), mouseX, mouseY);
        } else if (isHovering(125, 90, 34, 7, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.getHeatText()), mouseX, mouseY);
        } else if (isHovering(106, 80, 18, 18, mouseX, mouseY)) {
            LegacyGuiElements.renderRecipeTooltip(graphics, font, recipeTooltip(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(106, 80, 18, 18, mouseX, mouseY)) {
            minecraft.setScreen(new CrucibleRecipeSelectorScreen(this));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderMaterialStack(GuiGraphics graphics, List<MaterialStack> stacks, int capacity, int x, int y) {
        int lastHeight = 0;
        int previous = 0;
        for (MaterialStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int targetHeight = menu.getStackPixels(stack, capacity, previous);
            if (targetHeight <= lastHeight) {
                previous += stack.amount;
                continue;
            }
            int color = stack.material.moltenColor;
            graphics.setColor(((color >> 16) & 255) / 255.0F,
                    ((color >> 8) & 255) / 255.0F,
                    (color & 255) / 255.0F, 1.0F);
            graphics.blit(TEXTURE, leftPos + x, topPos + y - targetHeight,
                    176, 89 - targetHeight, 34, targetHeight - lastHeight);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            previous += stack.amount;
            lastHeight = targetHeight;
        }
    }

    private ItemStack recipeIcon() {
        CrucibleRecipeRuntime.Recipe recipe = menu.getSelectedRecipe();
        return recipe == null ? LegacyGuiElements.templateFolderStack() : recipe.icon();
    }

    private List<Component> recipeTooltip() {
        CrucibleRecipeRuntime.Recipe recipe = menu.getSelectedRecipe();
        if (recipe == null) {
            return List.of(Component.translatableWithFallback("gui.recipe.setRecipe", "Set recipe")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return new ArrayList<>(CrucibleRecipeRuntime.displayLines(recipe));
    }
}
