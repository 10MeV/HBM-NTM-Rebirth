package com.hbm.ntm.compat.jei;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.RotaryFurnaceRecipeRuntime;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public final class RotaryFurnaceRecipeCategory implements IRecipeCategory<RotaryFurnaceRecipeRuntime.Recipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 78;

    private final RecipeType<RotaryFurnaceRecipeRuntime.Recipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    RotaryFurnaceRecipeCategory(RecipeType<RotaryFurnaceRecipeRuntime.Recipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<RotaryFurnaceRecipeRuntime.Recipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_rotary_furnace", "Rotary Furnace");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RotaryFurnaceRecipeRuntime.Recipe recipe,
            IFocusGroup focuses) {
        int i = 0;
        for (RotaryFurnaceRecipeRuntime.IngredientSpec ingredient : recipe.ingredients()) {
            if (!ingredient.displayStacks().isEmpty()) {
                builder.addInputSlot(4 + i * 20, 10)
                        .addItemStacks(ingredient.displayStacks())
                        .setStandardSlotBackground();
                i++;
            }
        }
        if (recipe.fluid() != null) {
            JeiFluidSlots.addFluidSlot(builder,
                    new HbmFluidStack(recipe.fluid().type(), recipe.fluid().amount(), 0), true, 24, 40);
        }
    }

    @Override
    public void draw(RotaryFurnaceRecipeRuntime.Recipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 78, 24);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.literal("Output:"), 112, 12, 0x404040, false);
        guiGraphics.drawString(Minecraft.getInstance().font, materialLine(recipe.output()), 112, 24, 0x404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RotaryFurnaceRecipeRuntime.Recipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseY >= 56) {
            tooltip.add(materialLine(recipe.output()));
            tooltip.add(Component.literal("Duration: " + recipe.duration() + " ticks"));
            tooltip.add(Component.literal("Steam: " + recipe.steam() + " mB"));
        }
    }

    private static Component materialLine(MaterialStack stack) {
        return Component.translatable(stack.material.getUnlocalizedName())
                .append(": " + Mats.formatAmount(stack.amount, false));
    }
}