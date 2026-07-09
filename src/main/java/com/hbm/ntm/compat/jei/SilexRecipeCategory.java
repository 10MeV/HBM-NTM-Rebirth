package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.recipe.SilexRecipe;
import com.hbm.ntm.recipe.SilexRecipeRuntime;
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
import net.minecraft.world.level.ItemLike;

public final class SilexRecipeCategory implements IRecipeCategory<SilexRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_silex.png");

    private final RecipeType<SilexRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;

    SilexRecipeCategory(RecipeType<SilexRecipeRuntime.DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<SilexRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("SILEX");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SilexRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        List<ItemStack> inputs = inputStacks(recipe);
        if (!inputs.isEmpty()) {
            builder.addInputSlot(12, 24)
                    .addItemStacks(inputs);
        }

        int index = 0;
        int outputCount = recipe.recipe().outputs().size();
        for (SilexRecipe.WeightedOutput output : recipe.recipe().outputs()) {
            ItemStack stack = output.stack();
            if (!stack.isEmpty()) {
                int[] pos = outputCoords(index, outputCount);
                builder.addOutputSlot(pos[0], pos[1])
                        .addItemStack(stack);
                index++;
            }
        }
    }

    @Override
    public void draw(SilexRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        int outputCount = recipe.recipe().outputs().size();
        int index = 0;
        for (SilexRecipe.WeightedOutput output : recipe.recipe().outputs()) {
            if (!output.stack().isEmpty()) {
                int[] pos = outputCoords(index, outputCount);
                guiGraphics.drawString(font, chancePercent(output, recipe.recipe()) + "%", pos[0] + 18, pos[1] + 4,
                        0x404040, false);
                index++;
            }
        }

        String amount = producedRatioLine(recipe.recipe()) + "x";
        guiGraphics.drawString(font, amount, 52 - font.width(amount) / 2, 43, 0x404040, false);

        Component wavelength = wavelengthLine(recipe.recipe().laserStrength());
        guiGraphics.drawString(font, wavelength, 33 - font.width(wavelength.getVisualOrderText()) / 2, 8,
                0x404040, false);
    }

    private static Component wavelengthLine(LaserWavelength wavelength) {
        if (wavelength == LaserWavelength.NULL) {
            return Component.literal("N/A");
        }
        return Component.translatable(wavelength.displayNameKey()).withStyle(wavelength.textColor());
    }

    private static String producedRatioLine(SilexRecipe recipe) {
        return legacyOneDecimal(recipe.fluidProduced() / recipe.fluidConsumed());
    }

    private static String chancePercent(SilexRecipe.WeightedOutput output, SilexRecipe recipe) {
        return legacyOneDecimal(100D * output.weight() / recipe.totalWeight());
    }

    private static String legacyOneDecimal(double value) {
        return Double.toString(((int) (value * 10D)) / 10D);
    }

    private static List<ItemStack> inputStacks(SilexRecipeRuntime.DisplayRecipe recipe) {
        if (!recipe.itemInputs().isEmpty()) {
            return recipe.itemInputs();
        }
        if (recipe.directFluidSource() && recipe.fluidInput().type() != HbmFluids.NONE
                && recipe.fluidInput().amount() > 0) {
            return List.of(FluidIconItem.make(recipe.fluidInput().type(), 0));
        }
        return List.of();
    }

    private static int[] outputCoords(int index, int totalOutputs) {
        int sep = totalOutputs > 4 ? 3 : 2;
        if (index < sep) {
            int columnCount = Math.min(totalOutputs, sep);
            return new int[] {68, 24 + index * 18 - 9 * ((columnCount + 1) / 2)};
        }
        int columnCount = Math.min(totalOutputs - sep, sep);
        return new int[] {116, 24 + (index - sep) * 18 - 9 * ((columnCount + 1) / 2)};
    }
}
