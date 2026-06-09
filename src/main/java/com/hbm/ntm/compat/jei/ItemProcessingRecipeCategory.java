package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemProcessingRecipeCategory implements IRecipeCategory<ItemProcessingRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<ItemProcessingRecipe> type;
    private final ItemProcessingRecipe.Machine machine;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    ItemProcessingRecipeCategory(RecipeType<ItemProcessingRecipe> type, ItemProcessingRecipe.Machine machine,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.machine = machine;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ItemProcessingRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return switch (machine) {
            case SHREDDER -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_shredder", "Shredder");
            case CENTRIFUGE -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_centrifuge", "Centrifuge");
            case CRYSTALLIZER -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_crystallizer", "Crystallizer");
        };
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
    public void setRecipe(IRecipeLayoutBuilder builder, ItemProcessingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, recipe.fluidInput().isPresent() ? 8 : 26)
                .addItemStacks(recipe.input().displayStacks())
                .setStandardSlotBackground();
        recipe.fluidInput().ifPresent(input -> addFluidSlot(builder, input, true, 4, 34));

        List<HbmItemOutput> outputs = recipe.outputs();
        for (int i = 0; i < outputs.size(); i++) {
            List<ItemStack> stacks = outputs.get(i).displayStacks();
            if (!stacks.isEmpty()) {
                builder.addOutputSlot(118 + (i % 2) * 20, 8 + (i / 2) * 22)
                        .addItemStacks(stacks)
                        .setOutputSlotBackground();
            }
        }
    }

    @Override
    public void draw(ItemProcessingRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 76, 26);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ItemProcessingRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseY >= 54) {
            if (recipe.duration() > 0) {
                tooltip.add(Component.literal("Duration: " + recipe.duration() + " ticks"));
            }
            if (recipe.productivity() > 0.0F) {
                tooltip.add(Component.literal("Productivity: " + recipe.productivity()));
            }
        }
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, HbmFluidStack hbmStack,
            boolean input, int x, int y) {
        JeiFluidSlots.addFluidSlot(builder, hbmStack, input, x, y);
    }
}
