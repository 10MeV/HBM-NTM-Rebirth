package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CrucibleBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CrucibleRecipeSelectorScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_recipe_selector.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 132;
    private static final int RECIPES_PER_PAGE = 40;

    private final CrucibleScreen previousScreen;
    private final List<CrucibleRecipeRuntime.Recipe> allRecipes;
    private final List<CrucibleRecipeRuntime.Recipe> visibleRecipes = new ArrayList<>();
    private String selection;
    private int leftPos;
    private int topPos;
    private int page;
    private EditBox search;
    private boolean selectionSent;

    protected CrucibleRecipeSelectorScreen(CrucibleScreen previousScreen) {
        super(Component.translatableWithFallback("container.machineCrucible", "Crucible"));
        this.previousScreen = previousScreen;
        this.selection = previousScreen.getMenu().getBlockEntity().getSelectedRecipeName();
        this.allRecipes = CrucibleRecipeRuntime.recipes();
        regenerateRecipes("");
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        search = LegacyGuiElements.createLegacyTextField(font, leftPos + 28, topPos + 111, 102, 12, 32, "",
                0xFFFFFF);
        addRenderableWidget(search);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        renderHighlights(graphics, mouseX, mouseY);
        renderRecipeIcons(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHover(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInside(mouseX, mouseY, 152, 18, 16, 16)) {
            LegacyGuiElements.playClickSound();
            page = Math.max(0, page - 1);
            return true;
        }
        if (isInside(mouseX, mouseY, 152, 36, 16, 16)) {
            LegacyGuiElements.playClickSound();
            page = Math.min(maxPage(), page + 1);
            return true;
        }
        if (isInside(mouseX, mouseY, 151, 71, 18, 18)) {
            if (!CrucibleRecipeRuntime.NULL_RECIPE.equals(selection)) {
                LegacyGuiElements.playClickSound();
                selection = CrucibleRecipeRuntime.NULL_RECIPE;
            }
            return true;
        }
        if (isInside(mouseX, mouseY, 152, 90, 16, 16)) {
            sendSelection();
            minecraft.setScreen(previousScreen);
            return true;
        }
        if (isInside(mouseX, mouseY, 134, 108, 16, 16)) {
            search.setValue("");
            regenerateRecipes("");
            search.setFocused(true);
            return true;
        }
        int index = hoveredRecipeIndex(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            String clicked = visibleRecipes.get(index).internalName();
            LegacyGuiElements.playClickSound();
            selection = clicked.equals(selection) ? CrucibleRecipeRuntime.NULL_RECIPE : clicked;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        String previous = search.getValue();
        boolean typed = super.charTyped(codePoint, modifiers);
        if (!previous.equals(search.getValue())) {
            regenerateRecipes(search.getValue());
        }
        return typed;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            sendSelection();
            minecraft.setScreen(previousScreen);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            search.setFocused(!search.isFocused());
            return true;
        }
        if (LegacyGuiElements.isLegacyPageKey(keyCode)) {
            page = LegacyGuiElements.applyLegacyPageKey(page, maxPage(), keyCode);
            return true;
        }
        String previous = search.getValue();
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (!previous.equals(search.getValue())) {
            regenerateRecipes(search.getValue());
        }
        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        page = LegacyGuiElements.applyLegacyPageScroll(page, maxPage(), delta);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        sendSelection();
        super.removed();
    }

    private void renderHighlights(GuiGraphics graphics, int mouseX, int mouseY) {
        if (search != null && search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 26, topPos + 108, 0, 132, 106, 16);
        }
        if (isInside(mouseX, mouseY, 152, 18, 16, 16)) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 18, 176, 0, 16, 16);
        }
        if (isInside(mouseX, mouseY, 152, 36, 16, 16)) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 36, 176, 16, 16, 16);
        }
        if (isInside(mouseX, mouseY, 152, 90, 16, 16)) {
            graphics.blit(TEXTURE, leftPos + 152, topPos + 90, 176, 32, 16, 16);
        }
        if (isInside(mouseX, mouseY, 134, 108, 16, 16)) {
            graphics.blit(TEXTURE, leftPos + 134, topPos + 108, 176, 48, 16, 16);
        }
        if (isInside(mouseX, mouseY, 8, 108, 16, 16)) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 108, 176, 64, 16, 16);
        }
    }

    private void renderRecipeIcons(GuiGraphics graphics) {
        for (int i = page * 8; i < page * 8 + RECIPES_PER_PAGE && i < visibleRecipes.size(); i++) {
            CrucibleRecipeRuntime.Recipe recipe = visibleRecipes.get(i);
            int index = i - page * 8;
            int x = 7 + 18 * (index % 8);
            int y = 17 + 18 * (index / 8);
            if (recipe.internalName().equals(selection)) {
                graphics.blit(TEXTURE, leftPos + x, topPos + y, 192, 0, 18, 18);
            }
            graphics.renderItem(recipe.icon(), leftPos + x + 1, topPos + y + 1);
        }
        CrucibleRecipeRuntime.Recipe selectedRecipe = selectedRecipe();
        if (selectedRecipe != null) {
            graphics.renderItem(selectedRecipe.icon(), leftPos + 153, topPos + 72);
        }
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = hoveredRecipeIndex(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            LegacyGuiElements.renderRecipeTooltip(graphics, font,
                    CrucibleRecipeRuntime.displayLines(visibleRecipes.get(index)), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 151, 71, 18, 18) && selectedRecipe() != null) {
            LegacyGuiElements.renderRecipeTooltip(graphics, font,
                    CrucibleRecipeRuntime.displayLines(selectedRecipe()), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 152, 90, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Close"), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 134, 108, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Clear search"), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 8, 108, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Press ENTER to toggle focus")
                    .withStyle(ChatFormatting.ITALIC), mouseX, mouseY);
        }
    }

    private void regenerateRecipes(String filter) {
        visibleRecipes.clear();
        for (CrucibleRecipeRuntime.Recipe recipe : allRecipes) {
            if (CrucibleRecipeRuntime.matchesSearch(recipe, filter)) {
                visibleRecipes.add(recipe);
            }
        }
        page = 0;
    }

    private int hoveredRecipeIndex(double mouseX, double mouseY) {
        if (!isInside(mouseX, mouseY, 7, 17, 144, 90)) {
            return -1;
        }
        int relX = (int) mouseX - leftPos - 7;
        int relY = (int) mouseY - topPos - 17;
        int column = relX / 18;
        int row = relY / 18;
        return page * 8 + row * 8 + column;
    }

    private int maxPage() {
        return Math.max(0, (int) Math.ceil(Math.max(0, visibleRecipes.size() - RECIPES_PER_PAGE) / 8.0D));
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }

    private CrucibleRecipeRuntime.Recipe selectedRecipe() {
        return CrucibleRecipeRuntime.find(selection);
    }

    private void sendSelection() {
        if (selectionSent) {
            return;
        }
        selectionSent = true;
        ModMessages.sendTileControl(previousScreen.getMenu().getBlockEntity().getBlockPos(),
                CrucibleBlockEntity.recipeSelectionTag(selection));
    }
}
