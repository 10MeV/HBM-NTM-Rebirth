package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.menu.AmmoPressMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.AmmoPressRecipe;
import com.hbm.ntm.recipe.AmmoPressRecipeRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AmmoPressScreen extends AbstractContainerScreen<AmmoPressMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_ammo_press.png");
    private static final int RECIPES_PER_PAGE = 12;
    private static final int RECIPE_COLUMNS = 4;
    private static final int RECIPE_ROWS = 3;

    private final List<AmmoPressRecipe> visibleRecipes = new ArrayList<>();
    private EditBox search;
    private int page;

    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 10, topPos + 75, 66, 12, Component.empty());
        search.setTextColor(0xFFFFFF);
        search.setTextColorUneditable(0xFFFFFF);
        search.setBordered(false);
        search.setMaxLength(25);
        search.setResponder(ignored -> refreshRecipes());
        addRenderableWidget(search);
        refreshRecipes();
    }

    private void refreshRecipes() {
        String filter = search == null ? "" : search.getValue().toLowerCase(Locale.US);
        visibleRecipes.clear();
        for (AmmoPressRecipe recipe : AmmoPressRecipeRuntime.recipes(minecraft.level)) {
            if (filter.isBlank() || recipe.output().getHoverName().getString().toLowerCase(Locale.US).contains(filter)) {
                visibleRecipes.add(recipe);
            }
        }
        page = Mth.clamp(page, 0, maxPage());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (isHovering(7, 17, 9, 54, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 7, topPos + 17, 176, 0, 9, 54);
        }
        if (isHovering(88, 17, 9, 54, mouseX, mouseY)) {
            graphics.blit(TEXTURE, leftPos + 88, topPos + 17, 185, 0, 9, 54);
        }
        if (search != null && search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 72, 176, 54, 70, 16);
        }
        renderRecipeGrid(graphics, mouseX, mouseY);
        renderGhostInputs(graphics);
    }

    private void renderRecipeGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        List<AmmoPressRecipe> allRecipes = AmmoPressRecipeRuntime.recipes(minecraft.level);
        int start = page * 3;
        int end = Math.min(start + RECIPES_PER_PAGE, visibleRecipes.size());
        for (int i = start; i < end; i++) {
            int visibleIndex = i - start;
            int x = 16 + 18 * (visibleIndex / RECIPE_ROWS);
            int y = 17 + 18 * (visibleIndex % RECIPE_ROWS);
            AmmoPressRecipe recipe = visibleRecipes.get(i);
            graphics.renderItem(recipe.output(), leftPos + x + 1, topPos + y + 1);
            graphics.renderItemDecorations(font, recipe.output(), leftPos + x + 1, topPos + y + 1,
                    recipe.output().getCount() > 1 ? Integer.toString(recipe.output().getCount()) : null);
            int frameU = menu.getSelectedRecipeIndex() == allRecipes.indexOf(recipe) ? 194 : 212;
            graphics.blit(TEXTURE, leftPos + x, topPos + y, frameU, 0, 18, 18);
        }
    }

    private void renderGhostInputs(GuiGraphics graphics) {
        List<AmmoPressRecipe> allRecipes = AmmoPressRecipeRuntime.recipes(minecraft.level);
        int selected = menu.getSelectedRecipeIndex();
        if (selected < 0 || selected >= allRecipes.size()) {
            return;
        }
        AmmoPressRecipe recipe = allRecipes.get(selected);
        for (int slot = 0; slot < AmmoPressRecipe.INPUT_SLOTS; slot++) {
            if (menu.getBlockEntity().getItems().getStackInSlot(slot).isEmpty()) {
                List<ItemStack> options = recipe.displayInputs(slot);
                if (!options.isEmpty()) {
                    ItemStack display = options.get((int) ((System.currentTimeMillis() / 1000L) % options.size()));
                    int x = leftPos + 116 + 18 * (slot % 3);
                    int y = topPos + 18 + 18 * (slot / 3);
                    graphics.renderItem(display, x, y);
                    graphics.renderItemDecorations(font, display, x, y,
                            display.getCount() > 1 ? Integer.toString(display.getCount()) : null);
                    graphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0x88FFFFFF);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderRecipeTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderRecipeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Slot slot : menu.slots) {
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.hasItem()) {
                return;
            }
        }
        int index = recipeIndexAt(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            graphics.renderTooltip(font, visibleRecipes.get(index).output(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isHovering(7, 17, 9, 54, mouseX, mouseY)) {
            page = Math.max(0, page - 1);
            return true;
        }
        if (isHovering(88, 17, 9, 54, mouseX, mouseY)) {
            page = Math.min(maxPage(), page + 1);
            return true;
        }
        int index = recipeIndexAt(mouseX, mouseY);
        if (index >= 0 && index < visibleRecipes.size()) {
            int selection = AmmoPressRecipeRuntime.recipes(minecraft.level).indexOf(visibleRecipes.get(index));
            CompoundTag tag = AmmoPressBlockEntity.selectionControlTag(selection);
            ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (leftPos <= mouseX && leftPos + imageWidth > mouseX && topPos < mouseY && topPos + imageHeight >= mouseY) {
            if (delta > 0.0D && page > 0) {
                page--;
                return true;
            }
            if (delta < 0.0D && page < maxPage()) {
                page++;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int recipeIndexAt(double mouseX, double mouseY) {
        int start = page * 3;
        int end = Math.min(start + RECIPES_PER_PAGE, visibleRecipes.size());
        for (int i = start; i < end; i++) {
            int visibleIndex = i - start;
            int x = 16 + 18 * (visibleIndex / RECIPE_ROWS);
            int y = 17 + 18 * (visibleIndex % RECIPE_ROWS);
            if (isHovering(x, y, 18, 18, mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    private int maxPage() {
        return Math.max(0, (int) Math.ceil(Math.max(0, visibleRecipes.size() - RECIPES_PER_PAGE) / 3.0D));
    }
}
