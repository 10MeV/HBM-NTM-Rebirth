package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AssemblyFactoryRecipeSelectorScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/processing/gui_recipe_selector.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 132;
    private static final int RECIPES_PER_PAGE = 40;

    private final AssemblyFactoryScreen previousScreen;
    private final int module;
    private final List<GenericMachineRecipe> allRecipes;
    private final List<GenericMachineRecipe> visibleRecipes = new ArrayList<>();
    private String selection;
    private int leftPos;
    private int topPos;
    private int page;
    private EditBox search;
    private boolean selectionSent;

    protected AssemblyFactoryRecipeSelectorScreen(AssemblyFactoryScreen previousScreen, int module) {
        super(Component.translatableWithFallback("container.machineAssemblyFactory", "Assembly Factory"));
        this.previousScreen = previousScreen;
        this.module = module;
        this.selection = previousScreen.getMenu().getBlockEntity().getSelectedRecipeName(module);
        this.allRecipes = GenericMachineRecipeSelector.recipes(
                previousScreen.getMenu().getBlockEntity().getLevel(),
                GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
                previousScreen.getMenu().getBlockEntity().getItems()
                        .getStackInSlot(AssemblyFactoryBlockEntity.blueprintSlot(module)));
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
            page = Math.max(0, page - 1);
            return true;
        }
        if (isInside(mouseX, mouseY, 152, 36, 16, 16)) {
            page = Math.min(maxPage(), page + 1);
            return true;
        }
        if (isInside(mouseX, mouseY, 151, 71, 18, 18)) {
            selection = GenericMachineRecipeRuntime.NULL_RECIPE;
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
            String clicked = visibleRecipes.get(index).getInternalName();
            selection = clicked.equals(selection) ? GenericMachineRecipeRuntime.NULL_RECIPE : clicked;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean typed = super.charTyped(codePoint, modifiers);
        regenerateRecipes(search.getValue());
        return typed;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            sendSelection();
            minecraft.setScreen(previousScreen);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            search.setFocused(!search.isFocused());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            page = Math.max(0, page - (keyCode == GLFW.GLFW_KEY_PAGE_UP ? 5 : 1));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            page = Math.min(maxPage(), page + (keyCode == GLFW.GLFW_KEY_PAGE_DOWN ? 5 : 1));
            return true;
        }
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        regenerateRecipes(search.getValue());
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
            GenericMachineRecipe recipe = visibleRecipes.get(i);
            int index = i - page * 8;
            int x = 7 + 18 * (index % 8);
            int y = 17 + 18 * (index / 8);
            if (recipe.getInternalName().equals(selection)) {
                graphics.blit(TEXTURE, leftPos + x, topPos + y, 192, 0, 18, 18);
            }
            graphics.renderItem(recipe.getIcon(), leftPos + x + 1, topPos + y + 1);
        }
        GenericMachineRecipe selectedRecipe = selectedRecipe();
        if (selectedRecipe != null) {
            graphics.renderItem(selectedRecipe.getIcon(), leftPos + 153, topPos + 72);
        }
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = hoveredRecipeIndex(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            LegacyGuiElements.renderRecipeTooltip(graphics, font, recipeTooltip(visibleRecipes.get(index)), mouseX,
                    mouseY);
        } else if (isInside(mouseX, mouseY, 151, 71, 18, 18) && selectedRecipe() != null) {
            LegacyGuiElements.renderRecipeTooltip(graphics, font, recipeTooltip(selectedRecipe()), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 152, 90, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Close"), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 134, 108, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Clear search"), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 8, 108, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Press ENTER to toggle focus")
                    .withStyle(ChatFormatting.ITALIC), mouseX, mouseY);
        }
    }

    private List<Component> recipeTooltip(GenericMachineRecipe recipe) {
        return recipe.getDisplayLines();
    }

    private void regenerateRecipes(String filter) {
        visibleRecipes.clear();
        for (GenericMachineRecipe recipe : allRecipes) {
            if (recipe.matchesSearch(filter)) {
                visibleRecipes.add(recipe);
            }
        }
        page = Math.min(page, maxPage());
    }

    private int hoveredRecipeIndex(double mouseX, double mouseY) {
        if (!isInside(mouseX, mouseY, 7, 17, 144, 90)) {
            return -1;
        }
        int relX = (int) mouseX - leftPos - 7;
        int relY = (int) mouseY - topPos - 17;
        return page * 8 + (relY / 18) * 8 + relX / 18;
    }

    private int maxPage() {
        return Math.max(0, (int) Math.ceil(Math.max(0, visibleRecipes.size() - RECIPES_PER_PAGE) / 8.0D));
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    private GenericMachineRecipe selectedRecipe() {
        return allRecipes.stream().filter(recipe -> recipe.getInternalName().equals(selection)).findFirst().orElse(null);
    }

    private static List<FormattedCharSequence> splitTooltip(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private void sendSelection() {
        if (selectionSent) {
            return;
        }
        selectionSent = true;
        ModMessages.sendTileControl(previousScreen.getMenu().getBlockEntity().getBlockPos(),
                AssemblyFactoryBlockEntity.recipeSelectionTag(module, selection));
    }
}
