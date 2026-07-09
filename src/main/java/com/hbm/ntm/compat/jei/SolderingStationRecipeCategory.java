package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.SolderingStationRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class SolderingStationRecipeCategory implements IRecipeCategory<SolderingStationRecipe> {
    private static final int WIDTH = LegacyNeiUniversalLayout.WIDTH;
    private static final int HEIGHT = LegacyNeiUniversalLayout.HEIGHT;

    private final RecipeType<SolderingStationRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    SolderingStationRecipeCategory(RecipeType<SolderingStationRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<SolderingStationRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_soldering_station",
                "Soldering Station");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SolderingStationRecipe recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        addInputs(inputs, recipe.toppings());
        addInputs(inputs, recipe.pcb());
        addInputs(inputs, recipe.solder());
        recipe.fluid().ifPresent(input -> inputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(input))));
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, inputs);
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.output())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    @Override
    public void draw(SolderingStationRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        String duration = format(recipe.duration()) + " ticks";
        String consumption = format(recipe.consumption()) + " HE/t";
        int side = 160;
        guiGraphics.drawString(font, duration, side - font.width(duration), 43, 0x404040, false);
        guiGraphics.drawString(font, consumption, side - font.width(consumption), 55, 0x404040, false);
    }

    private static void addInputs(List<List<ItemStack>> inputs, List<HbmIngredient> ingredients) {
        for (HbmIngredient ingredient : ingredients) {
            inputs.add(ingredient.displayStacks());
        }
    }

    private static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }
}
