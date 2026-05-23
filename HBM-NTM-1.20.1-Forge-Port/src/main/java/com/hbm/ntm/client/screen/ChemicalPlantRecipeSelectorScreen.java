package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
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
import java.util.Locale;

public class ChemicalPlantRecipeSelectorScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_recipe_selector.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 132;
    private static final int RECIPES_PER_PAGE = 40;

    private final ChemicalPlantScreen previousScreen;
    private final List<GenericMachineRecipe> allRecipes;
    private final List<GenericMachineRecipe> visibleRecipes = new ArrayList<>();
    private String selection;
    private int leftPos;
    private int topPos;
    private int page;
    private EditBox search;

    protected ChemicalPlantRecipeSelectorScreen(ChemicalPlantScreen previousScreen) {
        super(Component.translatable("container.machineChemicalPlant"));
        this.previousScreen = previousScreen;
        this.selection = previousScreen.getMenu().getBlockEntity().getSelectedRecipeName();
        this.allRecipes = GenericMachineRecipeRuntime.recipes(
                previousScreen.getMenu().getBlockEntity().getLevel(),
                GenericMachineRecipe.Machine.CHEMICAL_PLANT);
        regenerateRecipes("");
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        search = new EditBox(font, leftPos + 28, topPos + 109, 102, 12, Component.empty());
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setMaxLength(32);
        addRenderableWidget(search);
        setInitialFocus(search);
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
            sendSelection();
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
            return true;
        }
        int index = hoveredRecipeIndex(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            String clicked = visibleRecipes.get(index).getInternalName();
            selection = clicked.equals(selection) ? GenericMachineRecipeRuntime.NULL_RECIPE : clicked;
            sendSelection();
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
        if (delta > 0) {
            page = Math.max(0, page - 1);
        } else if (delta < 0) {
            page = Math.min(maxPage(), page + 1);
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderHighlights(GuiGraphics graphics, int mouseX, int mouseY) {
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
            graphics.renderItem(recipeIcon(recipe), leftPos + x + 1, topPos + y + 1);
        }
        GenericMachineRecipe selectedRecipe = selectedRecipe();
        if (selectedRecipe != null) {
            graphics.renderItem(recipeIcon(selectedRecipe), leftPos + 153, topPos + 72);
        }
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = hoveredRecipeIndex(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            graphics.renderTooltip(font, splitTooltip(recipeTooltip(visibleRecipes.get(index))), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 151, 71, 18, 18) && selectedRecipe() != null) {
            graphics.renderTooltip(font, splitTooltip(recipeTooltip(selectedRecipe())), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 152, 90, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Close"), mouseX, mouseY);
        } else if (isInside(mouseX, mouseY, 134, 108, 16, 16)) {
            graphics.renderTooltip(font, Component.literal("Clear search"), mouseX, mouseY);
        }
    }

    private List<Component> recipeTooltip(GenericMachineRecipe recipe) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(recipe.getInternalName()));
        tooltip.add(Component.literal("Power: " + recipe.getPower() + " HE/t"));
        tooltip.add(Component.literal("Duration: " + recipe.getDuration() + " ticks"));
        return tooltip;
    }

    private static List<FormattedCharSequence> splitTooltip(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private void regenerateRecipes(String filter) {
        String needle = filter == null ? "" : filter.toLowerCase(Locale.ROOT);
        visibleRecipes.clear();
        for (GenericMachineRecipe recipe : allRecipes) {
            if (needle.isBlank()
                    || recipe.getInternalName().toLowerCase(Locale.ROOT).contains(needle)
                    || recipe.getId().toString().toLowerCase(Locale.ROOT).contains(needle)) {
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
        int column = relX / 18;
        int row = relY / 18;
        return page * 8 + row * 8 + column;
    }

    private int maxPage() {
        return Math.max(0, (int) Math.ceil(Math.max(0, visibleRecipes.size() - RECIPES_PER_PAGE) / 8.0D));
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    private GenericMachineRecipe selectedRecipe() {
        return allRecipes.stream()
                .filter(recipe -> recipe.getInternalName().equals(selection))
                .findFirst()
                .orElse(null);
    }

    private static ItemStack recipeIcon(GenericMachineRecipe recipe) {
        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        return result.isEmpty() ? recipe.getToastSymbol() : result;
    }

    private void sendSelection() {
        ModMessages.sendToServer(new TileControlPacket(
                previousScreen.getMenu().getBlockEntity().getBlockPos(),
                ChemicalPlantBlockEntity.recipeSelectionTag(selection)));
    }
}
