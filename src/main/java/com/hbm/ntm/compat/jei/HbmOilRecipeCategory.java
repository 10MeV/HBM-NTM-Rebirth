package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class HbmOilRecipeCategory implements HbmJeiRecipeCategory<HbmOilRecipe> {
    private static final int REFINERY_WIDTH = 176;
    private static final int REFINERY_HEIGHT = 85;
    private static final ResourceLocation REFINERY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_refinery.png");

    private final RecipeType<HbmOilRecipe> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final IDrawableAnimated refineryPower;
    private final IDrawableAnimated refineryProgress;
    private final ItemStack catalyst;
    private final boolean legacyUniversal;
    private final int width;
    private final int height;

    HbmOilRecipeCategory(RecipeType<HbmOilRecipe> type, Component title, ItemLike catalyst, IGuiHelper guiHelper) {
        this(type, title, catalyst, guiHelper, true);
    }

    HbmOilRecipeCategory(RecipeType<HbmOilRecipe> type, Component title, ItemLike catalyst, IGuiHelper guiHelper,
            boolean legacyUniversal) {
        this.type = type;
        this.title = title;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.catalyst = new ItemStack(catalyst);
        this.legacyUniversal = legacyUniversal;
        if (legacyUniversal) {
            this.width = LegacyNeiUniversalLayout.WIDTH;
            this.height = LegacyNeiUniversalLayout.HEIGHT;
            this.background = LegacyNeiUniversalLayout.background(guiHelper);
            this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
            this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
            this.refineryPower = null;
            this.refineryProgress = null;
        } else {
            this.width = REFINERY_WIDTH;
            this.height = REFINERY_HEIGHT;
            this.background = guiHelper.createDrawable(REFINERY_NEI_TEXTURE, 0, 0, REFINERY_WIDTH, REFINERY_HEIGHT);
            this.slotBackground = null;
            this.machineBackground = null;
            this.refineryPower = guiHelper.createAnimatedDrawable(
                    guiHelper.createDrawable(REFINERY_NEI_TEXTURE, 0, 86, 16, 52),
                    480, StartDirection.BOTTOM, false);
            this.refineryProgress = guiHelper.createAnimatedDrawable(
                    guiHelper.createDrawable(REFINERY_NEI_TEXTURE, 16, 86, 24, 17),
                    48, StartDirection.LEFT, false);
        }
    }

    @Override
    public RecipeType<HbmOilRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HbmOilRecipe recipe, IFocusGroup focuses) {
        if (legacyUniversal) {
            setLegacyUniversalRecipe(builder, recipe);
            return;
        }
        setLegacyRefineryRecipe(builder, recipe);
    }

    @Override
    public void draw(HbmOilRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        if (!legacyUniversal) {
            refineryPower.draw(guiGraphics, 3, 6);
            refineryProgress.draw(guiGraphics, 78, 24);
        }
    }

    private void setLegacyUniversalRecipe(IRecipeLayoutBuilder builder, HbmOilRecipe recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.itemInputs()) {
            inputs.add(List.of(ingredient.getItems()));
        }
        for (HbmFluidStack fluid : recipe.fluidInputs()) {
            if (!fluid.isEmpty()) {
                inputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(fluid)));
            }
        }
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, inputs);

        List<List<ItemStack>> outputs = new ArrayList<>();
        for (ItemStack stack : recipe.itemOutputs()) {
            if (!stack.isEmpty()) {
                outputs.add(List.of(stack));
            }
        }
        for (HbmFluidStack fluid : recipe.fluidOutputs()) {
            if (!fluid.isEmpty()) {
                outputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(fluid)));
            }
        }
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, outputs);
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    private void setLegacyRefineryRecipe(IRecipeLayoutBuilder builder, HbmOilRecipe recipe) {
        if (!recipe.fluidInputs().isEmpty()) {
            HbmFluidStack input = recipe.fluidInputs().get(0);
            if (!input.isEmpty()) {
                builder.addInputSlot(48, 24)
                        .addItemStack(LegacyNeiUniversalLayout.fluidIcon(input));
            }
        }

        int[][] fluidOutputCoords = {
                {111, 6},
                {129, 15},
                {111, 24},
                {129, 33}
        };
        int fluidOutputs = Math.min(recipe.fluidOutputs().size(), fluidOutputCoords.length);
        for (int i = 0; i < fluidOutputs; i++) {
            HbmFluidStack fluid = recipe.fluidOutputs().get(i);
            if (!fluid.isEmpty()) {
                int[] pos = fluidOutputCoords[i];
                builder.addOutputSlot(pos[0], pos[1])
                        .addItemStack(LegacyNeiUniversalLayout.fluidIcon(fluid));
            }
        }

        if (!recipe.itemOutputs().isEmpty()) {
            ItemStack solid = recipe.itemOutputs().get(0);
            if (!solid.isEmpty()) {
                builder.addOutputSlot(111, 42)
                        .addItemStack(solid);
            }
        }
    }
}
