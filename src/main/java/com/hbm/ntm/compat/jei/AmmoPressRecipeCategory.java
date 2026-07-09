package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.AmmoPressRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class AmmoPressRecipeCategory implements IRecipeCategory<AmmoPressRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei.png");

    private final RecipeType<AmmoPressRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    AmmoPressRecipeCategory(RecipeType<AmmoPressRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.slotBackground = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 87, 18, 18);
        this.machineBackground = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 59, 87, 18, 36);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<AmmoPressRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_ammo_press", "Ammo Press");
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
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AmmoPressRecipe recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = nonEmptyInputs(recipe);
        int[][] inputCoords = inputCoords(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            int[] pos = inputCoords[i];
            builder.addInputSlot(pos[0], pos[1])
                    .setBackground(slotBackground, -1, -1)
                    .addItemStacks(inputs.get(i));
        }

        builder.addOutputSlot(102, 24)
                .setBackground(slotBackground, -1, -1)
                .addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 31)
                .setBackground(machineBackground, -1, -17)
                .addItemStack(catalyst);
    }

    private static List<List<ItemStack>> nonEmptyInputs(AmmoPressRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (int slot = 0; slot < AmmoPressRecipe.INPUT_SLOTS; slot++) {
            List<ItemStack> stacks = recipe.displayInputs(slot);
            if (!stacks.isEmpty()) {
                inputs.add(stacks);
            }
        }
        return inputs;
    }

    private static int[][] inputCoords(int count) {
        return switch (count) {
            case 1 -> new int[][] {{48, 24}};
            case 2 -> new int[][] {{48, 24}, {30, 24}};
            case 3 -> new int[][] {{48, 24}, {30, 24}, {12, 24}};
            case 4 -> new int[][] {{48, 15}, {30, 15}, {48, 33}, {30, 33}};
            case 5 -> new int[][] {{48, 15}, {30, 15}, {12, 24}, {48, 33}, {30, 33}};
            case 6 -> new int[][] {{48, 15}, {30, 15}, {12, 15}, {48, 33}, {30, 33}, {12, 33}};
            case 7 -> new int[][] {{48, 6}, {30, 15}, {12, 15}, {48, 24}, {30, 33}, {12, 33}, {48, 42}};
            case 8 -> new int[][] {
                    {48, 6}, {30, 6}, {12, 15}, {48, 24}, {30, 24}, {12, 33}, {48, 42}, {30, 42}
            };
            case 9 -> new int[][] {
                    {48, 6}, {30, 6}, {12, 6}, {48, 24}, {30, 24}, {12, 24}, {48, 42}, {30, 42}, {12, 42}
            };
            default -> generatedCoords(count);
        };
    }

    private static int[][] generatedCoords(int count) {
        int[][] slots = new int[count][2];
        for (int i = 0; i < count; i++) {
            slots[i] = new int[] {i % 4 * 18, i / 4 * 18};
        }
        return slots;
    }

    @Override
    public void draw(AmmoPressRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }

    static List<AmmoPressRecipe> sorted(List<AmmoPressRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparingInt(AmmoPressRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }
}
