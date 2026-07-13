package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.RotaryFurnaceRecipeRuntime;
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

public final class RotaryFurnaceRecipeCategory implements HbmJeiRecipeCategory<RotaryFurnaceRecipeRuntime.Recipe> {
    private static final int WIDTH = LegacyNeiUniversalLayout.WIDTH;
    private static final int HEIGHT = LegacyNeiUniversalLayout.HEIGHT;

    private final RecipeType<RotaryFurnaceRecipeRuntime.Recipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    RotaryFurnaceRecipeCategory(RecipeType<RotaryFurnaceRecipeRuntime.Recipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
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
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RotaryFurnaceRecipeRuntime.Recipe recipe,
            IFocusGroup focuses) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (HbmIngredient ingredient : recipe.ingredients()) {
            inputs.add(ingredient.displayStacks());
        }
        if (recipe.fluid() != null) {
            inputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.fluid())));
        }
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, inputs);
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground,
                List.of(List.of(FoundryScrapsItem.create(recipe.output(), true))));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    @Override
    public void draw(RotaryFurnaceRecipeRuntime.Recipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        var font = Minecraft.getInstance().font;
        String duration = format(recipe.duration()) + " ticks";
        String steam = HbmFluids.STEAM.getDisplayName().getString() + ": " + format(recipe.steam()) + " mB/t";
        int side = 160;
        guiGraphics.drawString(font, duration, side - font.width(duration), 43, 0x404040, false);
        guiGraphics.drawString(font, steam, side - font.width(steam), 55, 0x404040, false);
    }

    private static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }
}
