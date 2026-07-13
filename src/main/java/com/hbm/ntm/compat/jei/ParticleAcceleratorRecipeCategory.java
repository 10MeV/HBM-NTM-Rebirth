package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.ParticleAcceleratorRecipe;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ParticleAcceleratorRecipeCategory implements HbmJeiRecipeCategory<ParticleAcceleratorRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei.png");

    private final RecipeType<ParticleAcceleratorRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    ParticleAcceleratorRecipeCategory(RecipeType<ParticleAcceleratorRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.slotBackground = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 87, 18, 18);
        this.machineBackground = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 59, 87, 18, 36);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<ParticleAcceleratorRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.pa_detector", "Particle Detector");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ParticleAcceleratorRecipe recipe, IFocusGroup focuses) {
        addInput(builder, recipe.input1(), 48, 24);
        addInput(builder, recipe.input2(), 30, 24);
        builder.addOutputSlot(102, 24)
                .addItemStack(recipe.output1())
                .setBackground(slotBackground, -1, -1);
        if (!recipe.output2().isEmpty()) {
            builder.addOutputSlot(120, 24)
                    .addItemStack(recipe.output2())
                    .setBackground(slotBackground, -1, -1);
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 31)
                .setBackground(machineBackground, -1, -17)
                .addItemStack(catalyst);
    }

    @Override
    public void draw(ParticleAcceleratorRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        guiGraphics.drawString(Minecraft.getInstance().font,
                "Momentum: " + String.format(Locale.US, "%,d", recipe.momentum()), 8, 52, 0x404040, false);
    }

    static List<ParticleAcceleratorRecipe> sorted(List<ParticleAcceleratorRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparingInt(ParticleAcceleratorRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    private void addInput(IRecipeLayoutBuilder builder, HbmIngredient input, int x, int y) {
        builder.addInputSlot(x, y)
                .addItemStacks(input.displayStacks())
                .setBackground(slotBackground, -1, -1);
    }
}
