package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.HotItem;
import com.hbm.ntm.recipe.AnvilSmithingRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.ModRecipes;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

public final class AnvilSmithingRecipeCategory implements HbmJeiRecipeCategory<AnvilSmithingRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 86;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_smithing.png");

    private final RecipeType<AnvilSmithingRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;

    AnvilSmithingRecipeCategory(RecipeType<AnvilSmithingRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 0, 0, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<AnvilSmithingRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Anvil");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilSmithingRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> left = displayInputStacks(recipe.left(), recipe.kind());
        List<ItemStack> right = displayInputStacks(recipe.right(), recipe.kind());
        if (!left.isEmpty()) {
            builder.addInputSlot(39, 24)
                    .addItemStacks(left);
        }
        if (!right.isEmpty()) {
            builder.addInputSlot(75, 24)
                    .addItemStacks(right);
        }

        ItemStack output = displayOutput(recipe, left, right);
        if (!output.isEmpty()) {
            builder.addOutputSlot(111, 24)
                    .addItemStack(output);
        }
    }

    @Override
    public void draw(AnvilSmithingRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        guiGraphics.drawString(Minecraft.getInstance().font, "Tier " + recipe.tier(), 52, 43, 0x404040, false);
    }

    static List<AnvilSmithingRecipe> recipes(RecipeManager recipeManager) {
        return recipeManager.getAllRecipesFor(ModRecipes.ANVIL_SMITHING.type().get()).stream()
                .sorted(Comparator.comparingInt(AnvilSmithingRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private static List<ItemStack> displayInputStacks(HbmIngredient input, AnvilSmithingRecipe.Kind kind) {
        return input.displayStacks().stream()
                .map(stack -> prepareInputStack(stack, kind))
                .toList();
    }

    private static ItemStack prepareInputStack(ItemStack stack, AnvilSmithingRecipe.Kind kind) {
        ItemStack copy = stack.copy();
        if (kind == AnvilSmithingRecipe.Kind.HOT && HotItem.isHotItem(copy)) {
            HotItem.heatUp(copy);
        }
        return copy;
    }

    private static ItemStack displayOutput(AnvilSmithingRecipe recipe, List<ItemStack> left, List<ItemStack> right) {
        if (recipe.kind() == AnvilSmithingRecipe.Kind.RENAME && !left.isEmpty()) {
            return left.get(0).copyWithCount(1);
        }
        if (!left.isEmpty() && !right.isEmpty()) {
            ItemStack result = recipe.result(left.get(0), right.get(0));
            if (!result.isEmpty()) {
                return result;
            }
        }
        return recipe.result();
    }
}
