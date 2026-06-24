package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.ICFPelletRecipeRuntime;
import java.util.Locale;
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
import net.minecraft.world.level.ItemLike;

public final class ICFPelletRecipeCategory implements IRecipeCategory<ICFPelletRecipeRuntime.DisplayPellet> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 58;

    private final RecipeType<ICFPelletRecipeRuntime.DisplayPellet> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    ICFPelletRecipeCategory(RecipeType<ICFPelletRecipeRuntime.DisplayPellet> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ICFPelletRecipeRuntime.DisplayPellet> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("container.machineICFPress", "ICF Fuel Press");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ICFPelletRecipeRuntime.DisplayPellet recipe,
            IFocusGroup focuses) {
        builder.addOutputSlot(116, 20)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(ICFPelletRecipeRuntime.DisplayPellet recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 62, 20);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ICFPelletRecipeRuntime.DisplayPellet recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Fuel: ")
                .append(Component.translatable(fuelKey(recipe.first())))
                .append(Component.literal(" / "))
                .append(Component.translatable(fuelKey(recipe.second()))));
        tooltip.add(Component.literal("Heat required: " + recipe.fusingDifficulty() + " TU"));
        tooltip.add(Component.literal("Max depletion: " + recipe.maxDepletion()));
        tooltip.add(Component.literal(String.format(Locale.US, "Reactivity multiplier: x%.2f",
                recipe.reactionMultiplier())));
        if (recipe.muon()) {
            tooltip.add(Component.literal("Muon catalyzed"));
        }
    }

    private static String fuelKey(com.hbm.ntm.item.ICFPelletItem.FuelType type) {
        return "icffuel." + type.name().toLowerCase(Locale.US);
    }
}
